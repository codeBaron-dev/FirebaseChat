package com.example.firechat.data.model

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantDetails: List<User> = emptyList(),
    val lastMessage: String = "",
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageTime: Long = 0L,
    val lastMessageSender: String = "",
    val unreadCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getOtherParticipant(currentUserId: String): User? {
        return participantDetails.firstOrNull { it.id != currentUserId }
    }
    
    fun getDisplayLastMessage(): String {
        return when (lastMessageType) {
            MessageType.TEXT -> lastMessage
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.AUDIO -> "🎵 Audio"
            MessageType.EMOJI -> lastMessage
            MessageType.PHOTO -> "📷 Photo"
            MessageType.THREAD -> "💬 Thread"
            MessageType.VOICE_CALL -> "📞 Voice call"
            MessageType.VIDEO_CALL -> "📹 Video call"
        }
    }
}