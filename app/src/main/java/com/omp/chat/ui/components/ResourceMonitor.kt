package com.omp.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omp.chat.data.PcResources

/**
 * Diskret ressursmonitor-knapp for chat-header.
 * Viser en liten chip uten farger. Ved trykk vises popup med CPU/RAM/GPU.
 */
@Composable
fun ResourceMonitorChip(
    resources: PcResources?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    var showPopup by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Diskret chip ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â ingen farger, bare tekst
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
                .clickable { showPopup = !showPopup }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                if (resources == null) "PC ..." else "PC ${resources.cpuPercent}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Popup med detaljer
        if (showPopup) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 28.dp)
                    .width(200.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (resources == null) {
                        Text("Laster ressurser...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        ResourceRow("CPU", resources.cpuPercent, "${resources.cpuCores} kjerner")
                    ResourceRow("RAM", resources.ramPercent, formatBytes(resources.ramUsedBytes) + " / " + formatBytes(resources.ramTotalBytes))
                    if (resources.gpuName.isNotBlank()) {
                        ResourceRow("GPU", resources.gpuPercent, resources.gpuName.take(20))
                    }
                    if (resources.gpuMemoryTotalBytes > 0) {
                        Text(
                            "GPU-minne: ${formatBytes(resources.gpuMemoryUsedBytes)} / ${formatBytes(resources.gpuMemoryTotalBytes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 9.sp
                        )
                    }
                    }
                    Text(
                        "Oppdatert: ${formatTime(resources?.timestamp ?: 0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceRow(label: String, percent: Int, detail: String) {
    val color = when {
        percent < 60 -> Color(0xFF4CAF50)  // GrÃƒÆ’Ã‚Â¸nn
        percent < 85 -> Color(0xFFFF9800)  // Oransje
        else -> Color(0xFFF44336)            // RÃƒÆ’Ã‚Â¸d
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text("$percent%", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
    Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 9.sp)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val gb = bytes / (1024.0 * 1024 * 1024)
    return if (gb >= 1) "%.1f GB".format(gb) else "%.0f MB".format(bytes / (1024.0 * 1024))
}

private fun formatTime(timestamp: Long): String {
    if (timestamp <= 0) return "ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â"
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
