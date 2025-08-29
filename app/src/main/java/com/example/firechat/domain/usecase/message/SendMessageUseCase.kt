package com.example.firechat.domain.usecase.message

import com.example.firechat.data.model.Message
import com.example.firechat.domain.repository.ChatRepository
import com.example.firechat.domain.repository.MessageRepository

/**
 * Use case for sending a message.
 *
 * This use case handles the business logic for sending a message, including validating the message content
 * and updating the chat's last message if the message is sent successfully.
 *
 * @property messageRepository The repository for managing messages.
 * @property chatRepository The repository for managing chats.
 */
class SendMessageUseCase(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(message: Message): Result<Message> {
        if (message.content.isBlank()) {
            return Result.failure(Exception("Message content cannot be empty"))
        }
        
        val result = messageRepository.sendMessage(message)
        
        if (result.isSuccess) {
            // Update the chat's last message
            chatRepository.updateLastMessage(
                chatId = message.chatId,
                message = message.content,
                messageType = message.type.name,
                senderId = message.senderId
            )
        }
        
        return result
    }
}