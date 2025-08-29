package com.example.firechat.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
) {
    val initials: String
        get() = displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
}