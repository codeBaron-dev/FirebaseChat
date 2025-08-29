package com.example.firechat.domain.repository

import com.example.firechat.data.model.Message
import com.example.firechat.data.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getChatMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message): Result<Message>
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}