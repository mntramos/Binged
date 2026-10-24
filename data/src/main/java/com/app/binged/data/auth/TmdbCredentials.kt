package com.app.binged.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device-local storage for the TMDB credential the user supplies.
 *
 * The value is mirrored in memory alongside [SharedPreferences] so that the
 * OkHttp interceptor can read it synchronously on every request without
 * touching disk. This deliberately does not depend on [TmdbService]: the
 * service needs the OkHttp client, which needs this store, so depending on the
 * service here would close a Hilt dependency cycle.
 */
@Singleton
class TmdbCredentials @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _credential =
        MutableStateFlow(prefs.getString(KEY_CREDENTIAL, "").orEmpty())

    val credential: StateFlow<String> = _credential.asStateFlow()

    /** Synchronous snapshot for the interceptor. Never performs disk I/O. */
    fun read(): String = _credential.value

    fun save(value: String) {
        val trimmed = value.trim()
        prefs.edit().putString(KEY_CREDENTIAL, trimmed).apply()
        _credential.value = trimmed
    }

    fun clear() {
        prefs.edit().remove(KEY_CREDENTIAL).apply()
        _credential.value = ""
    }

    private companion object {
        const val PREFS_NAME = "tmdb_credentials"
        const val KEY_CREDENTIAL = "credential"
    }
}
