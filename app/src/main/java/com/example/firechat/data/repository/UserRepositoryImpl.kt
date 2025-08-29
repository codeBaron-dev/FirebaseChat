package com.example.firechat.data.repository

import com.example.firechat.data.model.User
import com.example.firechat.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [UserRepository] that uses Firebase Firestore as a data source.
 *
 * @property firestore The Firebase Firestore instance to use for data access.
 */
class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {
    
    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { document ->
                        document.toObject(User::class.java)?.copy(id = document.id)
                    }
                    trySend(users)
                }
            }
        
        awaitClose {
            listener.remove()
        }
    }
    
    /**
     * Retrieves a user by their ID from Firestore.
     *
     * @param userId The ID of the user to retrieve.
     * @return The [User] object if found, otherwise null.
     */
    override suspend fun getUserById(userId: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            document.toObject(User::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Searches for users whose display name starts with the given query.
     *
     * This function queries the Firestore "users" collection, ordering the results by "displayName".
     * It uses `startAt` and `endAt` with a special Unicode character `\uf8ff` to perform a prefix search.
     * The results are emitted as a Flow of lists of User objects.
     * The listener is automatically removed when the Flow is closed.
     *
     * @param query The string to search for in user display names.
     * @return A Flow emitting lists of User objects that match the query.
     *         If an error occurs during the Firestore operation, the Flow will close with the error.
     */
    override fun searchUsers(query: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .orderBy("displayName")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { document ->
                        document.toObject(User::class.java)?.copy(id = document.id)
                    }
                    trySend(users)
                }
            }
        
        awaitClose {
            listener.remove()
        }
    }
}