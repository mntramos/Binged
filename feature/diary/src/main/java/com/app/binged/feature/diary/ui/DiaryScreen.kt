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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.binged.core.utils.UiEvent
import com.app.binged.feature.diary.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    onEpisodeClick: (showId: Int, season: Int, episode: Int, showName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val episodes by viewModel.episodes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect("snackbar") {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> launch { snackbarHostState.showSnackbar(event.message) }
                is UiEvent.ShowSnackbarWithAction -> launch {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastAction()
                    }
                }
            }
        }
    }

    val groupedEntries = episodes.groupBy { episode ->
        val date = episode.watchedDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        date.year to date.month.name
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            onDelete = { viewModel.deleteEpisode(it) }
                        )
                    }
                }
            }
        }
    }
}
