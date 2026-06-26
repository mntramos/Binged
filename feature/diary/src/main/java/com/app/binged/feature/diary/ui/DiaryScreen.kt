package com.app.binged.feature.diary.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.model.Episode
import com.app.binged.feature.diary.viewmodel.DiaryViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val pullRefreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeletion by remember { mutableStateOf<Episode?>(null) }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(pullRefreshState.isRefreshing) {
            viewModel.refresh()
            delay(500L)
            pullRefreshState.endRefresh()
        }
    }

    LaunchedEffect("snackbar") {
        var snackbarJob: Job? = null
        viewModel.uiEvent.collect { event ->
            snackbarJob?.cancel()
            snackbarJob = launch {
                when (event) {
                    is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                        event.message,
                        duration = SnackbarDuration.Short
                    )
                    is UiEvent.ShowSnackbarWithAction -> {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoLastAction()
                        }
                    }
                }
            }
        }
    }

    val groupedEntries = episodes.groupBy { episode ->
        val date = episode.watchedDate
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullRefreshState.nestedScrollConnection),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No episodes yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Log your first episode from a show's detail page to start building your diary.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                if (pullRefreshState.isRefreshing || pullRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = pullRefreshState
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .nestedScroll(pullRefreshState.nestedScrollConnection)
            ) {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier.fillMaxSize()
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
                if (pullRefreshState.isRefreshing || pullRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = pullRefreshState
                    )
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
