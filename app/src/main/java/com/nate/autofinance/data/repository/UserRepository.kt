package com.nate.autofinance.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.nate.autofinance.data.local.UserDao
import com.nate.autofinance.data.remote.FirebaseUserService
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.domain.models.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val firebaseUserService: FirebaseUserService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "UserRepository"
    }

    suspend fun addUser(user: User): Long? = withContext(ioDispatcher) {

        val localInsertResult = userDao.insert(user)

        try {
            // Tenta inserir o usuário no Firebase
            val firebaseDocId = firebaseUserService.addUser(user)
            if (firebaseDocId != null) {
                Log.i(TAG, "User added to Firebase with id: $firebaseDocId")
                // Atualiza o usuário local com o firebaseDocId e status SYNCED
                val updatedUser = user.copy(
                    firebaseDocId = firebaseDocId,
                    syncStatus = SyncStatus.SYNCED
                )
                userDao.update(updatedUser)
            }
            localInsertResult
        } catch (ex: Exception) {
            Log.e(TAG, "Error sending user to Firebase", ex)
            // Em caso de erro, marca o syncStatus como FAILED
            val updatedUser = user.copy(
                firebaseDocId = null,
                syncStatus = SyncStatus.FAILED
            )
            userDao.update(updatedUser)
            null
        }
    }

    suspend fun getOrCreateUser(firebaseUser: FirebaseUser): User = withContext(ioDispatcher) {
        userDao.getUserByEmail(firebaseUser.email!!)?.let { return@withContext it }

        val remoteUser = firebaseUserService.getUserById()

        val toInsert = (remoteUser
            ?.copy(syncStatus = SyncStatus.SYNCED)
            ?: User(
                id           = 0,
                name         = firebaseUser.displayName.orEmpty(),
                email        = firebaseUser.email!!,
                firebaseDocId= firebaseUser.uid,
                isSubscribed = false,
                syncStatus   = SyncStatus.SYNCED
            )
                )

        val newId = userDao.insert(toInsert)
        return@withContext userDao.getUserById(newId.toInt())!!
    }

    suspend fun updateUser(user: User) = withContext(ioDispatcher) {
        // Atualiza o registro local primeiro
        userDao.update(user)
        try {
            val updatedData = hashMapOf<String, Any>(
                "name" to user.name,
                "email" to user.email,
                "syncStatus" to user.syncStatus.name,
                "isSubscribed" to user.isSubscribed
            )
            // Se o firebaseDocId existir, atualiza o documento remoto
            if (user.firebaseDocId != null) {
                firebaseUserService.updateUser(updatedData)
                Log.i(TAG, "User updated in Firebase for id: ${user.firebaseDocId}")
                val updatedUser = user.copy(syncStatus = SyncStatus.SYNCED)
                userDao.update(updatedUser)
            } else {
                // Se não houver firebaseDocId, tenta adicioná-lo
                val newFirebaseDocId = firebaseUserService.addUser(user)
                if (newFirebaseDocId != null) {
                    Log.i(TAG, "User added to Firebase during update with id: $newFirebaseDocId")
                    val updatedUser =
                        user.copy(firebaseDocId = newFirebaseDocId, syncStatus = SyncStatus.SYNCED)
                    userDao.update(updatedUser)
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating user in Firebase", ex)
            val updatedUser = user.copy(syncStatus = SyncStatus.FAILED)
            userDao.update(updatedUser)
        }
    }

    suspend fun deleteUser(user: User) = withContext(ioDispatcher) {
        // Remove do banco local
        userDao.delete(user)
        try {
            user.firebaseDocId?.let { docId ->
                firebaseUserService.deleteUser()
                Log.i(TAG, "User deleted from Firebase for id: $docId")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error deleting user from Firebase", ex)
        }
    }

    suspend fun getUserById(id: Int): User? = withContext(ioDispatcher) {
        userDao.getUserById(id)
    }

    suspend fun getUserByEmail(email: String): User? =
        withContext(ioDispatcher) {
            userDao.getUserByEmail(email)
        }

}
