package com.app.binged.feature.shows.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.binged.core.utils.Result
import com.app.binged.domain.model.Episode
import com.app.binged.feature.shows.viewmodel.EpisodeDetailEvent
import com.app.binged.feature.shows.viewmodel.EpisodeDetailViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailScreen(
    onBack: () -> Unit,
    onShowClick: (Int) -> Unit = {},
    onLogEpisodeClick: (String) -> Unit = {},
    viewModel: EpisodeDetailViewModel = hiltViewModel()
) {
    val episodeState by viewModel.episodeDetail.collectAsState()
    val showName by viewModel.showName.collectAsState()
    val isLogged by viewModel.isLogged.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    var entryPendingDelete by remember { mutableStateOf<Episode?>(null) }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var noteIsRewatch by remember { mutableStateOf(false) }
    var noteRating by remember { mutableStateOf<Int?>(null) }
    var hadExistingNote by remember { mutableStateOf(false) }
    var historyExpanded by rememberSaveable { mutableStateOf(true) }
    val entryDateFormat =
        DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EpisodeDetailEvent.Deleted -> onBack()
            }
        }
    }

    entryPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Delete Entry") },
            text = {
                Text(
                    "Remove the watch from ${entryDateFormat.format(target.watchedDate)}? " +
                        "Other watches of this episode are kept."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    entryPendingDelete = null
                    viewModel.deleteEntry(target.id)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteNoteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteNoteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteNoteDialog = false
                    viewModel.updateEntry(
                        notes = null,
                        isRewatch = selectedEntry?.isRewatch ?: false,
                        rating = selectedEntry?.rating
                    )
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Edit Entry") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 10
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { noteIsRewatch = !noteIsRewatch }
                    ) {
                        Checkbox(
                            checked = noteIsRewatch,
                            onCheckedChange = { noteIsRewatch = it }
                        )
                        Text(text = "Rewatch", style = MaterialTheme.typography.bodyLarge)
                    }
                    Column {
                        Text(
                            text = "Rating",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StarRatingInput(
                            rating = noteRating,
                            onRatingChange = { noteRating = it }
                        )
                        if (noteRating != null) {
                            TextButton(
                                onClick = { noteRating = null },
                                modifier = Modifier.padding(top = 0.dp)
                            ) {
                                Text("Clear rating")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showNoteDialog = false
                    viewModel.updateEntry(
                        notes = noteText.takeIf { it.isNotBlank() },
                        isRewatch = noteIsRewatch,
                        rating = noteRating
                    )
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                if (hadExistingNote) {
                    TextButton(onClick = {
                        showNoteDialog = false
                        showDeleteNoteDialog = true
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = { showNoteDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (episodeState is Result.Success) (episodeState as Result.Success).data.title else "Episode Details"
                    Text(text = title, maxLines = Int.MAX_VALUE)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (!isLogged) {
                                DropdownMenuItem(
                                    text = { Text("Log Episode") },
                                    onClick = {
                                        menuExpanded = false
                                        onLogEpisodeClick(showName)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null
                                        )
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        menuExpanded = false
                                        noteText = selectedEntry?.notes.orEmpty()
                                        noteIsRewatch = selectedEntry?.isRewatch ?: false
                                        noteRating = selectedEntry?.rating
                                        hadExistingNote = !selectedEntry?.notes.isNullOrEmpty()
                                        showNoteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Log Again") },
                                    onClick = {
                                        menuExpanded = false
                                        onLogEpisodeClick(showName)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Replay,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menuExpanded = false
                                        entryPendingDelete = selectedEntry
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (episodeState) {
            is Result.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            }

            is Result.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text("Error loading episode details")
                }
            }

            is Result.Success -> {
                val episode = (episodeState as Result.Success).data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (episode.stillPath != null) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/original${episode.stillPath}",
                            contentDescription = episode.title,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = episode.getIdentifier(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (episode.showName.isNotEmpty()) {
                            Text(
                                text = episode.showName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onShowClick(episode.showId) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (episode.airDate.isNotEmpty()) {
                                Text(
                                    text = episode.airDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (episode.runtime > 0) {
                                if (episode.airDate.isNotEmpty()) {
                                    Text(
                                        text = " • ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${episode.runtime}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (episode.voteAverage > 0) {
                                if (episode.airDate.isNotEmpty() || episode.runtime > 0) {
                                    Text(
                                        text = " • ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", episode.voteAverage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (episode.overview.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = episode.overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!episode.notes.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = episode.notes.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (entries.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { historyExpanded = !historyExpanded }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (entries.size > 1) {
                                        "Watch History (${entries.size}) · ${entryDateFormat.format(entries.first().watchedDate)}"
                                    } else {
                                        "Watch History · ${entryDateFormat.format(entries.first().watchedDate)}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (historyExpanded) {
                                        Icons.Default.ExpandLess
                                    } else {
                                        Icons.Default.ExpandMore
                                    },
                                    contentDescription = if (historyExpanded) {
                                        "Collapse watch history"
                                    } else {
                                        "Expand watch history"
                                    }
                                )
                            }
                            if (historyExpanded) {
                                entries.forEach { entry ->
                                    WatchHistoryRow(
                                        entry = entry,
                                        isSelected = entry.id == selectedEntry?.id,
                                        dateFormat = entryDateFormat,
                                        onClick = { viewModel.selectEntry(entry.id) },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchHistoryRow(
    entry: Episode,
    isSelected: Boolean,
    dateFormat: DateTimeFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateFormat.format(entry.watchedDate),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            if (!entry.notes.isNullOrBlank()) {
                Text(
                    text = entry.notes.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        contentColor.copy(alpha = 0.75f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (entry.isRewatch || entry.rating != null) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (entry.isRewatch) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "Rewatch",
                        tint = if (isSelected) {
                            contentColor
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(14.dp)
                    )
                }
                entry.rating?.let { rating ->
                    RatingBadge(
                        rating = rating,
                        tint = if (isSelected) {
                            contentColor
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}
