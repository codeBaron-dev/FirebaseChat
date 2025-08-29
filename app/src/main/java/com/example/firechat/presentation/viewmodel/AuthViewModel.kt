package com.example.firechat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.model.User
import com.example.firechat.domain.usecase.auth.LoginUseCase
import com.example.firechat.domain.usecase.auth.LogoutUseCase
import com.example.firechat.domain.usecase.auth.RegisterUseCase
import com.example.firechat.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication-related operations.
 *
 * This ViewModel handles user login, registration, and logout. It interacts with
 * use cases for these operations and exposes UI state and authentication status
 * to the UI layer.
 *
 * @param loginUseCase The use case for handling user login.
 * @param registerUseCase The use case for handling user registration.
 * @param logoutUseCase The use case for handling user logout.
 * @param authRepository The repository for accessing authentication state (current user, login status).
 */
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    val currentUser = authRepository.currentUser
    val isLoggedIn = authRepository.isLoggedIn
    
    /**
     * Logs in a user with the given email and password.
     *
     * Updates the UI state to indicate loading and clears any previous error messages.
     * Invokes the [loginUseCase] to perform the login operation.
     * On successful login, updates the UI state to stop loading.
     * On failure, updates the UI state to stop loading and sets an error message.
     *
     * @param email The email of the user to log in.
     * @param password The password of the user to log in.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = loginUseCase(email, password)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Registers a new user with the provided email, password, and display name.
     *
     * This function initiates the registration process by:
     * 1. Updating the UI state to indicate loading and clearing any previous error messages.
     * 2. Calling the `registerUseCase` with the provided credentials.
     * 3. Handling the result of the registration:
     *    - On success: Updates the UI state to indicate that loading has finished.
     *    - On failure: Updates the UI state to indicate that loading has finished and sets an error message.
     *
     * @param email The email address of the user to register.
     * @param password The password for the new user account.
     * @param displayName The desired display name for the new user.
     */
    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = registerUseCase(email, password, displayName)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Logs out the current user.
     *
     * This function initiates the logout process by calling the `logoutUseCase`.
     * It operates within the `viewModelScope` to ensure proper lifecycle management
     * of the coroutine.
     */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)