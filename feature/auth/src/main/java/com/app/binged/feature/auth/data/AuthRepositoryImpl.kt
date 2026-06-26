package com.app.binged.feature.auth.data

import com.app.binged.core.utils.Result
import com.app.binged.domain.contract.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class EmailNotVerifiedException : Exception("Please verify your email before logging in")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun authState(): Flow<Boolean> = callbackFlow {
        trySend(auth.currentUser?.isEmailVerified == true)
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user != null && user.isEmailVerified)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            if (user != null && !user.isEmailVerified) {
                auth.signOut()
                Result.Error(EmailNotVerifiedException())
            } else {
                Result.Success(Unit)
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.Error(Exception("No account found with this email address"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Error(Exception("Incorrect password. Please try again"))
        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                "INVALID_LOGIN_CREDENTIALS" in msg -> Result.Error(Exception("Incorrect email or password. Please try again"))
                "USER_DISABLED" in msg -> Result.Error(Exception("This account has been disabled. Contact support."))
                "TOO_MANY_ATTEMPTS_TRY_LATER" in msg -> Result.Error(Exception("Too many login attempts. Please try again later."))
                else -> Result.Error(Exception("Sign in failed. Please try again."))
            }
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            auth.currentUser?.sendEmailVerification()?.await()
            auth.signOut()
            Result.Success(Unit)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.Error(Exception("Password is too weak. Use at least 6 characters."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Error(Exception("Invalid email format"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.Error(Exception("An account already exists with this email"))
        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                "INVALID_EMAIL" in msg -> Result.Error(Exception("Invalid email format"))
                "WEAK_PASSWORD" in msg -> Result.Error(Exception("Password is too weak. Use at least 6 characters."))
                "EMAIL_EXISTS" in msg -> Result.Error(Exception("An account already exists with this email"))
                else -> Result.Error(Exception("Registration failed. Try again."))
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            auth.currentUser?.reload()?.await()
            Result.Success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Error(Exception("Invalid Google sign-in. Please try again."))
        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                "NETWORK_ERROR" in msg || e is java.net.UnknownHostException ->
                    Result.Error(Exception("Network error. Please check your internet connection."))
                else -> Result.Error(Exception("Google sign-in failed. Please try again."))
            }
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserIdSync(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            auth.currentUser?.reload()?.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
