package com.app.binged.core.utils

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class ShowSnackbarWithAction(val message: String, val actionLabel: String) : UiEvent()
}
