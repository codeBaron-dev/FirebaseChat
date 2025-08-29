package com.example.firechat.domain.repository

import com.example.firechat.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(userId: String): User?
    fun searchUsers(query: String): Flow<List<User>>
}