package com.omp.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omp.chat.data.EndpointProbe
import com.omp.chat.data.EndpointResolver
import com.omp.chat.data.PcProfile
import com.omp.chat.ui.AppViewModel
import com.omp.chat.ui.LocalStrings
import com.omp.chat.ui.theme.NimayAccent
import com.omp.chat.ui.theme.NimayActive
import com.omp.chat.ui.theme.NimayBackground
import com.omp.chat.ui.theme.NimayDivider
import com.omp.chat.ui.theme.NimayError
import com.omp.chat.ui.theme.NimayInput
import com.omp.chat.ui.theme.NimaySidebar
import com.omp.chat.ui.theme.NimaySurface
import com.omp.chat.ui.theme.NimayTextPrimary
import com.omp.chat.ui.theme.NimayTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PcManagerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onScan: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val profiles by viewModel.pcProfileList.collectAsState()
    val activeId by viewModel.activePcId.collectAsState()
    val statusMap by viewModel.pcStatus.collectAsState()

    var editing by remember { mutableStateOf<PcProfile?>(null) }
    var creating by remember { mutableStateOf(false) }
    var busyId by remember { mutableStateOf<String?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var probeReport by remember { mutableStateOf<Map<String, List<EndpointProbe>>>(emptyMap()) }
    var refreshTick by remember { mutableStateOf(0) }

    LaunchedEffect(profiles, refreshTick) {
        // Test alle kjente adresser per PC (LAN, Tailscale, VPN) og vis hvilken
        // som faktisk svarte – aldri bare "Utilgjengelig".
        for (p in profiles) {
            viewModel.updatePcStatus(p.id, "checking")
            scope.launch {
                val probes = withContext(Dispatchers.IO) { EndpointResolver.probeAll(p) }
                probeReport = probeReport + (p.id to probes)
                val winner = probes.firstOrNull { it.ok }
                if (winner != null) {
                    viewModel.updatePcStatus(p.id, "connected")
                    if (winner.url != p.baseUrl) viewModel.promoteEndpoint(winner.url, p.id)
                } else {
                    viewModel.updatePcStatus(p.id, "offline")
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NimayBackground)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake", tint = NimayTextPrimary)
            }
            Text(
                "PC-er",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NimayTextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { refreshTick++ }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Test alle adresser", tint = NimayTextSecondary)
            }
            IconButton(onClick = onScan, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.QrCode, contentDescription = "Skann QR-kode", tint = NimayAccent)
            }
            IconButton(onClick = { creating = true }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Legg til PC", tint = NimayAccent)
            }
        }

        if (profiles.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Ingen PC-er registrert ennå.", color = NimayTextSecondary)
                Spacer(Modifier.height(8.dp))
                Text("Legg til en PC for å starte.", color = NimayTextSecondary, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { creating = true }) { Text("Legg til PC") }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(profiles, key = { it.id }) { profile ->
                    val status = statusMap[profile.id] ?: "unknown"
                    PcCard(
                        profile = profile,
                        active = profile.id == activeId,
                        status = status,
                        probes = probeReport[profile.id].orEmpty(),
                        busy = busyId == profile.id,
                        onConnect = {
                            val isActive = profile.id == activeId
                            if (isActive) {
                                viewModel.disconnectPc(profile.id)
                                toast = "Frakoblet ${profile.name}"
                            } else {
                                busyId = profile.id
                                viewModel.switchPc(profile.id)
                                busyId = null
                                toast = "Byttet til ${profile.name}"
                            }
                        },
                        onWake = {
                            busyId = profile.id
                            viewModel.wakePc(profile) { ok ->
                                busyId = null
                                toast = if (ok) "WOL-pakke sendt til ${profile.name}" else "Kunne ikke sende WOL-pakke"
                            }
                        },
                        onEdit = { editing = profile },
                        onDelete = { viewModel.deletePcProfile(profile.id); toast = "Slettet ${profile.name}" }
                    )
                }
            }
        }
    }

    if (creating) {
        PcEditDialog(
            profile = null,
            onDismiss = { creating = false },
            onSave = { p ->
                viewModel.savePcProfile(p)
                creating = false
                toast = "Lagt til ${p.name}"
            }
        )
    }
    editing?.let { p ->
        PcEditDialog(
            profile = p,
            onDismiss = { editing = null },
            onSave = { updated ->
                viewModel.savePcProfile(updated)
                editing = null
                toast = "Oppdatert ${updated.name}"
            }
        )
    }

    toast?.let { t ->
        LaunchedEffect(t) {
            kotlinx.coroutines.delay(2500)
            toast = null
        }
        androidx.compose.material3.Snackbar(
            modifier = Modifier.padding(12.dp)
        ) { Text(t) }
    }
}

