package com.example.data.auth

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.BuildConfig
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data payload containing validated Google user information extracted from Google ID Token.
 */
data class GoogleAccountResult(
    val idToken: String,
    val email: String,
    val displayName: String,
    val photoUrl: String = ""
)

/**
 * Production-ready Google Sign-In helper for MyCampus.
 *
 * Resolves the Google Web Client ID from BuildConfig or R.string.default_web_client_id.
 * Uses Android Credential Manager with GetGoogleIdOption (filterByAuthorizedAccounts = false)
 * to open the Google account chooser and authenticate with Firebase Authentication.
 */
class GoogleSignInHelper(
    private val context: Context,
    private val authProvider: FirebaseAuth? = null
) {
    private val firebaseAuth: FirebaseAuth by lazy {
        authProvider ?: run {
            val app = com.example.MyCampusApplication.ensureFirebaseInitialized(context)
            if (app != null) FirebaseAuth.getInstance(app) else FirebaseAuth.getInstance()
        }
    }

    /**
     * Resolves the Web Client ID with proper priority:
     * 1. Explicit client ID passed in call (if non-blank and non-placeholder)
     * 2. BuildConfig.GOOGLE_WEB_CLIENT_ID (injected via Secrets plugin from .env)
     * 3. R.string.default_web_client_id (from strings.xml or google-services.json generated resource)
     */
    fun resolveWebClientId(explicitClientId: String? = null): String {
        // 1. Explicit override if provided
        if (!explicitClientId.isNullOrBlank() && !explicitClientId.contains("placeholder", ignoreCase = true)) {
            Log.d(TAG, "Using explicit Web Client ID.")
            return explicitClientId.trim()
        }

        // 2. Read from BuildConfig (injected from .env via Secrets Gradle plugin)
        try {
            val buildConfigId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            if (!buildConfigId.isNullOrBlank() && !buildConfigId.contains("placeholder", ignoreCase = true)) {
                Log.d(TAG, "Using Web Client ID from BuildConfig.GOOGLE_WEB_CLIENT_ID: ${buildConfigId.take(15)}...")
                return buildConfigId.trim()
            }
        } catch (e: Exception) {
            Log.w(TAG, "BuildConfig.GOOGLE_WEB_CLIENT_ID lookup failed: ${e.message}")
        }

        // 3. Read from strings.xml or google-services generated resource
        try {
            val stringResVal = context.getString(R.string.default_web_client_id)
            if (stringResVal.isNotBlank() && !stringResVal.contains("placeholder", ignoreCase = true)) {
                Log.d(TAG, "Using Web Client ID from R.string.default_web_client_id: ${stringResVal.take(15)}...")
                return stringResVal.trim()
            }
        } catch (e: Exception) {
            Log.w(TAG, "R.string.default_web_client_id lookup failed: ${e.message}")
        }

        // 4. Try dynamic identifier lookup as last resource fallback
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val resVal = context.getString(resId)
                if (resVal.isNotBlank() && !resVal.contains("placeholder", ignoreCase = true)) {
                    Log.d(TAG, "Using Web Client ID from dynamic resource identifier: ${resVal.take(15)}...")
                    return resVal.trim()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Dynamic identifier lookup failed: ${e.message}")
        }

        val errMsg = "Google Sign-In configuration error: Web Client ID is missing. Please ensure GOOGLE_WEB_CLIENT_ID is set in .env / Secrets panel or google-services.json is present."
        Log.e(TAG, errMsg)
        throw AuthException(AuthErrorCode.MISSING_WEB_CLIENT_ID, errMsg)
    }

    /**
     * Obtains the Google Account Result (including ID Token) via Android Credential Manager.
     *
     * Launches the official Google Account Chooser UI using GetGoogleIdOption (with filterByAuthorizedAccounts = false)
     * and provides fallback to GetSignInWithGoogleOption if needed.
     */
    suspend fun getGoogleAccount(
        activityContext: Context,
        explicitClientId: String? = null,
        filterByAuthorizedAccounts: Boolean = false
    ): GoogleAccountResult = withContext(Dispatchers.IO) {
        val serverClientId = resolveWebClientId(explicitClientId)
        Log.d(TAG, "[CredentialManager] Initializing Google ID request with serverClientId=${serverClientId.take(15)}... filterByAuthorizedAccounts=$filterByAuthorizedAccounts")

        val credentialManager = CredentialManager.create(activityContext)

        // Build primary GetGoogleIdOption with filterByAuthorizedAccounts = false so all available accounts appear
        val googleIdOption = try {
            GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setAutoSelectEnabled(false)
                .build()
        } catch (e: Exception) {
            Log.w(TAG, "[CredentialManager] Falling back to GetSignInWithGoogleOption: ${e.message}")
            GetSignInWithGoogleOption.Builder(serverClientId).build()
        }

        val response: GetCredentialResponse = try {
            Log.d(TAG, "[CredentialManager] Launching Google Account chooser...")
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            credentialManager.getCredential(activityContext, request)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "[CredentialManager] Google sign-in was cancelled by user.")
            throw AuthException(AuthErrorCode.USER_CANCELLED, "Google sign-in was cancelled.", e)
        } catch (noCred: NoCredentialException) {
            Log.w(TAG, "[CredentialManager] NoCredentialException on GetGoogleIdOption. Attempting GetSignInWithGoogleOption fallback...", noCred)
            try {
                val fallbackOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(fallbackOption)
                    .build()
                credentialManager.getCredential(activityContext, fallbackRequest)
            } catch (fallbackCancel: GetCredentialCancellationException) {
                Log.d(TAG, "[CredentialManager] Fallback Google sign-in was cancelled by user.")
                throw AuthException(AuthErrorCode.USER_CANCELLED, "Google sign-in was cancelled.", fallbackCancel)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "[CredentialManager] Fallback also failed: ${fallbackEx.message}", fallbackEx)
                throw mapCredentialException(fallbackEx)
            }
        } catch (e: Exception) {
            throw mapCredentialException(e)
        }

        val credential = response.credential
        var idToken: String? = null
        var email: String? = null
        var displayName: String? = null
        var photoUrl: String? = null

        if (credential is CustomCredential) {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    idToken = googleIdTokenCredential.idToken
                    email = googleIdTokenCredential.id
                    displayName = googleIdTokenCredential.displayName
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                } catch (e: Exception) {
                    Log.w(TAG, "[CredentialManager] GoogleIdTokenCredential.createFrom failed, parsing bundle directly: ${e.message}")
                }
            }

            // Direct bundle parsing fallback
            if (idToken.isNullOrBlank()) {
                val bundle: Bundle = credential.data
                idToken = bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                    ?: bundle.getString("id_token")
                    ?: bundle.getString("google_id_token")
                    ?: bundle.getString("token")

                email = email ?: bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID")
                    ?: bundle.getString("id")
                    ?: bundle.getString("email")

                displayName = displayName ?: bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_DISPLAY_NAME")
                    ?: bundle.getString("displayName")
                    ?: bundle.getString("name")

                photoUrl = photoUrl ?: bundle.getString("photoUrl")
            }
        }

        if (idToken.isNullOrBlank()) {
            val errMsg = "Unable to retrieve Google security token. Please try again."
            Log.e(TAG, "[CredentialManager] $errMsg credential class=${credential.javaClass.name}")
            throw AuthException(AuthErrorCode.FIREBASE_AUTH_ERROR, errMsg)
        }

        val finalEmail = email?.lowercase()?.trim() ?: ""
        val finalDisplayName = displayName?.takeIf { it.isNotBlank() } ?: finalEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val finalPhoto = photoUrl ?: ""

        Log.d(TAG, "[CredentialManager] Successfully retrieved Google ID token for account: $finalEmail")
        return@withContext GoogleAccountResult(
            idToken = idToken,
            email = finalEmail,
            displayName = finalDisplayName,
            photoUrl = finalPhoto
        )
    }

    private fun mapCredentialException(e: Exception): AuthException {
        return when (e) {
            is GetCredentialCancellationException -> {
                AuthException(AuthErrorCode.USER_CANCELLED, "Google sign-in was cancelled.", e)
            }
            is GetCredentialProviderConfigurationException -> {
                Log.e(TAG, "[CredentialManager] ProviderConfigurationException: ${e.message}", e)
                AuthException(
                    AuthErrorCode.OAUTH_CONFIGURATION_ERROR,
                    "Google Sign-In configuration is incomplete. Please ensure Google Play Services is available and updated.",
                    e
                )
            }
            is GetCredentialCustomException -> {
                Log.e(TAG, "[CredentialManager] GetCredentialCustomException: type=${e.type}, message=${e.message}", e)
                val isNetwork = e.message?.contains("network", ignoreCase = true) == true
                val isDevError = e.type.contains("16") || e.message?.contains("16") == true || e.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true
                val isError10 = e.type.contains("10") || e.message?.contains("10") == true

                val msg = when {
                    isNetwork -> "Network error during Google authentication. Please check your internet connection."
                    isDevError -> "Google OAuth configuration does not match this app (Developer Error 16 / SHA-1 fingerprint mismatch). Please verify SHA-1 in Firebase Console and ensure Google Sign-In is enabled."
                    isError10 -> "Google Sign-In configuration mismatch (Error 10). Please verify the Web Client ID and SHA-1 in Firebase Console."
                    else -> "Google authentication service error: ${e.message ?: e.type}"
                }
                val code = if (isNetwork) AuthErrorCode.NETWORK_ERROR else AuthErrorCode.OAUTH_CONFIGURATION_ERROR
                AuthException(code, msg, e)
            }
            is NoCredentialException -> {
                Log.w(TAG, "[CredentialManager] NoCredentialException: ${e.message}", e)
                AuthException(
                    AuthErrorCode.INVALID_CREDENTIAL,
                    "Google authentication could not be completed. Please try again.",
                    e
                )
            }
            is GetCredentialException -> {
                Log.e(TAG, "[CredentialManager] GetCredentialException (${e.javaClass.simpleName}): ${e.message}", e)
                AuthException(
                    AuthErrorCode.UNKNOWN_ERROR,
                    "Google account selection failed: ${e.message ?: "Please try again."}",
                    e
                )
            }
            else -> {
                Log.e(TAG, "[CredentialManager] Unexpected error: ${e.message}", e)
                AuthException(
                    AuthErrorCode.UNKNOWN_ERROR,
                    "Google sign-in failed: ${e.message ?: "Please try again."}",
                    e
                )
            }
        }
    }

    /**
     * Authenticates with Firebase using the Google ID Token obtained from Credential Manager.
     */
    suspend fun authenticateWithFirebase(googleAccount: GoogleAccountResult): FirebaseUser = withContext(Dispatchers.IO) {
        Log.d(TAG, "[FirebaseAuth] Authenticating with Firebase using Google ID Token for ${googleAccount.email}...")

        val firebaseCredential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)

        val authResult = try {
            awaitTask(firebaseAuth.signInWithCredential(firebaseCredential))
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "[FirebaseAuth] Network error during Firebase sign-in", e)
            throw AuthException(AuthErrorCode.NETWORK_ERROR, "Unable to connect. Please check your internet connection.", e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "[FirebaseAuth] Invalid Firebase credential", e)
            throw AuthException(AuthErrorCode.INVALID_CREDENTIAL, "Invalid authentication credentials. Please try again.", e)
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e(TAG, "[FirebaseAuth] User collision error", e)
            throw AuthException(AuthErrorCode.INVALID_CREDENTIAL, "An account already exists with this email address.", e)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "[FirebaseAuth] FirebaseAuthException code=${e.errorCode}: ${e.message}", e)
            val msg = when (e.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" -> "Google Sign-In is not enabled in Firebase Authentication."
                "ERROR_INVALID_CREDENTIAL" -> "Invalid authentication credentials."
                else -> "Unable to complete authentication. Please try again."
            }
            throw AuthException(AuthErrorCode.FIREBASE_AUTH_ERROR, msg, e)
        } catch (e: Exception) {
            Log.e(TAG, "[FirebaseAuth] Unexpected error during Firebase signInWithCredential", e)
            throw AuthException(AuthErrorCode.UNKNOWN_ERROR, "Unable to sign in right now. Please try again.", e)
        }

        val user = authResult.user ?: firebaseAuth.currentUser
        if (user == null) {
            val msg = "Firebase authentication succeeded but authenticated user is null."
            Log.e(TAG, "[FirebaseAuth] $msg")
            throw AuthException(AuthErrorCode.FIREBASE_AUTH_ERROR, msg)
        }

        Log.d(TAG, "[FirebaseAuth] Firebase authentication successful. UID: ${user.uid}, Email: ${user.email}")
        return@withContext user
    }

    /**
     * Clears credential state upon sign out.
     */
    suspend fun clearCredentialState(activityContext: Context) = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(activityContext)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "[CredentialManager] Cleared credential state.")
        } catch (e: Exception) {
            Log.w(TAG, "[CredentialManager] Failed to clear credential state: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "GoogleSignInHelper"

        suspend fun <T> awaitTask(task: com.google.android.gms.tasks.Task<T>): T =
            suspendCancellableCoroutine { continuation ->
                task.addOnCompleteListener { completedTask ->
                    if (completedTask.isSuccessful) {
                        continuation.resume(completedTask.result)
                    } else {
                        val exception = completedTask.exception ?: Exception("Task failed with unknown error")
                        continuation.resumeWithException(exception)
                    }
                }
            }
    }
}
