package com.example.firechat.domain.usecase.chat

import com.example.firechat.data.model.Chat
import com.example.firechat.domain.repository.ChatRepository

/**
 * Use case for creating a new chat.
 *
 * This class handles the business logic for creating a new chat with the given participants.
 * It interacts with the [ChatRepository] to persist the chat data.
 *
 * @property chatRepository The repository responsible for chat data operations.
 */
class CreateChatUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(participantIds: List<String>): Result<Chat> {
        if (participantIds.isEmpty()) {
            return Result.failure(Exception("Chat must have at least one participant"))
        }
        
        return chatRepository.createChat(participantIds)
    }
}