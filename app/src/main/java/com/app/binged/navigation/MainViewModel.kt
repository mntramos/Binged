package com.app.binged.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.NetworkMonitor
import com.app.binged.data.sync.SyncManager
import com.app.binged.domain.contract.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val authState: StateFlow<Boolean> = authRepository.authState()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUserId: String
        get() = authRepository.getCurrentUserIdSync() ?: "default"

    init {
        viewModelScope.launch {
            authState.first { it }
            syncManager.startListening()
            syncManager.pullAll()
        }
    }
}
