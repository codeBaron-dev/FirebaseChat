package com.example.firechat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firechat.data.model.Message
import com.example.firechat.data.model.MessageType
import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.AuthRepository
import com.example.firechat.domain.usecase.chat.CreateChatUseCase
import com.example.firechat.domain.usecase.message.GetMessagesUseCase
import com.example.firechat.domain.usecase.message.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var currentUser: User? = null
    private var currentChatId: String? = null
    
    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUser = user
            }
        }
    }
    
    /**
     * Loads messages for a given chat.
     *
     * If the `chatId` starts with "new_", it attempts to create a new chat with `otherUserId`.
     * Otherwise, it fetches messages for the existing `chatId`.
     *
     * The UI state is updated to reflect loading status, messages, and any errors.
     *
     * @param chatId The ID of the chat to load messages for. If it starts with "new_",
     * a new chat will be created.
     * @param otherUserId The ID of the other user in the chat. Required if `chatId`
     * starts with "new_".
     */
    fun loadMessages(chatId: String, otherUserId: String? = null) {
        if (currentChatId == chatId) return
        
        currentChatId = chatId
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        // Handle new chat creation
        if (chatId.startsWith("new_") && otherUserId != null) {
            createNewChat(otherUserId)
            return
        }
        
        viewModelScope.launch {
            getMessagesUseCase(chatId).collect { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    isLoading = false,
                    isEmpty = messages.isEmpty()
                )
            }
        }
    }
    
    /**
     * Creates a new chat with the given user.
     *
     * This function will first attempt to create a new chat between the current user and the user
     * identified by `otherUserId`. If successful, it will then start loading messages for the
     * newly created chat.
     *
     * The UI state will be updated to reflect the loading status, messages, and any potential errors.
     *
     * @param otherUserId The ID of the other user to create a chat with.
     */
    private fun createNewChat(otherUserId: String) {
        viewModelScope.launch {
            val user = currentUser ?: return@launch
            
            val result = createChatUseCase(listOf(user.id, otherUserId))
            result.fold(
                onSuccess = { chat ->
                    currentChatId = chat.id
                    // Start loading messages for the new chat
                    getMessagesUseCase(chat.id).collect { messages ->
                        _uiState.value = _uiState.value.copy(
                            messages = messages,
                            isLoading = false,
                            isEmpty = messages.isEmpty()
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message,
                        isEmpty = true
                    )
                }
            )
        }
    }
    
    /**
     * Sends a new message to the current chat.
     *
     * This function first checks if there's a logged-in user and an active chat.
     * If the message content is blank, the function returns without sending.
     * It then updates the UI state to indicate that a message is being sent.
     * A [Message] object is created with the provided content, receiver ID,
     * current user's details, and current chat ID.
     * The `sendMessageUseCase` is invoked to persist the message.
     * Finally, the UI state is updated based on the success or failure of the send operation.
     *
     * @param content The text content of the message.
     * @param receiverId The ID of the user who will receive the message.
     */
    fun sendMessage(content: String, receiverId: String) {
        val user = currentUser ?: return
        val chatId = currentChatId ?: return
        
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            
            val message = Message(
                chatId = chatId,
                senderId = user.id,
                receiverId = receiverId,
                content = content,
                type = MessageType.TEXT,
                timestamp = System.currentTimeMillis(),
                senderName = user.displayName,
                senderAvatar = user.avatarUrl
            )
            
            val result = sendMessageUseCase(message)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSending = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)