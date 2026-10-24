package com.app.binged.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.NetworkMonitor
import com.app.binged.data.auth.TmdbCredentials
import com.app.binged.data.sync.SyncManager
import com.app.binged.domain.contract.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor,
    credentials: TmdbCredentials
) : ViewModel() {

    val authState: StateFlow<Boolean> = authRepository.authState()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val hasApiKey: StateFlow<Boolean> = credentials.credential
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, credentials.read().isNotBlank())

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Escape hatch for the API key gate, which has no other route to Settings. */
    fun signOut() {
        viewModelScope.launch {
            syncManager.stopListening()
            authRepository.signOut()
        }
    }

    val currentUserId: String
        get() = authRepository.getCurrentUserIdSync() ?: "default"

    init {
        viewModelScope.launch {
            authRepository.authState().collect { authenticated ->
                if (authenticated) {
                    syncManager.startListening()
                    syncManager.pullAll()
                } else {
                    syncManager.stopListening()
                }
            }
        }
    }
}
