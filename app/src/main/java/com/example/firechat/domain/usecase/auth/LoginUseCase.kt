package com.example.firechat.domain.usecase.auth

import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.AuthRepository

/**
 * Use case for logging in a user.
 *
 * This class handles the business logic for user login. It validates the input
 * email and password and then interacts with the [AuthRepository] to perform
 * the actual login operation.
 *
 * @property authRepository The repository responsible for authentication operations.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Invalid email format"))
        }
        
        return authRepository.login(email, password)
    }
}