package com.example.firechat.domain.usecase.message

import com.example.firechat.data.model.Message
import com.example.firechat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving messages for a specific chat.
 *
 * This use case takes a chat ID as input and returns a Flow of lists of messages.
 * The Flow will emit a new list of messages whenever the messages in the chat are updated.
 *
 * @property messageRepository The repository for accessing message data.
 */
class GetMessagesUseCase(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(chatId: String): Flow<List<Message>> {
        return messageRepository.getChatMessages(chatId)
    }
}