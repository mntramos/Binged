package com.app.binged.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.data.auth.InvalidCredentialException
import com.app.binged.data.auth.TmdbCredentialVerifier
import com.app.binged.data.auth.TmdbCredentials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

sealed interface ApiKeyUiState {
    data object Idle : ApiKeyUiState
    data object Validating : ApiKeyUiState
    data class Error(val message: String) : ApiKeyUiState
}

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val credentials: TmdbCredentials,
    private val verifier: TmdbCredentialVerifier
) : ViewModel() {

    private val _uiState = MutableStateFlow<ApiKeyUiState>(ApiKeyUiState.Idle)
    val uiState: StateFlow<ApiKeyUiState> = _uiState.asStateFlow()

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    val configured: StateFlow<Boolean> = credentials.credential
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, credentials.read().isNotBlank())

    /**
     * The candidate is stored before it is verified because the interceptor reads
     * the credential from [TmdbCredentials]. If verification fails the previous
     * credential is put back: a previously working key must not be destroyed by a
     * typo, and clearing it would trip the gate and eject the user from Settings
     * before they could read the error.
     */
    fun save(input: String) {
        if (_uiState.value is ApiKeyUiState.Validating) return

        val candidate = input.trim()
        if (candidate.isBlank()) {
            _uiState.value = ApiKeyUiState.Error("Enter your TMDB API key")
            return
        }

        _uiState.value = ApiKeyUiState.Validating
        val previous = credentials.read()
        credentials.save(candidate)

        viewModelScope.launch {
            when (val result = verifier.verify()) {
                is Result.Success -> {
                    _uiState.value = ApiKeyUiState.Idle
                    _saved.emit(Unit)
                }
                is Result.Error -> {
                    if (previous.isBlank()) {
                        credentials.clear()
                    } else {
                        credentials.save(previous)
                    }
                    _uiState.value = ApiKeyUiState.Error(messageFor(result.exception))
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun messageFor(exception: Exception): String = when (exception) {
        is InvalidCredentialException ->
            "TMDB rejected this key. Paste the API Read Access Token or the API Key exactly as shown."
        is IOException ->
            "Could not reach TMDB. Check your connection and try again."
        else ->
            "Could not verify this key. Try again."
    }
}
