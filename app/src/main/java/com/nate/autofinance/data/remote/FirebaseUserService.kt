package com.nate.autofinance.data.remote

import com.google.firebase.firestore.FirebaseFirestore
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
        val userMap = hashMapOf(
            "name" to user.name,
            "email" to user.email,
            "syncStatus" to user.syncStatus.name,
            "isSubscribed" to user.isSubscribed
        )
        val documentRef = usersCollection.add(userMap).await()
        return documentRef.id
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
    suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = usersCollection.whereEqualTo("email", email).get().await()
        val document = querySnapshot.documents.firstOrNull()
        val user = document?.toObject(User::class.java)
        if (user != null) {
            user.firebaseDocId = document.id
        }
        return user
    }
}
