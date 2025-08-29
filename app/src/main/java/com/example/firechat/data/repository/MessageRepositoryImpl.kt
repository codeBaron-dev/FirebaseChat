package com.example.firechat.data.repository

import com.example.firechat.data.model.Message
import com.example.firechat.data.model.MessageStatus
import com.example.firechat.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [MessageRepository] that uses Firebase Firestore as the data source.
 *
 * @property firestore The FirebaseFirestore instance to interact with the database.
 */
class MessageRepositoryImpl(
    private val firestore: FirebaseFirestore
) : MessageRepository {
    
    override fun getChatMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { document ->
                        document.toObject(Message::class.java)?.copy(id = document.id)
                    }
                    trySend(messages)
                }
            }
        
        awaitClose {
            listener.remove()
        }
    }
    
    /**
     * Sends a message to a chat.
     *
     * @param message The message to be sent.
     * @return A [Result] indicating success or failure. On success, it contains the sent message with its ID.
     */
    override suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            val messageData = hashMapOf(
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "type" to message.type.name,
                "timestamp" to message.timestamp,
                "status" to message.status.name,
                "senderName" to message.senderName,
                "senderAvatar" to message.senderAvatar
            )
            
            val documentRef = firestore.collection("chats")
                .document(message.chatId)
                .collection("messages")
                .add(messageData)
                .await()
            
            val sentMessage = message.copy(id = documentRef.id)
            Result.success(sentMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates the status of a specific message.
     *
     * Note: The current implementation is a placeholder. In a real scenario,
     * updating a message status would likely require knowing the `chatId` to locate
     * the message within the Firestore structure. This method would need to be
     * restructured or the data model adjusted to support this functionality efficiently.
     *
     * @param messageId The ID of the message to update.
     * @param status The new [MessageStatus] for the message.
     * @return A [Result] indicating success (with [Unit]) or failure (with an [Exception]).
     */
    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus): Result<Unit> {
        return try {
            // This would require knowing the chatId, which we don't have in this method
            // In a real implementation, you might need to restructure this or store messages differently
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes a specific message.
     *
     * Note: The current implementation is a placeholder. Similar to [updateMessageStatus],
     * deleting a message in Firestore typically requires knowing the `chatId` to locate
     * the message within its collection. This method would need to be
     * restructured or the data model adjusted to support this functionality efficiently.
     *
     * @param messageId The ID of the message to delete.
     * @return A [Result] indicating success (with [Unit]) or failure (with an [Exception]).
     */
    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            // Similar issue as updateMessageStatus - need chatId
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}