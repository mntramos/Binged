package com.app.binged.feature.diary.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.binged.domain.model.Episode
import com.app.binged.feature.diary.viewmodel.DiaryViewModel
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    onEpisodeClick: (showId: Int, season: Int, episode: Int, showName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val episodes by viewModel.episodes.collectAsState()
    var pendingDeletion by remember { mutableStateOf<Episode?>(null) }

    val groupedEntries = episodes.groupBy { episode ->
        val date = episode.watchedDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        date.year to date.month.name
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Diary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (groupedEntries.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Start watching!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                groupedEntries.forEach { (year, month), entries ->
                    stickyHeader {
                        MonthHeader(year, month)
                    }
                    items(entries, key = { it.id }) { episode ->
                        DiaryItem(
                            episode = episode,
                            onClick = { showId, season, episodeNumber, showName ->
                                onEpisodeClick(showId, season, episodeNumber, showName)
                            },
                            onDeleteRequest = { pendingDeletion = it }
                        )
                    }
                }
            }
        }
    }

    pendingDeletion?.let { episode ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("Delete entry?") },
            text = { Text("Are you sure you want to delete \"${episode.title}\" from your diary?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEpisode(episode)
                    pendingDeletion = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
