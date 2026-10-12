package com.example.data.repository

import android.content.Context
import com.example.data.model.UserSession
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserSession) : AuthResult()
    data class Error(val message: String, val isFirebaseConfigIssue: Boolean = false) : AuthResult()
}

class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // Tracks demo/guest session state in memory if Firebase is offline
    private var localGuestSession: UserSession? = null

    fun getAuthStateFlow(): Flow<UserSession?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(localGuestSession)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { fa ->
            val currentUser = fa.currentUser
            if (currentUser != null) {
                trySend(
                    UserSession(
                        uid = currentUser.uid,
                        email = currentUser.email,
                        displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@"),
                        isAnonymous = currentUser.isAnonymous,
                        isDemo = currentUser.isAnonymous
                    )
                )
            } else {
                trySend(localGuestSession)
            }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun getCurrentUser(): UserSession? {
        val fbUser = auth?.currentUser
        if (fbUser != null) {
            return UserSession(
                uid = fbUser.uid,
                email = fbUser.email,
                displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@"),
                isAnonymous = fbUser.isAnonymous,
                isDemo = fbUser.isAnonymous
            )
        }
        return localGuestSession
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResult {
        val fbAuth = auth ?: return AuthResult.Error(
            "Firebase is not yet initialized for this Android app. Please register package in Firebase Console.",
            isFirebaseConfigIssue = true
        )

        return try {
            val result = fbAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw IllegalStateException("User not returned after sign in")
            AuthResult.Success(
                UserSession(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: user.email?.substringBefore("@"),
                    isAnonymous = false,
                    isDemo = false
                )
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Invalid email or password.")
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Sign in failed"
            val isConfig = msg.contains("api-key-not-valid", ignoreCase = true) ||
                    msg.contains("no such host", ignoreCase = true) ||
                    msg.contains("app not authorized", ignoreCase = true)
            AuthResult.Error(
                if (isConfig) "Firebase Config Error: The Android package name must be registered in Firebase Console (timemanagement-v1) with a valid google-services.json."
                else msg,
                isFirebaseConfigIssue = isConfig
            )
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): AuthResult {
        val fbAuth = auth ?: return AuthResult.Error(
            "Firebase is not initialized for Android. Use Demo mode or configure Firebase.",
            isFirebaseConfigIssue = true
        )

        return try {
            val result = fbAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw IllegalStateException("User creation failed")
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            AuthResult.Success(
                UserSession(
                    uid = user.uid,
                    email = user.email,
                    displayName = displayName.ifBlank { user.email?.substringBefore("@") },
                    isAnonymous = false,
                    isDemo = false
                )
            )
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("An account already exists with this email address.")
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Registration failed"
            val isConfig = msg.contains("api-key-not-valid", ignoreCase = true)
            AuthResult.Error(
                if (isConfig) "Firebase API Key Invalid: Register native package com.aistudio.timeflow.tzkmvw in Firebase Console."
                else msg,
                isFirebaseConfigIssue = isConfig
            )
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        val fbAuth = auth ?: return Result.failure(Exception("Firebase Auth unavailable"))
        return try {
            fbAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithCredential(credential: com.google.firebase.auth.AuthCredential): AuthResult {
        val fbAuth = auth ?: return AuthResult.Error("Firebase Auth unavailable", isFirebaseConfigIssue = true)
        return try {
            val result = fbAuth.signInWithCredential(credential).await()
            val user = result.user ?: throw IllegalStateException("No user returned")
            AuthResult.Success(
                UserSession(
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: "Google User",
                    isAnonymous = false,
                    isDemo = false
                )
            )
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    suspend fun signInAsDemo(): AuthResult {
        val fbAuth = auth
        if (fbAuth != null) {
            try {
                val result = fbAuth.signInAnonymously().await()
                val user = result.user
                if (user != null) {
                    return AuthResult.Success(
                        UserSession(
                            uid = user.uid,
                            email = null,
                            displayName = "Demo User",
                            isAnonymous = true,
                            isDemo = true
                        )
                    )
                }
            } catch (e: Exception) {
                // If Anonymous Auth is not enabled in Firebase console, fall back to safe local guest demo
            }
        }

        // Guaranteed local guest demo mode so user is never blocked
        val guestUid = "demo_guest_" + UUID.randomUUID().toString().take(8)
        val guest = UserSession(
            uid = guestUid,
            email = null,
            displayName = "Demo Guest",
            isAnonymous = true,
            isDemo = true
        )
        localGuestSession = guest
        return AuthResult.Success(guest)
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
        localGuestSession = null
    }
}
