package com.app.binged.data.auth

import com.app.binged.core.utils.Result
import com.app.binged.data.api.TmdbService
import retrofit2.HttpException
import javax.inject.Inject

/** TMDB returned 401, so the supplied credential is wrong or revoked. */
class InvalidCredentialException : Exception("TMDB rejected the credential")

/**
 * Probes TMDB with whatever credential is currently stored in [TmdbCredentials].
 *
 * Verification lives here rather than in the store to keep the dependency graph
 * acyclic: this depends on [TmdbService], which depends on the OkHttp client,
 * which depends on [TmdbCredentials].
 */
class TmdbCredentialVerifier @Inject constructor(
    private val tmdbService: TmdbService
) {
    suspend fun verify(): Result<Unit> {
        return try {
            tmdbService.getConfiguration()
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(if (e.code() == 401) InvalidCredentialException() else e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
