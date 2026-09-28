package com.app.binged.feature.shows.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.binged.domain.model.Episode

@Composable
fun EpisodeItem(
    episode: Episode,
    isSeen: Boolean,
    isLogged: Boolean,
    onToggleSeen: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isChecked = isSeen || isLogged
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(
                text = episode.title,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            // Logged episodes derive their checkmark from the diary entry, so
            // the toggle is disabled: seen state follows the log, not the tap.
            Icon(
                imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = when {
                    isLogged -> "Logged"
                    isChecked -> "Seen"
                    else -> "Not seen"
                },
                tint = if (isChecked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isLogged, onClick = onToggleSeen)
            )
        }
    }
}
