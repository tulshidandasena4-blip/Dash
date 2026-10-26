package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.security.AuthUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginMode: Boolean = true
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<AuthUser?> = repository.currentUser

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _firebaseUser.value = auth.currentUser
    }

    init {
        Firebase.auth.addAuthStateListener(authStateListener)
        viewModelScope.launch {
            repository.seedDefaultAdminIfEmpty()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Firebase.auth.removeAuthStateListener(authStateListener)
    }

    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = repository.login(email, pass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Welcome back, ${res.user.name}!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun register(name: String, email: String, pass: String, role: String = "Operator") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = repository.register(name, email, pass, role)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Account registered successfully for ${res.user.name}"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.message
                    )
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Google Sign-In configuration missing: default_web_client_id not found"
            )
            return
        }

        val activity = context as? Activity
        if (activity == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Activity context is required for Google Sign-In")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val credentialManager = CredentialManager.create(activity)
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user

                    if (user != null) {
                        val authUser = AuthUser(
                            id = 1L,
                            name = user.displayName ?: "Authorized Operator",
                            email = user.email ?: "google_user@dashpollution.com",
                            role = "Operator"
                        )
                        repository.saveDirectSession(authUser)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Signed in as ${user.displayName ?: user.email}"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to obtain user profile")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Unexpected credential received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Sign in failed"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun attemptAutoSignIn(context: Context) {
        val firebaseCurrent = Firebase.auth.currentUser
        if (firebaseCurrent != null) {
            val authUser = AuthUser(
                id = 1L,
                name = firebaseCurrent.displayName ?: "Authorized Operator",
                email = firebaseCurrent.email ?: "operator@dashpollution.com",
                role = "Operator"
            )
            repository.saveDirectSession(authUser)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val authUser = AuthUser(
                            id = 1L,
                            name = user.displayName ?: "Authorized Operator",
                            email = user.email ?: "google_user@dashpollution.com",
                            role = "Operator"
                        )
                        repository.saveDirectSession(authUser)
                    }
                }
            } catch (_: Exception) {
                // Silent check failure, remain on login screen
            }
        }
    }

    fun logout() {
        Firebase.auth.signOut()
        repository.logout()
    }
}
