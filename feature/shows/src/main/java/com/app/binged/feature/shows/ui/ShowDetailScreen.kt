package com.app.binged.feature.shows.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.binged.core.utils.Result
import com.app.binged.feature.shows.viewmodel.ShowDetailViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun ShowDetailScreen(
    showId: Int,
    onLogEpisodeClick: (Int, String) -> Unit,
    onBack: () -> Unit,
    viewModel: ShowDetailViewModel = koinViewModel()
) {
    LaunchedEffect(showId) {
        viewModel.loadShowDetails(showId)
    }

    val showState by viewModel.showDetails.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val isTracked by viewModel.isTracked.collectAsState()
    val isWatching by viewModel.isWatching.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    var showName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(showState) {
        showName = when (showState) {
            is Result.Success -> (showState as Result.Success).data.name
            else -> ""
        }
    }

    val headerHeight = 200.dp
    val appBarHeight = 56.dp
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val imageHeight = headerHeight + appBarHeight
    val parallaxFactor = 0.3f
    val titleVisibilityThreshold = headerHeight.value * 0.7f

    val showTitleInAppBar = scrollState.value > titleVisibilityThreshold

    val parallaxOffset = with(LocalDensity.current) {
        (scrollState.value * parallaxFactor).toDp()
    }

    val appBarAlpha = (scrollState.value.coerceAtMost(200) / 200f).coerceIn(0f, 0.95f)

    Box(modifier = Modifier.fillMaxSize()) {
        when (showState) {
            is Result.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Result.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error loading show details")
                }
            }

            is Result.Success -> {
                val show = (showState as Result.Success).data

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight + statusBarHeight)
                            .offset(y = -parallaxOffset)
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/original${show.backdropPath}",
                            contentDescription = "${show.name} backdrop",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(modifier = Modifier.height(headerHeight + statusBarHeight))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 16.dp)
                        ) {
                            if (!showTitleInAppBar) {
                                Text(
                                    text = show.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = show.firstAirDate.take(4),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                val seasonLabel = if (show.seasonCount == 1) "season" else "seasons"
                                Text(
                                    text = " • ${show.seasonCount} $seasonLabel",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val rating = String.format(Locale.getDefault(), "%.1f", show.rating)
                                    Text(
                                        text = rating,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(modifier = Modifier.padding(start = 8.dp)) {
                                        repeat(5) { index ->
                                            val isFilled = index < (show.rating / 2).toInt()
                                            Icon(
                                                imageVector = if (isFilled) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                contentDescription = null,
                                                tint = if (isFilled) Color(0xFF4CAF50) else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (show.tagline.isNotEmpty()) {
                                Text(
                                    text = show.tagline,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }

                            ExpandableText(text = show.overview)

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

                                    seasonEpisodes.distinctBy { it.episodeId }.forEach { episode ->
                                        EpisodeItem(
                                            episode = episode,
                                            onLongClick = { viewModel.deleteEpisode(episode) }
                                        )
                                    }
                                }
                            } else if (isTracked) {
                                Text(
                                    text = "You haven't logged any episodes yet.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(appBarHeight + statusBarHeight)
                            .background(
                                if (scrollState.value < 50)
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                else
                                    SolidColor(MaterialTheme.colorScheme.surface.copy(alpha = appBarAlpha))
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = statusBarHeight),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Go back",
                                    tint = if (scrollState.value < 50) Color.White else LocalContentColor.current
                                )
                            }

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Crossfade(
                                    targetState = showTitleInAppBar,
                                    label = "TitleCrossfade"
                                ) { showTitle ->
                                    if (showTitle) {
                                        Text(
                                            text = show.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = if (scrollState.value < 50) Color.White else LocalContentColor.current,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

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
                                    imageVector = if (isTracked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isTracked) "Untrack show" else "Track show",
                                    tint = if (scrollState.value < 50) Color.White else LocalContentColor.current
                                )
                            }

                            if (isTracked) {
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            tint = if (scrollState.value < 50) Color.White else LocalContentColor.current
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(if (isWatching) "Remove from currently watching" else "Mark as watching") },
                                            onClick = { viewModel.updateWatchingStatus(isWatching.not()) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isWatching) Icons.Default.RemoveRedEye else Icons.Outlined.RemoveRedEye,
                                                    contentDescription = if (isWatching) "Unwatch show" else "Watch show"
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text(if (isFavorite) "Remove from favorites" else "Mark as favorite") },
                                            onClick = { viewModel.updateFavoriteStatus(isFavorite.not()) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = if (isFavorite) "Unfavorite show" else "Favorite show"
                                                )
                                            }
                                        )
                                    }
                                }

                            }

                        }
                    }

                    if (isTracked) {
                        FloatingActionButton(
                            onClick = { onLogEpisodeClick(showId, showName) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .navigationBarsPadding()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Log episode"
                            )
                        }
                    }
                }
            }
        }
    }
}
