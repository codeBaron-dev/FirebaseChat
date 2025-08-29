package com.example.firechat.domain.usecase.auth

import com.example.firechat.domain.repository.AuthRepository

/**
 * Use case for logging out a user.
 *
 * This class encapsulates the logic for logging out a user through the [AuthRepository].
 * It provides a simple suspendable function `invoke` to trigger the logout process.
 *
 * @param authRepository The repository responsible for authentication-related operations.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}