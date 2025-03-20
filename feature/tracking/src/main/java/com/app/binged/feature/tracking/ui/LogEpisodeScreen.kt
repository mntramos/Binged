package com.app.binged.feature.tracking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.binged.feature.tracking.viewmodel.LogEpisodeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEpisodeScreen(
    showId: Int,
    onNavigateBack: () -> Unit,
    viewModel: LogEpisodeViewModel = koinViewModel()
) {
    var season by remember { mutableStateOf("") }
    var episode by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LaunchedEffect(showId) {
        viewModel.setShowId(showId)
    }

    val saveSuccess by viewModel.saveSuccess.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
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
            OutlinedTextField(
                value = season,
                onValueChange = { season = it },
                label = { Text("Season") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = episode,
                onValueChange = { episode = it },
                label = { Text("Episode") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Episode Title (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date Watched: ${dateFormat.format(selectedDate)}")
            }

            if (showDatePicker) {
                // TODO: use DatePickerDialog
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
                            seasonNumber = seasonNum,
                            episodeNumber = episodeNum,
                            title = title,
                            watchedDate = selectedDate,
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = season.isNotBlank() && episode.isNotBlank() &&
                        (season.toIntOrNull() ?: 0) > 0 && (episode.toIntOrNull() ?: 0) > 0
            ) {
                Text("Save Episode")
            }
        }
    }
}
