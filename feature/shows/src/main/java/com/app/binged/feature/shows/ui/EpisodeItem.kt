package com.app.binged.feature.shows.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.binged.domain.model.Episode
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeItem(
    episode: Episode,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = episode.episodeNumber.toString(),
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    )
                    .wrapContentSize(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(text = episode.title, fontWeight = FontWeight.Bold, maxLines = 2)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EpisodeItemPreview() {
    val episode = Episode(
        episodeId = 123456,
        showId = 3946240,
        showName = "Frieren: Beyond Journey's End",
        seasonNumber = 1,
        episodeNumber = 1,
        title = "The Journey's End",
        watchedDate = Date(),
        stillPath = "/zVobco2BeS830uaUYOhtMnod9WX.jpg"
    )
    EpisodeItem(episode, {})
}
