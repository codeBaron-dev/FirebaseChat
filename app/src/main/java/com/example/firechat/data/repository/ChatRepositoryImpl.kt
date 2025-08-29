package com.example.firechat.data.repository

import com.example.firechat.data.model.Chat
import com.example.firechat.data.model.MessageType
import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [ChatRepository] that uses Firebase Firestore as its data source.
 * This class handles all chat-related operations, including retrieving, creating,
 * and updating chat data in Firestore.
 *
 * @property firestore The FirebaseFirestore instance used for database operations.
 */
class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ChatRepository {
    
    /**
     * Retrieves a flow of chat lists for a given user.
     *
     * This function listens for real-time updates to the user's chats in Firestore.
     * It fetches chats where the user is a participant, sorts them by the last message time,
     * and loads the details of the other participants in each chat.
     *
     * The returned Flow will emit a new list of chats whenever there's an update
     * (e.g., a new message arrives, a new chat is created, or participant details change).
     *
     * Errors during Firestore operations will be propagated through the Flow.
     * The listener is automatically removed when the Flow is cancelled.
     *
     * @param userId The ID of the user whose chats are to be fetched.
     * @return A Flow emitting a list of [Chat] objects. Each [Chat] object will
     *         contain the details of its participants. The list is sorted by
     *         [Chat.lastMessageTime] in descending order.
     */
    override fun getUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val chats = mutableListOf<Chat>()
                    
                    for (document in snapshot.documents) {
                        val chat = document.toObject(Chat::class.java)?.copy(id = document.id)
                        if (chat != null) {
                            chats.add(chat)
                        }
                    }
                    
                    // Sort chats by lastMessageTime in code instead of Firestore
                    val sortedChats = chats.sortedByDescending { it.lastMessageTime }
                    
                    // Load participant details for each chat
                    loadParticipantDetails(sortedChats, userId) { chatsWithDetails ->
                        trySend(chatsWithDetails)
                    }
                }
            }
        
        awaitClose {
            listener.remove()
        }
    }
    
    /**
     * Loads participant details for a list of chats.
     *
     * This function fetches user details for all participants in the provided chats from Firestore.
     * It then updates each chat object with the fetched participant details.
     *
     * @param chats The list of chats for which to load participant details.
     * @param currentUserId The ID of the current user (not directly used in fetching, but good for context).
     * @param onComplete A callback function that is invoked when all participant details have been loaded
     *                   (or when all requests have completed, even if some failed).
     *                   It receives the list of chats updated with participant details.
     */
    private fun loadParticipantDetails(
        chats: List<Chat>,
        currentUserId: String,
        onComplete: (List<Chat>) -> Unit
    ) {
        if (chats.isEmpty()) {
            onComplete(emptyList())
            return
        }
        
        val allParticipants = chats.flatMap { it.participants }.distinct()
        
        // Load user details one by one using document IDs
        val userMap = mutableMapOf<String, User>()
        var loadedCount = 0
        
        if (allParticipants.isEmpty()) {
            onComplete(chats)
            return
        }
        
        allParticipants.forEach { userId ->
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        doc.toObject(User::class.java)?.let { user ->
                            userMap[doc.id] = user.copy(id = doc.id)
                        }
                    }
                    
                    loadedCount++
                    if (loadedCount == allParticipants.size) {
                        // All users loaded, update chats
                        val updatedChats = chats.map { chat ->
                            val participantDetails = chat.participants.mapNotNull { userMap[it] }
                            chat.copy(participantDetails = participantDetails)
                        }
                        onComplete(updatedChats)
                    }
                }
                .addOnFailureListener {
                    loadedCount++
                    if (loadedCount == allParticipants.size) {
                        // All requests completed (some failed), return what we have
                        val updatedChats = chats.map { chat ->
                            val participantDetails = chat.participants.mapNotNull { userMap[it] }
                            chat.copy(participantDetails = participantDetails)
                        }
                        onComplete(updatedChats)
                    }
                }
        }
    }
    
    /**
     * Creates a new chat with the given participants.
     *
     * @param participantIds The list of user IDs to be included in the chat.
     * @return A [Result] object containing the created [Chat] on success, or an exception on failure.
     */
    override suspend fun createChat(participantIds: List<String>): Result<Chat> {
        return try {
            val chatData = hashMapOf(
                "participants" to participantIds,
                "lastMessage" to "",
                "lastMessageType" to MessageType.TEXT.name,
                "lastMessageTime" to System.currentTimeMillis(),
                "lastMessageSender" to "",
                "createdAt" to System.currentTimeMillis()
            )
            
            val documentRef = firestore.collection("chats").add(chatData).await()
            
            val chat = Chat(
                id = documentRef.id,
                participants = participantIds,
                lastMessageTime = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Retrieves a chat by its ID.
     *
     * @param chatId The ID of the chat to retrieve.
     * @return The chat if found, or null if an error occurs or the chat doesn't exist.
     */
    override suspend fun getChatById(chatId: String): Chat? {
        return try {
            val document = firestore.collection("chats")
                .document(chatId)
                .get()
                .await()
            
            document.toObject(Chat::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Updates the last message details of a chat in Firestore.
     *
     * This function takes the chat ID, the new message content, message type, and sender ID as input.
     * It constructs a map of updates including the message, message type, current timestamp, and sender ID.
     * It then attempts to update the corresponding chat document in the "chats" collection in Firestore.
     *
     * @param chatId The ID of the chat to update.
     * @param message The content of the last message.
     * @param messageType The type of the last message (e.g., TEXT, IMAGE).
     * @param senderId The ID of the user who sent the last message.
     * @return A [Result] indicating success (with [Unit]) or failure (with an [Exception]).
     */
    override suspend fun updateLastMessage(
        chatId: String,
        message: String,
        messageType: String,
        senderId: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "lastMessage" to message,
                "lastMessageType" to messageType,
                "lastMessageTime" to System.currentTimeMillis(),
                "lastMessageSender" to senderId
            )
            
            firestore.collection("chats")
                .document(chatId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}