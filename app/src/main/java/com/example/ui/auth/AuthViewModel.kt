package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserSession
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFirebaseConfigIssue: Boolean = false,
    val passwordResetSent: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, pass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, currentUser = result.user, errorMessage = null)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isFirebaseConfigIssue = result.isFirebaseConfigIssue
                    )
                }
            }
        }
    }

    fun signUp(email: String, pass: String, confirmPass: String, displayName: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.signUpWithEmail(email, pass, displayName)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, currentUser = result.user, errorMessage = null)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isFirebaseConfigIssue = result.isFirebaseConfigIssue
                    )
                }
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.signInWithCredential(credential)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, currentUser = result.user, errorMessage = null)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun signInAsDemo() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.signInAsDemo()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, currentUser = result.user, errorMessage = null)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email to reset password.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val res = authRepository.resetPassword(email)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, passwordResetSent = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Failed to send reset email."
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, passwordResetSent = false)
    }
}
