package com.example.firechat.domain.repository

import com.example.firechat.data.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getUserChats(userId: String): Flow<List<Chat>>
    suspend fun createChat(participantIds: List<String>): Result<Chat>
    suspend fun getChatById(chatId: String): Chat?
    suspend fun updateLastMessage(chatId: String, message: String, messageType: String, senderId: String): Result<Unit>
}