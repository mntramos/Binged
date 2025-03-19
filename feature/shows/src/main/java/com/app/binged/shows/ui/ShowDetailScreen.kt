package com.app.binged.shows.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.binged.core.utils.Result
import com.app.binged.shows.viewmodel.ShowDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: Int,
    onLogEpisodeClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: ShowDetailViewModel = koinViewModel()
) {
    LaunchedEffect(showId) {
        viewModel.loadShowDetails(showId)
    }

    val showState by viewModel.showDetails.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val isTracked by viewModel.isTracked.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (showState) {
                        is Result.Success -> Text((showState as Result.Success).data.name)
                        else -> Text("Show Details")
                    }
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
                    IconButton(
                        onClick = {
                            if (isTracked) {
                                viewModel.untrackShow()
                            } else {
                                viewModel.trackShow()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isTracked) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (isTracked) "Untrack show" else "Track show"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isTracked) {
                FloatingActionButton(onClick = { onLogEpisodeClick(showId) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log episode"
                    )
                }
            }
        }
    ) { paddingValues ->
        when (showState) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Result.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error loading show details")
                }
            }

            is Result.Success -> {
                val show = (showState as Result.Success).data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${show.backdropPath}",
                            contentDescription = "${show.name} poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "First aired: ${show.firstAirDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "Rating: ${show.rating}/10",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = show.overview,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        if (isTracked && episodes.isNotEmpty()) {
                            Text(
                                text = "Your Watched Episodes",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                            )

                            episodes.groupBy { it.seasonNumber }.forEach { (season, seasonEpisodes) ->
                                Text(
                                    text = "Season $season",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )

                                seasonEpisodes.forEach { episode ->
                                    EpisodeItem(
                                        episode = episode,
                                        onClick = { viewModel.deleteEpisode(episode) }
                                    )
                                }
                            }
                        } else if (isTracked) {
                            Text(
                                text = "You haven't logged any episodes yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
