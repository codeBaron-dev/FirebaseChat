package com.example.firechat.data.repository

import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [AuthRepository] that uses Firebase Authentication and Firestore.
 *
 * @property auth The [FirebaseAuth] instance used for authentication.
 */
class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isOnline = true
                )
                trySend(user)
            } else {
                trySend(null)
            }
        }
        
        auth.addAuthStateListener(listener)
        
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }
    
    override val isLoggedIn: Flow<Boolean> = currentUser.map { it != null }
    
    /**
     * Logs in a user with the given email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return A [Result] object containing the [User] object if login is successful, or an [Exception] if login fails.
     */
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Update online status
                updateUserOnlineStatus(firebaseUser.uid, true)
                
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isOnline = true
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registers a new user with the given email, password, and display name.
     *
     * This function attempts to create a new user account using Firebase Authentication.
     * If successful, it updates the user's profile with the provided display name and
     * creates a corresponding user document in Firestore.
     *
     * @param email The email address of the user.
     * @param password The password for the new account.
     * @param displayName The desired display name for the user.
     * @return A [Result] object containing the newly created [User] on success,
     * or an [Exception] on failure.
     */
    override suspend fun register(email: String, password: String, displayName: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Update profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Create user document in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    isOnline = true
                )
                
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()
                
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logs out the current user.
     *
     * This function attempts to sign out the current user from Firebase Authentication.
     * Before signing out, it updates the user's online status to `false` in Firestore
     * if a user is currently logged in.
     *
     * @return A [Result] object that indicates success (with [Unit]) or failure (with an [Exception]).
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                updateUserOnlineStatus(currentUserId, false)
            }
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Retrieves the currently authenticated user.
     *
     * This function checks the Firebase Authentication state and returns a [User] object
     * if a user is currently signed in. If no user is signed in, it returns `null`.
     * The `isOnline` status of the returned [User] object is set to `true` as it's assumed
     * that if a user is being fetched, they are currently active.
     *
     * @return The [User] object representing the current user, or `null` if no user is signed in.
     */
    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                isOnline = true
            )
        } else {
            null
        }
    }
    
    /**
     * Updates the online status and last seen timestamp of a user in Firestore.
     *
     * This function performs an update operation on the "users" collection in Firestore.
     * It sets the `isOnline` field to the provided boolean value and updates the
     * `lastSeen` field with the current system time in milliseconds.
     * Any exceptions during the Firestore operation are caught and handled silently.
     *
     * @param userId The unique identifier of the user whose status is to be updated.
     * @param isOnline A boolean value indicating whether the user is online (`true`) or offline (`false`).
     */
    private suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}