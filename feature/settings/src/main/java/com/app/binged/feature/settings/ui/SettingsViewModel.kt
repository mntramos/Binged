package com.app.binged.feature.settings.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.data.sync.SyncManager
import com.app.binged.domain.contract.AuthRepository
import com.app.binged.domain.contract.EpisodeRepository
import com.app.binged.domain.contract.ShowRepository
import com.app.binged.domain.model.Episode
import com.app.binged.domain.model.Show
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ExportData(
    val version: Int = 1,
    val exportedAt: String,
    val shows: List<Show>,
    val episodes: List<Episode>
)

sealed interface SettingsUiState {
    data object Idle : SettingsUiState
    data object Loading : SettingsUiState
}

sealed interface SettingsEvent {
    data class ShareJson(val json: String, val filename: String) : SettingsEvent
    data class ShowError(val message: String) : SettingsEvent
    data class ShowSuccess(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val showRepository: ShowRepository,
    private val episodeRepository: EpisodeRepository,
    private val syncManager: SyncManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantSerializer())
        .registerTypeAdapter(Instant::class.java, InstantDeserializer())
        .create()

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    init {
        viewModelScope.launch {
            _userEmail.value = authRepository.getCurrentUserEmail()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            syncManager.stopListening()
            authRepository.signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            syncManager.deleteAll()
            syncManager.stopListening()
            showRepository.deleteAll()
            episodeRepository.deleteAll()
            when (val result = authRepository.deleteAccount()) {
                is Result.Success -> {
                    _uiState.value = SettingsUiState.Idle
                    authRepository.signOut()
                }
                is Result.Error -> {
                    _uiState.value = SettingsUiState.Idle
                    _events.emit(
                        SettingsEvent.ShowError(
                            result.exception.message ?: "Failed to delete account. You've been signed out — please log in again and retry."
                        )
                    )
                    authRepository.signOut()
                }
                is Result.Loading -> {}
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val shows = showRepository.getTrackedShows().first()
                val episodes = episodeRepository.getAllEpisodes().first()
                val now = Instant.now()
                val exportData = ExportData(
                    version = 1,
                    exportedAt = now.toString(),
                    shows = shows,
                    episodes = episodes
                )
                val json = gson.toJson(exportData)
                val filename = "binged-export-${DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault()).format(now)}.json"
                _events.emit(SettingsEvent.ShareJson(json, filename))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError("Export failed: ${e.message}"))
            } finally {
                _uiState.value = SettingsUiState.Idle
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Could not open file")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                reader.close()

                val exportData = gson.fromJson(json, ExportData::class.java)

                for (show in exportData.shows) {
                    showRepository.saveShow(show)
                }
                for (episode in exportData.episodes) {
                    episodeRepository.saveEpisode(episode)
                }

                _events.emit(
                    SettingsEvent.ShowSuccess(
                        "Imported ${exportData.shows.size} shows and ${exportData.episodes.size} episodes"
                    )
                )
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError("Import failed: ${e.message}"))
            } finally {
                _uiState.value = SettingsUiState.Idle
            }
        }
    }

}

private class InstantSerializer : com.google.gson.JsonSerializer<Instant> {
    override fun serialize(src: Instant, typeOfSrc: java.lang.reflect.Type, context: com.google.gson.JsonSerializationContext): com.google.gson.JsonElement {
        return com.google.gson.JsonPrimitive(src.toString())
    }
}

private class InstantDeserializer : com.google.gson.JsonDeserializer<Instant> {
    override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): Instant {
        return Instant.parse(json.asString)
    }
}
