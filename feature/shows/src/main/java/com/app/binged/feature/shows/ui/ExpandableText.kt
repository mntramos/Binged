package com.app.binged.feature.shows.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(
    text: String,
    maxChars: Int = 200,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (expanded || text.length <= maxChars) text else text.take(maxChars) + "..."

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(text = displayText, style = style, color = MaterialTheme.colorScheme.onSurface)
        if (text.length > maxChars) {
            Text(
                text = if (expanded) "See less" else "See more",
                style = style.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp)
            )
        }
    }
}
