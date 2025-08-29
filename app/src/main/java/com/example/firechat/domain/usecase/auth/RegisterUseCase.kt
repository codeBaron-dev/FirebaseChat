package com.example.firechat.domain.usecase.auth

import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.AuthRepository

/**
 * Use case for registering a new user.
 *
 * This class handles the business logic for user registration, including input validation
 * and interaction with the [AuthRepository].
 *
 * @property authRepository The repository responsible for authentication operations.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }
        
        if (displayName.isBlank()) {
            return Result.failure(Exception("Display name cannot be empty"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Invalid email format"))
        }
        
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        
        return authRepository.register(email, password, displayName)
    }
}