package com.example.firechat.data.model

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val senderName: String = "",
    val senderAvatar: String = ""
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    EMOJI,
    PHOTO,
    THREAD,
    VOICE_CALL,
    VIDEO_CALL
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}