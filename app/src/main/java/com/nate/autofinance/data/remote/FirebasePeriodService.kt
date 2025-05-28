package com.nate.autofinance.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.domain.models.FinancialPeriod
import kotlinx.coroutines.tasks.await

class FirebasePeriodService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val periodsCollection = firestore.collection("financialPeriods")

    /**
     * Adiciona um novo período ao Firestore e devolve o ID do documento criado.
     * Usa o Auth-UID como chave em "firebaseDocUserId".
     */
    suspend fun addFinancialPeriod(period: FinancialPeriod): String {
        val authUid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        val periodMap = hashMapOf(
            "startDate"          to period.startDate,
            "endDate"            to period.endDate,
            "isSelected"         to period.isSelected,
            "totalIncome"        to period.totalIncome,
            "totalExpenses"      to period.totalExpenses,
            // removido "userId" local: não mais necessário
            "syncStatus"         to period.syncStatus.name,
            "firebaseDocUserId"  to authUid
        )

        val documentRef = periodsCollection.add(periodMap).await()  // gera ID próprio
        return documentRef.id
    }

    /**
     * Atualiza campos do período existente, usando o documentId retornado em addFinancialPeriod().
     */
    suspend fun updateFinancialPeriod(documentId: String, updatedData: Map<String, Any>) {
        periodsCollection
            .document(documentId)
            .update(updatedData)
            .await()
    }

    /**
     * Exclui o período com base no seu ID de documento.
     */
    suspend fun deleteFinancialPeriod(documentId: String) {
        periodsCollection
            .document(documentId)
            .delete()
            .await()
    }

    /**
     * Busca todos os períodos desse usuário (Auth-UID).
     */
    suspend fun getFinancialPeriodsForUser(): List<FinancialPeriod> {
        val authUid = FirebaseAuth.getInstance().currentUser
            ?.uid
            ?: throw IllegalStateException("Nenhum usuário autenticado")

        return periodsCollection
            .whereEqualTo("firebaseDocUserId", authUid)
            .get()
            .await()
            .map { it.toObject(FinancialPeriod::class.java) }
    }
}
