package com.app.binged.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authResult = MutableSharedFlow<kotlin.Result<Unit>>()
    val authResult: SharedFlow<kotlin.Result<Unit>> = _authResult.asSharedFlow()

    private val _verificationEmailSent = Channel<Unit>(Channel.BUFFERED)
    val verificationEmailSent: Flow<Unit> = _verificationEmailSent.receiveAsFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, password)) {
                is Result.Success -> _authResult.emit(kotlin.Result.success(Unit))
                is Result.Error -> _authResult.emit(kotlin.Result.failure(result.exception))
                is Result.Loading -> {}
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.signUpWithEmail(email, password)) {
                is Result.Success -> _verificationEmailSent.send(Unit)
                is Result.Error -> _authResult.emit(kotlin.Result.failure(result.exception))
                is Result.Loading -> {}
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> _authResult.emit(kotlin.Result.success(Unit))
                is Result.Error -> _authResult.emit(kotlin.Result.failure(result.exception))
                is Result.Loading -> {}
            }
        }
    }
}
