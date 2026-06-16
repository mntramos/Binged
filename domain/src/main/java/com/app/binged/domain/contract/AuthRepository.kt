package com.app.binged.domain.contract

import com.app.binged.core.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun authState(): Flow<Boolean>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
    suspend fun getCurrentUserId(): String?
    suspend fun getCurrentUserEmail(): String?
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun isEmailVerified(): Boolean
    suspend fun reloadUser(): Result<Unit>
}
