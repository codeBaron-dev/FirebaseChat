package com.example.firechat.domain.usecase.chat

import com.example.firechat.data.model.Chat
import com.example.firechat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving a list of chats for a specific user.
 *
 * This use case depends on a [ChatRepository] to fetch the chat data.
 *
 * @property chatRepository The repository responsible for chat data operations.
 */
class GetChatsUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(userId: String): Flow<List<Chat>> {
        return chatRepository.getUserChats(userId)
    }
}