@Composable
private fun PcCard(
    profile: PcProfile,
    active: Boolean,
    status: String,
    probes: List<EndpointProbe>,
    busy: Boolean,
    onConnect: () -> Unit,
    onWake: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (status) {
        "connected" -> NimayActive
        "pc_available" -> NimayAccent
        "starting", "checking", "wol_sent" -> NimayAccent
        "offline" -> NimayError
        else -> NimayTextSecondary
    }
    val statusText = when (status) {
        "connected" -> "OMP Bridge tilkoblet"
        "pc_available" -> "PC tilgjengelig"
        "starting", "wol_sent" -> "WOL sendt"
        "checking" -> "Sjekker…"
        "offline" -> "Utilgjengelig"
        else -> "Ukjent"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (active) NimayAccent.copy(alpha = 0.12f) else NimaySurface
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    profile.name,
                    color = NimayTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (active) {
                    Text("Aktiv", color = NimayAccent, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(profile.baseUrl, color = NimayTextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (profile.mac.isNotBlank()) {
                Text("MAC: ${profile.mac}", color = NimayTextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            Text("Status: $statusText", color = statusColor, style = MaterialTheme.typography.labelSmall)
            if (probes.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                probes.forEach { probe ->
                    Text(
                        "${EndpointResolver.hostLabel(probe.url)}: ${if (probe.ok) "svarte" else probe.reason}",
                        color = if (probe.ok) NimayActive else NimayTextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onConnect,
                    enabled = !busy && profile.baseUrl.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (busy) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else if (active) {
                        Text("Koble fra")
                    } else {
                        Text("Bytt til")
                    }
                }
                TextButton(onClick = onWake, enabled = profile.mac.isNotBlank() && !busy) {
                    Text("Vekk", color = NimayAccent, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Rediger", tint = NimayTextSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Slett", tint = NimayError)
                }
            }
        }
    }
}

@Composable
private fun PcEditDialog(
    profile: PcProfile?,
    onDismiss: () -> Unit,
    onSave: (PcProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var baseUrl by remember { mutableStateOf(profile?.baseUrl ?: "") }
    var token by remember { mutableStateOf(profile?.token ?: "") }
    var mac by remember { mutableStateOf(profile?.mac ?: "") }
    var broadcast by remember { mutableStateOf(profile?.wolBroadcast ?: "255.255.255.255") }
    var wolPort by remember { mutableStateOf((profile?.wolPort ?: 9).toString()) }
    var useDocker by remember { mutableStateOf(profile?.useDockerWol ?: false) }
    var dockerUrl by remember { mutableStateOf(profile?.dockerWolUrl ?: "") }
    var dockerToken by remember { mutableStateOf(profile?.dockerWolToken ?: "") }
    var autoConnect by remember { mutableStateOf(profile?.autoConnect ?: true) }
    var extraEndpoints by remember { mutableStateOf(profile?.endpoints?.joinToString("\n").orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Ny PC" else "Rediger PC", color = NimayTextPrimary) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Navn") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Adresse (http://ip:7777)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = extraEndpoints,
                    onValueChange = { extraEndpoints = it },
                    label = { Text("Ekstra adresser (LAN/Tailscale), én per linje") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("Token") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = mac, onValueChange = { mac = it }, label = { Text("MAC-adresse (for WOL)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = broadcast, onValueChange = { broadcast = it }, label = { Text("Broadcast") }, singleLine = true, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(6.dp))
                    OutlinedTextField(value = wolPort, onValueChange = { wolPort = it }, label = { Text("Port") }, singleLine = true, modifier = Modifier.width(70.dp))
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = useDocker, onCheckedChange = { useDocker = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Bruk sentral WOL-tjeneste", color = NimayTextPrimary, style = MaterialTheme.typography.labelSmall)
                }
                if (useDocker) {
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = dockerUrl, onValueChange = { dockerUrl = it }, label = { Text("WOL-tjeneste URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(value = dockerToken, onValueChange = { dockerToken = it }, label = { Text("WOL-tjeneste token") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = autoConnect, onCheckedChange = { autoConnect = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Automatisk tilkobling", color = NimayTextPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || baseUrl.isBlank()) return@TextButton
                onSave(
                    PcProfile(
                        id = profile?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name.trim(),
                        baseUrl = baseUrl.trim(),
                        token = token.trim(),
                        mac = mac.trim(),
                        wolBroadcast = broadcast.trim().ifBlank { "255.255.255.255" },
                        wolPort = wolPort.toIntOrNull() ?: 9,
                        lastCwd = profile?.lastCwd ?: "",
                        lastSession = profile?.lastSession ?: "",
                        autoConnect = autoConnect,
                        useDockerWol = useDocker,
                        dockerWolUrl = dockerUrl.trim(),
                        dockerWolToken = dockerToken.trim(),
                        endpoints = extraEndpoints.lines()
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .distinct()
                    )
                )
            }) { Text("Lagre", color = NimayAccent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Avbryt", color = NimayTextSecondary) }
        },
        containerColor = NimaySidebar
    )
}
