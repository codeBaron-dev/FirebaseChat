package com.example.firechat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.model.Chat
import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.AuthRepository
import com.example.firechat.domain.repository.UserRepository
import com.example.firechat.domain.usecase.chat.CreateChatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the UserListScreen.
 *
 * This ViewModel is responsible for fetching and managing the list of users,
 * handling user search functionality, and initiating chat creation.
 *
 * @property userRepository Repository for accessing user data.
 * @property createChatUseCase Use case for creating new chats.
 * @property authRepository Repository for accessing authentication-related data.
 */
class UserListViewModel(
    private val userRepository: UserRepository,
    private val createChatUseCase: CreateChatUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    
    init {
        loadAllUsers()
    }
    
    /**
     * Loads all users from the repository and updates the UI state.
     *
     * This function launches a coroutine in the viewModelScope to perform the asynchronous operation.
     * It first sets the isLoading flag to true to indicate that data is being fetched.
     * Then, it collects the flow of users from the userRepository and updates the UI state
     * with the fetched users and sets isLoading to false.
     */
    fun loadAllUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            userRepository.getAllUsers().collect { users ->
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Searches for users based on the provided query and updates the UI state.
     *
     * This function launches a coroutine in the viewModelScope to perform the asynchronous search operation.
     * It first sets the isLoading flag to true to indicate that data is being fetched.
     * Then, it collects the flow of users from the userRepository based on the search query
     * and updates the UI state with the fetched users and sets isLoading to false.
     *
     * @param query The search query string.
     */
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            userRepository.searchUsers(query).collect { users ->
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Creates a new chat with the specified user.
     *
     * This function launches a coroutine in the viewModelScope to perform the asynchronous operation.
     * It retrieves the current user from the authRepository.
     * If the current user is not null, it calls the createChatUseCase with the IDs of the current user and the other user.
     * On success, it invokes the onChatCreated callback with the newly created chat.
     * On failure, it updates the UI state with the error message.
     *
     * @param otherUser The user with whom to create the chat.
     * @param onChatCreated A callback function that is invoked when the chat is successfully created.
     *                      It receives the created Chat object as a parameter.
     */
    fun createChatWithUser(otherUser: User, onChatCreated: (Chat) -> Unit) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val result = createChatUseCase(listOf(currentUser.id, otherUser.id))
                result.fold(
                    onSuccess = { chat ->
                        onChatCreated(chat)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class UserListUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)