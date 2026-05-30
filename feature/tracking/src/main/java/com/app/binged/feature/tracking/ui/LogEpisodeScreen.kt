package com.app.binged.feature.tracking.ui

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.binged.core.utils.Result
import com.app.binged.feature.tracking.viewmodel.LogEpisodeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEpisodeScreen(
    showId: Int,
    showName: String,
    onNavigateBack: () -> Unit,
    viewModel: LogEpisodeViewModel = hiltViewModel()
) {
    var season by remember { mutableStateOf("") }
    var episode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }

    val episodeState by viewModel.episodeDetails.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LaunchedEffect(showId) {
        viewModel.setShowId(showId)
    }

    val context = LocalContext.current
    LaunchedEffect(episodeState) {
        when (episodeState) {
            is Result.Success -> onNavigateBack()
            is Result.Loading -> {}
            else -> Toast.makeText(context, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(season, episode) {
        val seasonNum = season.toIntOrNull() ?: 0
        val episodeNum = episode.toIntOrNull() ?: 0
        viewModel.verifyEpisode(seasonNum, episodeNum)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Episode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showName.isNotBlank()) {
                Text(
                    text = "Show: $showName",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Season") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = episode,
                    onValueChange = { episode = it },
                    label = { Text("Episode") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            when (val verification = verificationState) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Result.Success -> {
                    val ep = verification.data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ep.stillPath != null) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${ep.stillPath}",
                                contentDescription = ep.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(
                                text = ep.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ep.getIdentifier(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is Result.Error -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Episode not found. Check the season and episode numbers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                null -> {}
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date Watched: ${dateFormat.format(selectedDate)}")
            }

            if (showDatePicker) {
                CustomDatePicker(
                    initialDate = selectedDate,
                    onDateSelected = { date ->
                        selectedDate = date
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val seasonNum = season.toIntOrNull() ?: 0
                    val episodeNum = episode.toIntOrNull() ?: 0
                    if (seasonNum > 0 && episodeNum > 0) {
                        viewModel.saveEpisode(
                            showName = showName,
                            seasonNumber = seasonNum,
                            episodeNumber = episodeNum,
                            watchedDate = selectedDate,
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = season.isNotBlank() && episode.isNotBlank() &&
                        (season.toIntOrNull() ?: 0) > 0 && (episode.toIntOrNull() ?: 0) > 0 &&
                        verificationState is Result.Success
            ) {
                Text("Save Episode")
            }
        }
    }
}
