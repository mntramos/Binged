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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.binged.feature.diary.viewmodel.DiaryViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    onBack: () -> Unit,
    viewModel: DiaryViewModel = koinViewModel()
) {
    val episodes by viewModel.episodes.collectAsState()

    val groupedEntries = episodes
        .sortedByDescending { it.watchedDate }
        .groupBy { episode ->
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
                },
            )
        },
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
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
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
                groupedEntries.forEach { (year, month), episodes ->
                    stickyHeader {
                        MonthHeader(year, month)
                    }
                    items(episodes) { episode ->
                        DiaryItem(episode)
                    }
                }
            }
        }
    }
}
