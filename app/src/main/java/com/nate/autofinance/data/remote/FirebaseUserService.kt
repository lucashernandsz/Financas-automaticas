package com.nate.autofinance.data.remote

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nate.autofinance.data.models.User
import com.nate.autofinance.data.models.SyncStatus
import com.nate.autofinance.data.local.UserDao
import com.nate.autofinance.utils.SessionManager
import kotlinx.coroutines.tasks.await

/**
 * Serviço responsável pelas operações de persistência dos dados de usuários no Firestore,
 * utilizando sempre o FirebaseAuth UID como ID do documento.
 */
class FirebaseUserService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userDao: UserDao,
    private val session: SessionManager
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Recupera ou cria o registro no Firestore, sempre com documentId = Auth UID.
     * @return o Auth‐UID do usuário.
     */
    suspend fun getOrCreateUser(context: Context): String {
        // 1) Pega o UID do FirebaseAuth (único e consistente)
        val authUid = FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        // 2) Busca dados locais para enviar à nuvem
        val localUser = userDao.getUserById(session.getUserId(context)!!)
            ?: throw IllegalStateException("Usuário local não encontrado")

        // 3) Monta o mapa de campos que quer armazenar/atualizar
        val userMap = mapOf(
            "name"         to localUser.name,
            "email"        to localUser.email,
            "syncStatus"   to SyncStatus.SYNCED.name,
            "isSubscribed" to localUser.isSubscribed
        )

        // 4) Grava (ou atualiza) o documento com ID = authUid
        usersCollection
            .document(authUid)
            .set(userMap, SetOptions.merge())
            .await()

        // 5) Atualiza o campo firebaseDocId no banco local
        userDao.update(
            localUser.copy(firebaseDocId = authUid, syncStatus = SyncStatus.SYNCED)
        )

        return authUid
    }

    suspend fun addUser(user: User): String {
        // 1) obtém o UID atual do FirebaseAuth
        val authUid = FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        // 2) monta o mapa de campos que vão para a nuvem
        val userMap = hashMapOf(
            "name"         to user.name,
            "email"        to user.email,
            "syncStatus"   to user.syncStatus.name,
            "isSubscribed" to user.isSubscribed
        )

        // 3) grava (ou atualiza) o documento com ID = authUid
        firestore
            .collection("users")
            .document(authUid)
            .set(userMap, SetOptions.merge())
            .await()

        // 4) devolve o UID para que o repository atualize o Room
        return authUid
    }

    /**
     * Atualiza campos específicos do usuário no Firestore.
     * @param updatedData mapa de pares campo→valor a atualizar.
     */
    suspend fun updateUser(updatedData: Map<String, Any>) {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        usersCollection
            .document(authUid)
            .set(updatedData, SetOptions.merge())
            .await()
    }

    /**
     * Remove o registro do usuário no Firestore usando o Auth‐UID.
     */
    suspend fun deleteUser() {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        usersCollection
            .document(authUid)
            .delete()
            .await()
    }

    /**
     * Busca um usuário por e-mail na nuvem e alinha o firebaseDocId local.
     */
    suspend fun getUserByEmail(email: String): User? {
        val query = usersCollection
            .whereEqualTo("email", email)
            .get()
            .await()

        val doc = query.documents.firstOrNull() ?: return null
        val user = doc.toObject(User::class.java)!!
        user.firebaseDocId = doc.id
        return user
    }

    /**
     * Lê diretamente o documento cujo ID é o Auth‐UID.
     */
    suspend fun getUserById(): User? {
        val authUid = FirebaseAuth.getInstance().currentUser!!.uid
        val docSnap = usersCollection
            .document(authUid)
            .get()
            .await()

        val user = docSnap.toObject(User::class.java)
        user?.firebaseDocId = authUid
        return user
    }
}
