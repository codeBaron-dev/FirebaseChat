package com.example.firechat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.model.Chat
import com.example.firechat.domain.repository.AuthRepository
import com.example.firechat.domain.usecase.chat.GetChatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the ChatListScreen.
 *
 * This ViewModel is responsible for loading and managing the list of chats for the current user.
 * It observes the user's authentication state and fetches chats accordingly.
 *
 * @property getChatsUseCase The use case for fetching chats.
 * @property authRepository The repository for accessing authentication state.
 */
class ChatListViewModel(
    private val getChatsUseCase: GetChatsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
    
    init {
        loadChats()
    }
    
    /**
     * Loads the chats for the current user.
     *
     * This function observes the current user and their login status from the [authRepository].
     * If the user is logged in and not null, it fetches the chats for the user using [getChatsUseCase].
     * The fetched chats are then used to update the UI state.
     * If the user is not logged in or is null, the UI state is updated to reflect an empty list of chats.
     */
    private fun loadChats() {
        viewModelScope.launch {
            combine(
                authRepository.currentUser,
                authRepository.isLoggedIn
            ) { user, isLoggedIn ->
                Pair(user, isLoggedIn)
            }.collect { (user, isLoggedIn) ->
                if (isLoggedIn && user != null) {
                    getChatsUseCase(user.id).collect { chats ->
                        _uiState.value = _uiState.value.copy(
                            chats = chats,
                            isLoading = false,
                            isEmpty = chats.isEmpty()
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        chats = emptyList(),
                        isLoading = false,
                        isEmpty = true
                    )
                }
            }
        }
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadChats()
    }
}

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)