package com.omp.chat.data

import android.util.Base64
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * HTTP + Server-Sent-Events client for the OmpChat bridge.
 *
 * The bridge owns all OMP RPC traffic; the app only speaks this small REST
 * surface. Every call carries the pairing token.
 */
class BridgeClient(
    private val baseUrlProvider: () -> String,
    private val tokenProvider: () -> String?,
    /** Alle kjente adresser til samme PC (LAN, Tailscale, VPN), primær først. */
    private val candidatesProvider: () -> List<String> = { emptyList() },
    /** Kalles når en annen adresse enn primæradressen faktisk svarte. */
    private val onEndpointResolved: (String) -> Unit = { }
) {

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val streamClient: OkHttpClient = client.newBuilder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    /**
     * Streaming client with a read gap guard. The bridge writes `: ping`
     * heartbeats every 15 s, so a 45 s silent gap means the socket is dead
     * (Android Doze / Wi-Fi switch / killed NAT) and the stream must be
     * rebuilt. With readTimeout 0 a half-open socket would block the reader
     * forever and the app would look frozen after returning from background.
     */
    private val watchdogStreamClient: OkHttpClient = streamClient.newBuilder()
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    // -----------------------------------------------------------------------
    // Pairing / health
    // -----------------------------------------------------------------------

    fun ping(baseUrl: String): JsonObject {
        val request = Request.Builder()
            .url(join(baseUrl, "/api/ping"))
            .get()
            .build()
        return execute(request)
    }

    fun health(): JsonObject = call("GET", "/api/health")

    // -----------------------------------------------------------------------
    // REST surface
    // -----------------------------------------------------------------------

    fun projects(): List<Project> =
        call("GET", "/api/projects").arr("projects").mapNotNull { it.asObjectOrNull()?.let(Parsers::project) }

    fun sessions(cwd: String? = null, archived: Boolean = false): List<SessionSummary> {
        val query = buildString {
            append("/api/sessions")
            val params = mutableListOf<String>()
            if (!cwd.isNullOrBlank()) params += "cwd=${encode(cwd)}"
            if (archived) params += "archived=1"
            if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
        }
        return call("GET", query).arr("sessions").mapNotNull { it.asObjectOrNull()?.let(Parsers::session) }
    }

    fun transcript(file: String): Pair<SessionSummary?, List<ChatMessage>> {
        val json = call("GET", "/api/session/messages?file=${encode(file)}")
        val session = json.obj("session")?.let(Parsers::session)
        val messages = json.arr("messages").mapNotNull { element ->
            element.asObjectOrNull()?.let(Parsers::message)
        }
        return session to messages
    }

    fun openSession(file: String): Pair<ChatState, List<ChatMessage>> {
        val json = call("POST", "/api/sessions/open", JsonObject().apply { addProperty("file", file) }, 90)
        val state = Parsers.state(json.obj("state"))
        val messages = json.arr("messages").mapNotNull { it.asObjectOrNull()?.let(Parsers::message) }
        return state to messages
    }

    fun newSession(cwd: String): Pair<SessionSummary?, ChatState> {
        val json = call("POST", "/api/sessions/new", JsonObject().apply { addProperty("cwd", cwd) }, 90)
        return json.obj("session")?.let(Parsers::session) to Parsers.state(json.obj("state"))
    }

    fun renameSession(file: String, name: String) {
        call(
            "POST", "/api/sessions/name",
            JsonObject().apply {
                addProperty("file", file)
                addProperty("name", name)
            }
        )
    }

    fun archiveSession(file: String, archived: Boolean) {
        call(
            "POST", "/api/sessions/archive",
            JsonObject().apply {
                addProperty("file", file)
                addProperty("archived", archived)
            }
        )
    }

    fun deleteSession(file: String) {
        call("DELETE", "/api/session?file=${encode(file)}")
    }

    fun state(cwd: String): ChatState =
        Parsers.state(call("GET", "/api/state?cwd=${encode(cwd)}").obj("state"))

    /** Raw `state` object from GET /api/state, for the reconnect resync path. */
    fun stateRaw(cwd: String): JsonObject? =
        call("GET", "/api/state?cwd=${encode(cwd)}").obj("state")

    fun models(cwd: String): List<ModelInfo> =
        call("GET", "/api/models?cwd=${encode(cwd)}").arr("models")
            .mapNotNull { it.asObjectOrNull()?.let(Parsers::model) }

    fun setModel(cwd: String, model: ModelInfo): ModelInfo? {
        val json = call(
            "POST", "/api/model",
            JsonObject().apply {
                addProperty("cwd", cwd)
                addProperty("provider", model.provider)
                addProperty("modelId", model.id)
            },
            90
        )
        return json.obj("model")?.let(Parsers::model)
    }

    fun prompt(cwd: String, file: String?, text: String, images: List<String> = emptyList()) {
        val body = JsonObject().apply {
            addProperty("cwd", cwd)
            if (!file.isNullOrBlank()) addProperty("file", file)
            addProperty("text", text)
            if (images.isNotEmpty()) {
                val array = com.google.gson.JsonArray()
                images.forEach { dataUrl ->
                    val mime = dataUrl.substringAfter("data:", "image/jpeg").substringBefore(';')
                    val data = dataUrl.substringAfter("base64,", dataUrl)
                    array.add(JsonObject().apply {
                        addProperty("type", "image")
                        addProperty("mimeType", mime)
                        addProperty("data", data)
                    })
                }
                add("images", array)
            }
        }
        call("POST", "/api/prompt", body, 90)
    }

    fun uploadFiles(cwd: String, files: List<Triple<String, String, String>>): List<String> {
        // Triple(name, mimeType, base64)
        val array = com.google.gson.JsonArray()
        files.forEach { (name, _, data) ->
            array.add(JsonObject().apply {
                addProperty("name", name)
                addProperty("data", data)
            })
        }
        val json = call(
            "POST", "/api/upload",
            JsonObject().apply {
                addProperty("cwd", cwd)
                add("files", array)
            },
            120
        )
        return json.arr("files").mapNotNull { it.asObjectOrNull()?.str("relative") }
    }

    fun abort(cwd: String) {
        call("POST", "/api/abort", JsonObject().apply { addProperty("cwd", cwd) })
    }

    /** Generell JSON-kall for funksjoner utenfor chatten (f.eks. filbehandler). */
    fun callJson(method: String, pathParam: String, body: JsonObject? = null, timeoutSeconds: Long = 60): JsonObject =
        call(method, pathParam, body, timeoutSeconds)

    /** Endre OMPs godkjenningsmodus (always-ask / default / ask). */
    fun setApprovalMode(mode: String) {
        call("POST", "/api/approval", JsonObject().apply { addProperty("mode", mode) })
    }

    /** Sett aktiv STT- eller TTS-motor i voice-config.json på PC-en, valgfri stemme. */
    fun selectVoiceEngine(kind: String, engineId: String, voice: String? = null) {
        val body = JsonObject().apply {
            addProperty("kind", kind)
            addProperty("engineId", engineId)
            if (!voice.isNullOrBlank()) addProperty("voice", voice)
        }
        call("POST", "/api/voice/select", body)
    }

    /** Starter bakgrunnsinstallasjon av en tale-motor på PC-en. */
    fun installVoiceEngine(kind: String, engineId: String) {
        val body = JsonObject().apply {
            addProperty("kind", kind)
            addProperty("engine", engineId)
        }
        call("POST", "/api/voice/install", body, 15)
    }

    fun regenerate(cwd: String, file: String?, entryId: String, text: String) {
        call(
            "POST", "/api/regenerate",
            JsonObject().apply {
                addProperty("cwd", cwd)
                if (!file.isNullOrBlank()) addProperty("file", file)
                addProperty("entryId", entryId)
                addProperty("text", text)
            },
            120
        )
    }

    fun answer(request: UiRequest, value: String? = null, confirmed: Boolean? = null, cancelled: Boolean = false, answers: List<Pair<String, List<String>>>? = null) {
        val body = JsonObject().apply {
            addProperty("cwd", request.cwd.ifBlank { baseCwdFallback })
            addProperty("id", request.id)
            when {
                cancelled -> addProperty("cancelled", true)
                answers != null -> {
                    val array = com.google.gson.JsonArray()
                    answers.forEach { (id, selected) ->
                        array.add(JsonObject().apply {
                            addProperty("id", id)
                            val list = com.google.gson.JsonArray()
                            selected.forEach { list.add(it) }
                            add("selectedOptions", list)
                        })
                    }
                    add("answers", array)
                }
                confirmed != null -> addProperty("confirmed", confirmed)
                else -> addProperty("value", value ?: "")
            }
        }
        call("POST", "/api/ask", body)
    }

    /** Used when a UI request arrives without a cwd (it always carries one in practice). */
    var baseCwdFallback: String = ""

    fun bash(cwd: String, command: String): JsonObject {
        val json = call(
            "POST", "/api/bash",
            JsonObject().apply {
                addProperty("cwd", cwd)
                addProperty("command", command)
            },
            180
        )
        return json.obj("result") ?: JsonObject()
    }

    fun diagnostics(): JsonObject = call("GET", "/api/diagnostics")

    // -----------------------------------------------------------------------
    // Voice (local STT/TTS engines on the PC)
    // -----------------------------------------------------------------------

    fun voiceEngines(): JsonObject = call("GET", "/api/voice/engines", null, 90)

    /** Parsed tale-motorliste fra /api/voice/engines. */
    fun voiceEngineList(): VoiceEngineList {
        val json = call("GET", "/api/voice/engines", null, 90)
        val engines = json.obj("engines") ?: JsonObject()
        val sttObj = json.obj("stt") ?: JsonObject()
        val active = json.obj("active") ?: JsonObject()

        val ttsMap = mutableMapOf<String, VoiceEngine>()
        engines.entrySet().forEach { entry ->
            val obj = entry.value.asObjectOrNull() ?: return@forEach
            val voices = obj.obj("voices")?.let { arr ->
                val list = mutableListOf<VoiceEntry>()
                arr.entrySet().forEach { voiceElem ->
                    val v = voiceElem.value.asObjectOrNull() ?: return@forEach
                    list.add(VoiceEntry(id = v.str("id"), name = v.str("name").ifBlank { v.str("id") }))
                }
                list
            } ?: emptyList()
            ttsMap[entry.key] = VoiceEngine(
                id = obj.str("id").ifBlank { entry.key },
                name = obj.str("name").ifBlank { entry.key },
                lang = obj.str("lang"),
                type = obj.str("type"),
                hardware = obj.str("hardware"),
                description = obj.str("description"),
                downloadMB = obj.int("downloadMB"),
                installed = obj.bool("installed"),
                verified = obj.bool("verified"),
                bytes = obj.long("bytes"),
                voices = voices,
                voice = obj.str("voice")
            )
        }

        val sttMap = mutableMapOf<String, VoiceEngine>()
        val sttEngines = sttObj.obj("engines") ?: JsonObject()
        sttEngines.entrySet().forEach { entry ->
            val obj = entry.value.asObjectOrNull() ?: return@forEach
            sttMap[entry.key] = VoiceEngine(
                id = obj.str("id").ifBlank { entry.key },
                name = obj.str("name").ifBlank { entry.key },
                lang = obj.str("lang"),
                type = obj.str("type"),
                hardware = obj.str("hardware"),
                description = obj.str("description"),
                downloadMB = obj.int("downloadMB"),
                installed = obj.bool("installed"),
                verified = obj.bool("verified"),
                bytes = obj.long("bytes")
            )
        }

        val settingsMap = mutableMapOf<String, Any?>()
        active.obj("settings")?.entrySet()?.forEach { entry ->
            val v = entry.value
            settingsMap[entry.key] = when {
                v.isJsonPrimitive -> v.asString
                v.isJsonObject -> v.toString()
                v.isJsonArray -> v.toString()
                else -> v.toString()
            }
        }

        return VoiceEngineList(
            sttEngine = sttObj.str("engine"),
            ttsEngine = active.str("engine"),
            ttsVoice = active.str("voice"),
            sttEngines = sttMap.values.toList(),
            ttsEngines = ttsMap.values.toList(),
            installed = json.bool("installed"),
            python = json.bool("python"),
            profile = active.str("profile"),
            settings = settingsMap
        )
    }

    fun voiceTranscribe(wav: ByteArray, timeoutSeconds: Long = 240): VoiceTranscription {
        val body = JsonObject().apply {
            addProperty("audio", Base64.encodeToString(wav, Base64.NO_WRAP))
        }
        val json = call("POST", "/api/voice/transcribe", body, timeoutSeconds)
        if (json.get("ok")?.asBoolean == false) {
            throw BridgeException(json.str("error").ifBlank { "Transkriberingen feilet" })
        }
        return VoiceTranscription(
            text = json.str("text"),
            engine = json.str("engine"),
            duration = json.get("duration")?.asDouble ?: 0.0
        )
    }

    fun voiceSpeak(text: String, timeoutSeconds: Long = 180): VoiceClip {
        val body = JsonObject().apply { addProperty("text", text) }
        val json = call("POST", "/api/voice/speak", body, timeoutSeconds)
        val url = json.str("audioUrl")
        if (url.isBlank()) throw BridgeException(json.str("error").ifBlank { "Talesyntesen feilet" })
        return VoiceClip(
            audioId = json.str("audioId"),
            audioUrl = url,
            duration = json.get("duration")?.asDouble ?: 0.0,
            engine = json.str("engine")
        )
    }

    /** Downloads a TTS WAV, accepting either an absolute or bridge-relative URL. */
    fun voiceAudio(urlOrPath: String): ByteArray {
        val url = if (urlOrPath.startsWith("http://") || urlOrPath.startsWith("https://")) {
            urlOrPath
        } else {
            join(baseUrlProvider(), urlOrPath)
        }
        val request = Request.Builder()
            .url(url)
            .apply { tokenProvider()?.let { header("Authorization", "Bearer $it") } }
            .get()
            .build()
        return executeBytes(request)
    }

    // -----------------------------------------------------------------------
    // Events (SSE)
    // -----------------------------------------------------------------------

    fun events(cwd: String?): Flow<Event> = flow {
        var attempt = 0
        while (currentCoroutineContext().isActive) {
            var connected = false
            var lastError: Exception? = null
            val urls = urlsToTry()
            for ((index, base) in urls.withIndex()) {
                try {
                    val url = join(base, "/api/events") + if (cwd.isNullOrBlank()) "" else "?cwd=${encode(cwd)}"
                    val request = Request.Builder()
                        .url(url)
                        .header("Accept", "text/event-stream")
                        .apply { tokenProvider()?.let { header("Authorization", "Bearer $it") } }
                        .build()
                    // The watchdog client turns a silent gap (no heartbeat from the
                    // bridge for 45 s) into a recoverable read failure instead of
                    // blocking forever on a half-open socket.
                    watchdogStreamClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw BridgeException("Sanntidsstrøm avvist (HTTP ${response.code})", response.code)
                        }
                        if (index > 0) onEndpointResolved(base)
                        connected = true
                        attempt = 0
                        emit(Event.of("connected"))
                        val source = response.body?.source() ?: return@use
                        var data = StringBuilder()
                        while (currentCoroutineContext().isActive) {
                            // Throws IOException (SocketTimeoutException) on a
                            // silent gap; the retry loop below rebuilds the stream.
                            val line = source.readUtf8Line() ?: break
                            when {
                                line.isEmpty() -> {
                                    if (data.isNotEmpty()) {
                                        parseEvent(data.toString())?.let { emit(it) }
                                        data = StringBuilder()
                                    }
                                }
                                line.startsWith("data:") -> data.append(line.removePrefix("data:").trim())
                                // ": ping" heartbeats and comments are ignored on purpose;
                                // their arrival (read) is what defeats the 45 s timeout.
                            }
                        }
                    }
                    lastError = null
                    break
                } catch (error: CancellationException) {
                    throw error
                } catch (error: BridgeException) {
                    // Serveren svarte – dette er ikke en nettverksfeil, så vi
                    // prøver ikke andre adresser.
                    lastError = error
                    break
                } catch (error: Exception) {
                    lastError = error
                    android.util.Log.d(
                        "OmpChat",
                        "events feilet mot $base: ${error.javaClass.simpleName}: ${error.message}"
                    )
                }
            }
            if (!currentCoroutineContext().isActive) break
            if (connected) {
                emit(Event.of("disconnected"))
            } else {
                val error = lastError
                emit(
                    Event(
                        "connection_error",
                        JsonObject().apply {
                            addProperty("error", error?.message ?: "Ukjent tilkoblingsfeil")
                            addProperty("status", (error as? BridgeException)?.status ?: 0)
                        }
                    )
                )
            }
            attempt++
            if (!connected) {
                // Back off only on failed attempts; a closed stream retries quickly.
                delay(min(15000L, 800L * (1L shl min(attempt, 4))))
            } else {
                delay(1200)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun parseEvent(raw: String): Event? {
        return try {
            val json = JsonParser.parseString(raw).asJsonObject
            val type = json.str("type")
            if (type.isEmpty()) null else Event(type, json)
        } catch (error: Exception) {
            null
        }
    }

    // -----------------------------------------------------------------------
    // Plumbing
    // -----------------------------------------------------------------------

    private fun call(method: String, path: String, body: JsonObject? = null, timeoutSeconds: Long = 45): JsonObject {
        val urls = urlsToTry()
        var lastError: Exception? = null
        for ((index, base) in urls.withIndex()) {
            val builder = Request.Builder()
                .url(join(base, path))
                .apply { tokenProvider()?.let { header("Authorization", "Bearer $it") } }
            val requestBody = body?.toString()?.toRequestBody(jsonType)
            when (method.uppercase()) {
                "GET" -> builder.get()
                "POST" -> builder.post(requestBody ?: "{}".toRequestBody(jsonType))
                "DELETE" -> builder.delete()
                "PUT" -> builder.put(requestBody ?: "{}".toRequestBody(jsonType))
                else -> error("Ukjent HTTP-metode $method")
            }
            try {
                val result = execute(builder.build(), timeoutSeconds)
                if (index > 0) onEndpointResolved(base)
                return result
            } catch (error: java.io.IOException) {
                // Nettverksfeil: prøv neste adresse (typisk LAN -> Tailscale).
                lastError = error
                android.util.Log.d(
                    "OmpChat",
                    "bridge $path feilet mot $base: ${error.javaClass.simpleName}: ${error.message}"
                )
            }
        }
        throw lastError ?: BridgeException("Ingen adresse å koble til")
    }

    /** Primæradresse først, deretter alle kjente alternativer. */
    private fun urlsToTry(): List<String> {
        val all = buildList {
            add(baseUrlProvider())
            addAll(candidatesProvider())
        }
        return all.map { it.trimEnd('/') }.filter { it.isNotBlank() }.distinct()
    }

    private fun execute(request: Request, timeoutSeconds: Long = 45): JsonObject {
        val scoped = if (timeoutSeconds > 0) {
            client.newBuilder()
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build()
        } else {
            client
        }
        scoped.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val json = try {
                if (text.isBlank()) JsonObject() else JsonParser.parseString(text).asJsonObject
            } catch (error: Exception) {
                throw BridgeException("Uventet svar fra bridge (HTTP ${response.code})", response.code)
            }
            if (!response.isSuccessful) {
                throw BridgeException(json.str("error").ifBlank { "HTTP ${response.code}" }, response.code)
            }
            return json
        }
    }

    private fun executeBytes(request: Request, timeoutSeconds: Long = 120): ByteArray {
        val scoped = client.newBuilder()
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build()
        scoped.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val text = response.body?.string().orEmpty()
                val message = try {
                    JsonParser.parseString(text).asJsonObject.str("error")
                } catch (error: Exception) {
                    ""
                }
                throw BridgeException(message.ifBlank { "HTTP ${response.code}" }, response.code)
            }
            return response.body?.bytes() ?: ByteArray(0)
        }
    }

    private fun join(base: String, path: String): String = base.trimEnd('/') + path

    private fun encode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")
}

/** Result of `POST /api/voice/transcribe`. */
data class VoiceTranscription(
    val text: String,
    val engine: String,
    val duration: Double
)

/** Result of `POST /api/voice/speak`; [audioUrl] is playable through [BridgeClient.voiceAudio]. */
data class VoiceClip(
    val audioId: String,
    val audioUrl: String,
    val duration: Double,
    val engine: String
)
