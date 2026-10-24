package com.app.binged.feature.settings.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Lets the user supply the TMDB credential that the app runs on.
 *
 * Used both as the mandatory gate before the main navigation (when [onBack] is
 * null) and as a re-entry point from Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(
    onBack: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    viewModel: ApiKeyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val configured by viewModel.configured.collectAsState()
    val context = LocalContext.current
    var input by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onBack?.invoke() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TMDB API Key") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Binged looks up shows and episodes through TMDB, which requires an " +
                        "API key. Each copy of the app runs on the key of whoever installed it.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "1. Sign in to TMDB and open Settings, then API.\n" +
                        "2. Request a key if you do not already have one.\n" +
                        "3. Copy either the API Read Access Token or the API Key and paste " +
                        "it below.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.themoviedb.org/settings/api")
                        )
                    )
                }
            ) {
                Text("Open TMDB API settings")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("API key") },
                singleLine = false,
                maxLines = 3,
                enabled = uiState !is ApiKeyUiState.Validating,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (configured) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "A key is already saved. Saving a new one replaces it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState is ApiKeyUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as ApiKeyUiState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save(input) },
                enabled = uiState !is ApiKeyUiState.Validating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState is ApiKeyUiState.Validating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text("Verifying")
                } else {
                    Text("Verify and save")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (onSignOut != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
