package com.omp.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omp.chat.data.RepoState
import com.omp.chat.data.SessionSummary
import com.omp.chat.ui.theme.NimayAccent
import com.omp.chat.ui.theme.NimayBackground
import com.omp.chat.ui.theme.NimayDivider
import com.omp.chat.ui.theme.NimayInput
import com.omp.chat.ui.theme.NimayTextPrimary
import com.omp.chat.ui.theme.NimayTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ProjectHistoryScreen(
    projectPath: String,
    state: RepoState,
    onBack: () -> Unit,
    onOpenSession: (SessionSummary) -> Unit,
    onLoadPage: suspend (Int) -> List<SessionSummary>
) {
    val scheme = MaterialTheme.colorScheme
    var sessions by remember { mutableStateOf<List<SessionSummary>>(emptyList()) }
    var offset by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(projectPath) {
        loading = true
        val page = onLoadPage(0)
        sessions = page
        offset = page.size
        hasMore = page.size >= 20
        loading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NimayBackground)
            .padding(top = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake", tint = NimayTextPrimary)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = projectPath.substringAfterLast('\\').substringAfterLast('/').ifBlank { "Prosjekt" },
                    style = MaterialTheme.typography.titleMedium,
                    color = NimayTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = projectPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = NimayTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (loading) {
            BoxFill { CircularProgressIndicator(color = NimayAccent) }
        } else if (sessions.isEmpty()) {
            BoxFill { Text("Ingen samtaler", color = NimayTextSecondary) }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(sessions, key = { it.file }) { session ->
                    Surface(
                        color = NimayInput,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSession(session) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                session.title.ifBlank { session.file.substringAfterLast('/') },
                                style = MaterialTheme.typography.bodyMedium,
                                color = NimayTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(session.updatedAt.toLong())),
                                style = MaterialTheme.typography.labelSmall,
                                color = NimayTextSecondary
                            )
                        }
                    }
                }
                if (hasMore) {
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    loadingMore = true
                                    val page = onLoadPage(offset)
                                    sessions = sessions + page
                                    offset += page.size
                                    hasMore = page.size >= 20
                                    loadingMore = false
                                }
                            },
                            enabled = !loadingMore,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loadingMore) {
                                CircularProgressIndicator(color = NimayTextPrimary, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Last flere")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxFill(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { content() }
}
