package com.omp.chat.data

import android.content.Context
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val androidVersion: String = "",
    val androidVersionCode: Int = 0,
    val androidUrl: String = "",
    val androidSha256: String = "",
    val windowsBridgeVersion: String = "",
    val linuxBridgeVersion: String = "",
    val installerVersion: String = "",
    val minProtocolVersion: Int = 0
)

object UpdateChecker {
    const val UPDATE_URL = "https://chat.nimay.net/nimayomp/update.json"

    fun fetch(): UpdateInfo? {
        return try {
            val text = URL(UPDATE_URL).openStream().bufferedReader().readText()
            val root = JSONObject(text)
            val stable = root.optJSONObject("channels")?.optJSONObject("stable") ?: return null
            val android = stable.optJSONObject("android")
            UpdateInfo(
                androidVersion = android?.optString("version").orEmpty(),
                androidVersionCode = android?.optInt("versionCode", 0) ?: 0,
                androidUrl = android?.optString("url").orEmpty(),
                androidSha256 = android?.optString("sha256").orEmpty(),
                windowsBridgeVersion = stable.optJSONObject("windowsBridge")?.optString("version").orEmpty() ?: "",
                linuxBridgeVersion = stable.optJSONObject("linuxBridge")?.optString("version").orEmpty() ?: "",
                installerVersion = stable.optJSONObject("installer")?.optString("version").orEmpty() ?: "",
                minProtocolVersion = root.optInt("minProtocolVersion", 0)
            )
        } catch (e: Exception) {
            null
        }
    }
}
