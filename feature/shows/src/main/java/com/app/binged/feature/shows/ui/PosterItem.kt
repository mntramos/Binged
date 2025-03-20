package com.app.binged.feature.shows.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.binged.domain.model.Show

@Composable
fun PosterItem(
    show: Show,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp) // Width remains constant
            .height(180.dp) // Height follows 2:3 ratio (120 * 1.5 = 180)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${show.posterPath}",
            contentDescription = "${show.name} poster",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
