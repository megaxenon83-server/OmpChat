package com.omp.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.omp.chat.ui.Strings

/**
 * Diskret pinne-knapp for chat-meldinger.
 * Viser et lite pushpin-ikon uten tekst. Ved trykk pinnes eller opp-pinnes meldingen.
 *
 * Popup rendres via Compose Popup (eget overlay-vindu), så den ikke påvirker
 * header-radens måling — chippen holder 32dp og flytter seg ikke når menyen åpnes.
 *
 * [isPinned] angjør om meldingen er pinnet nå.
 * [onTogglePin] kalles med true for å pinne, false for å opp-pinne.
 * [onToast] kalles med bekreftelsesmelding.
 */
@Composable
fun PinMessageChip(
    strings: Strings,
    messageId: String,
    isPinned: Boolean,
    onTogglePin: (String) -> Unit = {},
    onToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showPopup by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Diskret ikon-knapp (32dp, fast størrelse)
        Surface(
            onClick = {
                showPopup = true
                onTogglePin(messageId)
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                    contentDescription = if (isPinned) strings.unpinMessage else strings.pinMessage,
                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bekreftelses-popup i eget overlay-vindu - påvirker ikke header-layout
        if (showPopup) {
            val density = LocalDensity.current
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, with(density) { 40.dp.roundToPx() }),
                onDismissRequest = { showPopup = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(200.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isPinned) strings.pinnedMessages else strings.pinMessage,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isPinned) "Meldingen er nå heftet øverst i chatten."
                                 else "Meldingen er nå løsnet.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}