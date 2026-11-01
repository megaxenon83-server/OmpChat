package com.omp.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omp.chat.data.ModelInfo
import com.omp.chat.data.RepoState
import com.omp.chat.data.SessionSummary
import com.omp.chat.ui.LocalStrings
import com.omp.chat.ui.theme.NimayAccent
import com.omp.chat.ui.theme.NimayDivider
import com.omp.chat.ui.theme.NimayInput
import com.omp.chat.ui.theme.NimaySidebar
import com.omp.chat.ui.theme.NimayTextPrimary
import com.omp.chat.ui.theme.NimayTextSecondary
import com.omp.chat.ui.theme.NimayUserBubble

@Composable
fun ChatGptDrawerContent(
    state: RepoState,
    currentSessionFile: String,
    onOpenSession: (SessionSummary) -> Unit,
    onNewChat: (String) -> Unit,
    onSelectProject: (String) -> Unit,
    onOpenModelPicker: () -> Unit,
    onRename: (SessionSummary) -> Unit,
    onArchive: (SessionSummary, Boolean) -> Unit,
    onDelete: (SessionSummary) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFiles: () -> Unit,
    onOpenPcs: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenGit: () -> Unit,
    onReconnect: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    isFavorite: (String) -> Boolean,
    onCloseDrawer: () -> Unit
) {
    val strings = LocalStrings.current
    var query by remember { mutableStateOf("") }
    var projectMenuOpen by remember { mutableStateOf(false) }
    var showAll by remember { mutableStateOf(false) }

    // Filter sessions based on query and project filter
    val filteredSessions = remember(state.sessions, state.cwd, query, showAll) {
        state.sessions.filter { session ->
            val matchesQuery = query.isBlank() ||
                session.title.contains(query, ignoreCase = true) ||
                session.cwd.contains(query, ignoreCase = true)
            val matchesProject = showAll || session.cwd.equals(state.cwd, ignoreCase = true)
            matchesQuery && matchesProject
        }
    }

    val currentProjectName = remember(state.projects, state.cwd) {
        state.projects.firstOrNull { it.path.equals(state.cwd, ignoreCase = true) }?.name
            ?: fileName(state.cwd).ifBlank { strings.project }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(NimaySidebar)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ------------------------------------------------------------- Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // New chat button
            Surface(
                color = NimayInput,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .border(1.dp, NimayDivider, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onNewChat(state.cwd)
                        onCloseDrawer()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        OmpIcons.NewChat,
                        contentDescription = null,
                        tint = NimayTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        strings.newChat,
                        style = MaterialTheme.typography.labelLarge,
                        color = NimayTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Search input
            Surface(
                color = NimayInput,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .border(1.dp, NimayDivider, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        OmpIcons.Search,
                        contentDescription = null,
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                strings.sessionSearchHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NimayTextSecondary,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = NimayTextPrimary),
                            cursorBrush = SolidColor(NimayAccent),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = NimayTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Project / CWD picker pill
            Box {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (state.projects.isNotEmpty()) projectMenuOpen = true
                        }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            OmpIcons.Folder,
                            contentDescription = null,
                            tint = NimayAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                currentProjectName,
                                style = MaterialTheme.typography.labelMedium,
                                color = NimayTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (state.cwd.isNotBlank()) {
                                Text(
                                    state.cwd,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NimayTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (state.projects.size > 1) {
                            Icon(
                                OmpIcons.ChevronDown,
                                contentDescription = null,
                                tint = NimayTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = projectMenuOpen,
                    onDismissRequest = { projectMenuOpen = false }
                ) {
                    state.projects.forEach { project ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(project.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        project.path,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NimayTextSecondary
                                    )
                                }
                            },
                            onClick = {
                                projectMenuOpen = false
                                onSelectProject(project.path)
                            }
                        )
                    }
                }
            }

            // Project filter toggle: This project | All conversations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val allLabel = if (showAll) strings.allConversations else strings.thisProject
                Surface(
                    color = if (showAll) NimayAccent.copy(alpha = 0.2f) else NimayInput,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .border(
                            1.dp,
                            if (showAll) NimayAccent else NimayDivider,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showAll = !showAll }
                ) {
                    Text(
                        text = allLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (showAll) NimayAccent else NimayTextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp).align(Alignment.CenterVertically)
                    )
                }
            }
        }

        HorizontalDivider(color = NimayDivider, thickness = 1.dp)

        // ------------------------------------------------------------- Conversation list
        Box(modifier = Modifier.weight(1f)) {
            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (query.isNotBlank()) strings.noResults else strings.noChats,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NimayTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredSessions, key = { it.file }) { session ->
                        val isActive = session.file == currentSessionFile
                        var itemMenuOpen by remember { mutableStateOf(false) }
                        val isFav = isFavorite(session.file)

                        Surface(
                            color = if (isActive) NimayInput else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onOpenSession(session)
                                    onCloseDrawer()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Favorite star
                                IconButton(
                                    onClick = {
                                        onToggleFavorite(session.file)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) LocalDrawerIcons.Star else LocalDrawerIcons.StarBorder,
                                        contentDescription = if (isFav) strings.unpin else strings.pin,
                                        tint = if (isFav) NimayAccent else NimayTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = strings.displayTitle(session.title),
                                    style = if (isActive) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    else MaterialTheme.typography.bodyMedium,
                                    color = if (isActive) NimayTextPrimary else NimayTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                // Time for inactive, menu for active
                                if (isActive) {
                                    Box {
                                        IconButton(
                                            onClick = { itemMenuOpen = true },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                OmpIcons.MoreVert,
                                                contentDescription = null,
                                                tint = NimayTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = itemMenuOpen,
                                            onDismissRequest = { itemMenuOpen = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(strings.rename) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                },
                                                onClick = {
                                                    itemMenuOpen = false
                                                    onRename(session)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (isFav) strings.unpin else strings.pin) },
                                                leadingIcon = {
                                                    Icon(
                                                        if (isFav) LocalDrawerIcons.StarBorder else LocalDrawerIcons.Star,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                onClick = {
                                                    itemMenuOpen = false
                                                    onToggleFavorite(session.file)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(strings.archive) },
                                                leadingIcon = {
                                                    Icon(OmpIcons.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                                                },
                                                onClick = {
                                                    itemMenuOpen = false
                                                    onArchive(session, true)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(strings.delete, color = Color(0xFFEF5350)) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                                },
                                                onClick = {
                                                    itemMenuOpen = false
                                                    onDelete(session)
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    val time = relativeTime(session.updatedAt, strings.lang)
                                    if (time.isNotBlank()) {
                                        Text(
                                            time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NimayTextSecondary.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = NimayDivider, thickness = 1.dp)

        // ------------------------------------------------------------- Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Model selector button
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onOpenModelPicker()
                    }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        OmpIcons.Sparkle,
                        contentDescription = null,
                        tint = NimayAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        shortModel(state.chat.model?.name ?: "").ifBlank { strings.model },
                        style = MaterialTheme.typography.labelMedium,
                        color = NimayTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        OmpIcons.ChevronDown,
                        contentDescription = null,
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Connection status and Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (!state.connected) Modifier.clickable { onReconnect() } else Modifier
                        )
                ) {
                    StatusDot(online = state.connected, size = 7.dp)
                    Text(
                        if (state.connected) strings.connected else strings.disconnected,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.connected) NimayTextSecondary else Color(0xFFEF5350)
                    )
                    if (!state.connected) {
                        Text(
                            strings.reconnect,
                            style = MaterialTheme.typography.labelSmall,
                            color = NimayAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(onClick = onOpenFiles, modifier = Modifier.size(36.dp)) {
                    Icon(
                        OmpIcons.Folder,
                        contentDescription = strings.fileManagerTitle,
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onOpenPcs, modifier = Modifier.size(36.dp)) {
                    Icon(
                        OmpIcons.Computer,
                        contentDescription = "PC-er",
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onOpenTasks, modifier = Modifier.size(36.dp)) {
                    Icon(
                        OmpIcons.Checklist,
                        contentDescription = "Oppgaver",
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onOpenTerminal, modifier = Modifier.size(36.dp)) {
                    Icon(
                        OmpIcons.Terminal,
                        contentDescription = "Terminal",
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onOpenGit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        OmpIcons.Git,
                        contentDescription = "Git",
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = strings.settings,
                        tint = NimayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/** Star border icon (inline since not in OmpIcons). */
/** Star icons (inline since not in OmpIcons). */
private object LocalDrawerIcons {
    private fun icon(name: String, path: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(path),
        fill = SolidColor(Color.Black)
    ).build()

    val Star = icon("Star", "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z")
    val StarBorder = icon("StarBorder", "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z")
}
