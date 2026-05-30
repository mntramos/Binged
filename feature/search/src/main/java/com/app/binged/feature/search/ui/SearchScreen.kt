package com.app.binged.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.binged.core.utils.Result
import com.app.binged.core.utils.UiEvent
import com.app.binged.domain.model.Show
import com.app.binged.feature.search.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onShowClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val popularShows by viewModel.popularShows.collectAsState()
    val trackedShowIds by viewModel.trackedShowIds.collectAsState()
    val searchInProgress by viewModel.searchInProgress.collectAsState()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    var showToUntrack by remember { mutableStateOf<Show?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500)
            viewModel.search(searchQuery)
        }
    }

    LaunchedEffect("focus") {
        focusRequester.requestFocus()
    }

    LaunchedEffect("snackbar") {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> launch { snackbarHostState.showSnackbar(event.message) }
            }
        }
    }

    showToUntrack?.let { show ->
        AlertDialog(
            onDismissRequest = { showToUntrack = null },
            title = { Text("Remove Show") },
            text = { Text("Remove ${show.name} and all its episodes from your library?") },
            confirmButton = {
                TextButton(onClick = {
                    showToUntrack = null
                    viewModel.untrackShow(show)
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToUntrack = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onClearSearch = {
                    searchQuery = ""
                    viewModel.clearSearchResults()
                },
                onBack = onBack,
                focusRequester = focusRequester
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                searchQuery.isBlank() -> {
                    when (val popular = popularShows) {
                        is Result.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is Result.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Enter a show name to search", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is Result.Success -> {
                            val shows = popular.data
                            if (shows.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Enter a show name to search", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = listState,
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "Popular Shows",
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    items(shows) { show ->
                                        SearchResultItem(
                                            show = show,
                                            isAlreadyTracked = show.id in trackedShowIds,
                                            onClick = { onShowClick(show.id) },
                                            onTrackClick = { viewModel.trackShow(show) },
                                            onUntrackClick = { showToUntrack = show }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                searchInProgress -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                searchResults is Result.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error searching for shows")
                    }
                }

                searchResults is Result.Success -> {
                    val shows = (searchResults as Result.Success<List<Show>>).data

                    if (shows.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No shows found matching '$searchQuery'")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(shows) { show ->
                                SearchResultItem(
                                    show = show,
                                    isAlreadyTracked = show.id in trackedShowIds,
                                    onClick = { onShowClick(show.id) },
                                    onTrackClick = { viewModel.trackShow(show) },
                                    onUntrackClick = { showToUntrack = show }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
