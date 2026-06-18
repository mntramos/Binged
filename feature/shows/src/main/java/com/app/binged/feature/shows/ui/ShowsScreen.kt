package com.app.binged.feature.shows.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.binged.domain.model.Show
import com.app.binged.feature.shows.viewmodel.ShowsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowsScreen(
    onShowClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onDiaryClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: ShowsViewModel = hiltViewModel()
) {
    val shows by viewModel.shows.collectAsState()
    val filteredShows by viewModel.filteredShows.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val showSearch by viewModel.showSearch.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val displayShows = if (showSearch) filteredShows else shows
    val watching = displayShows.filter { it.isWatching }
    val favorites = displayShows.filter { it.isFavorite }
    var showFavorites by remember { mutableStateOf(true) }
    var showWatching by remember { mutableStateOf(true) }
    var showAllShows by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    IconButton(onClick = onDiaryClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Open Diary"
                        )
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (showSearch) "Close Search" else "Search") },
                            leadingIcon = { Icon(if (showSearch) Icons.Filled.Close else Icons.Filled.Search, null) },
                            onClick = { showMenu = false; viewModel.toggleSearch() }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isGridView) "List View" else "Grid View") },
                            leadingIcon = {
                                Icon(
                                    if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                                    null
                                )
                            },
                            onClick = { showMenu = false; viewModel.toggleView() }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = { showMenu = false; onSettingsClick() }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Search shows"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (isGridView && displayShows.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, top = 0.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = gridColumns.toFloat(),
                        onValueChange = { viewModel.setGridColumns(it.roundToInt()) },
                        valueRange = 2f..4f,
                        steps = 1,
                        modifier = Modifier
                            .widthIn(max = 120.dp)
                            .padding(horizontal = 8.dp)
                    )
                    Text("4", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Filter by name...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }

            if (displayShows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showSearch) "No shows match '$searchQuery'" else "You're not tracking any shows yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (favorites.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader("Favorites", showFavorites, favorites.size) { showFavorites = !showFavorites }
                        }
                        if (showFavorites) {
                            items(favorites) { show ->
                                PosterItem(show) { onShowClick(show.id) }
                            }
                        }
                    }

                    if (watching.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader("Currently Watching", showWatching, watching.size) { showWatching = !showWatching }
                        }
                        if (showWatching) {
                            items(watching) { show ->
                                PosterItem(show) { onShowClick(show.id) }
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader("All Shows", showAllShows, displayShows.size) { showAllShows = !showAllShows }
                    }
                    if (showAllShows) {
                        items(displayShows) { show ->
                            PosterItem(show) { onShowClick(show.id) }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (favorites.isNotEmpty()) {
                        item {
                            SectionHeader("Favorites", showFavorites, favorites.size) { showFavorites = !showFavorites }
                        }
                        if (showFavorites) {
                            items(favorites) { show ->
                                ShowItem(show) { onShowClick(show.id) }
                            }
                        }
                    }

                    if (watching.isNotEmpty()) {
                        item {
                            SectionHeader("Currently Watching", showWatching, watching.size) { showWatching = !showWatching }
                        }
                        if (showWatching) {
                            items(watching) { show ->
                                ShowItem(show) { onShowClick(show.id) }
                            }
                        }
                    }

                    item {
                        SectionHeader("All Shows", showAllShows, displayShows.size) { showAllShows = !showAllShows }
                    }
                    if (showAllShows) {
                        items(displayShows) { show ->
                            ShowItem(show) { onShowClick(show.id) }
                        }
                    }
                }
            }
        }
    }
}
