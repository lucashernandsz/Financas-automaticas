package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.domain.models.User
import kotlinx.coroutines.tasks.await

/**
 * Serviço responsável pelas operações de persistência dos dados de usuários no Firestore.
 */
class FirebaseUserService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Adiciona um novo usuário na coleção "users" do Firestore.
     * Cria um mapa com os campos relevantes (não incluímos firebaseDocId, pois o Firebase gerará o ID).
     *
     * @param user Instância de [User] com os dados a serem salvos.
     * @return O ID do documento criado, ou null se a operação falhar.
     */
    suspend fun addUser(user: User): String? {
        val uid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val userMap = hashMapOf(
            "name" to user.name,
            "email" to user.email,
            "syncStatus" to SyncStatus.SYNCED.name,
            "isSubscribed" to user.isSubscribed
        )
        usersCollection
            .document(uid)
            .set(userMap)
            .await()

        return uid
    }

    /**
     * Atualiza os dados de um usuário existente com base no ID do documento.
     *
     * @param documentId O identificador do documento no Firestore.
     * @param updatedData Um mapa contendo os campos a serem atualizados.
     */
    suspend fun updateUser(documentId: String, updatedData: Map<String, Any>) {
        usersCollection.document(documentId).update(updatedData).await()
    }

    /**
     * Remove um usuário da coleção com base no ID do documento.
     *
     * @param documentId O identificador do documento a ser removido.
     */
    suspend fun deleteUser(documentId: String) {
        usersCollection.document(documentId).delete().await()
    }

    /**
     * Busca e retorna um usuário com base no email fornecido.
     * Caso o usuário seja encontrado, atualiza o campo firebaseDocId com o ID do documento.
     *
     * @param email O email a ser pesquisado.
     * @return Uma instância de [User], ou null se não houver correspondência.
     */

    suspend fun getUserById(uid: String): User? {
        val snap = usersCollection.document(uid)
            .get()
            .await()
        if (!snap.exists()) return null

        // mapeia manualmente cada campo
        val data = snap.data ?: return null
        val name         = data["name"]         as? String ?: return null
        val email        = data["email"]        as? String ?: return null
        val rawStatus    = data["syncStatus"]   as? String ?: SyncStatus.PENDING.name
        val syncStatus   = try { SyncStatus.valueOf(rawStatus) }
        catch(_:Exception) { SyncStatus.PENDING }
        val isSubscribed = data["isSubscribed"] as? Boolean ?: false

        return User(
            id            = 0,           // será gerado pelo Room
            name          = name,
            email         = email,
            syncStatus    = syncStatus,
            isSubscribed  = isSubscribed,
            firebaseDocId = uid
        )
    }

    suspend fun getUserByEmail(email: String): User? {
        val snaps = usersCollection
            .whereEqualTo("email", email)
            .get()
            .await()
        val doc = snaps.documents.firstOrNull() ?: return null

        val data = doc.data ?: return null
        val name         = data["name"]         as? String ?: return null
        val rawStatus    = data["syncStatus"]   as? String ?: SyncStatus.PENDING.name
        val syncStatus   = try { SyncStatus.valueOf(rawStatus) }
        catch(_:Exception) { SyncStatus.PENDING }
        val isSubscribed = data["isSubscribed"] as? Boolean ?: false

        return User(
            id            = 0,
            name          = name,
            email         = email,
            syncStatus    = syncStatus,
            isSubscribed  = isSubscribed,
            firebaseDocId = doc.id
        )
    }
}
