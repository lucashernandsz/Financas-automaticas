package com.nate.autofinance.data.repository

import android.util.Log
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.remote.FirebasePeriodService
import com.nate.autofinance.domain.models.FinancialPeriod
import com.nate.autofinance.domain.models.SyncStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodRepository(
    private val periodDao: FinancialPeriodDao,
    private val firebasePeriodService: FirebasePeriodService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val TAG = "FinancialPeriodRepo"
    }

    suspend fun addFinancialPeriod(period: FinancialPeriod) = withContext(ioDispatcher) {
        // 1. Insere o período localmente e captura o ID gerado pelo Room.
        val localId = periodDao.insert(period)

        try {
            // 2. Envia o período para o Firebase e captura o firebaseDocId gerado.
            val firebaseDocId = firebasePeriodService.addFinancialPeriod(period)
            if (firebaseDocId != null) {
                Log.i(TAG, "FinancialPeriod sent to Firebase with id: $firebaseDocId")
                val updatedPeriod = period.copy(
                    id = localId.toInt(),          // Garante que o ID local seja o mesmo
                    firebaseDocId = firebaseDocId, // Atualiza o campo firebaseDocId
                    syncStatus = SyncStatus.SYNCED // Marca como sincronizado
                )
                periodDao.update(updatedPeriod)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error sending FinancialPeriod to Firebase", ex)
            // Em caso de falha, marca o syncStatus como FAILED para nova tentativa de sincronização.
            val updatedPeriod = period.copy(
                id = localId.toInt(),
                syncStatus = SyncStatus.FAILED
            )
            periodDao.update(updatedPeriod)
        }
    }

    suspend fun updateFinancialPeriod(period: FinancialPeriod) = withContext(ioDispatcher) {
        // Atualiza primeiro o registro local.
        periodDao.update(period)
        try {
            period.firebaseDocId?.let { docId ->
                val updatedData: Map<String, Any?> = mapOf(
                    "startDate" to period.startDate,
                    "endDate" to period.endDate,
                    "totalIncome" to period.totalIncome,
                    "totalExpenses" to period.totalExpenses,
                    "userId" to period.userId,
                    "syncStatus" to period.syncStatus.name,
                    "firebaseDocUserId" to period.firebaseDocUserId
                )
                firebasePeriodService.updateFinancialPeriod(docId, updatedData as Map<String, Any>)
                Log.i(TAG, "FinancialPeriod updated in Firebase: $docId")
            }
            // Se a operação remota tiver sucesso, atualiza o syncStatus para SYNCED localmente.
            val updatedPeriod = period.copy(syncStatus = SyncStatus.SYNCED)
            periodDao.update(updatedPeriod)
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating FinancialPeriod in Firebase", ex)
            // Em caso de erro, marca o período como FAILED.
            val updatedPeriod = period.copy(syncStatus = SyncStatus.FAILED)
            periodDao.update(updatedPeriod)
        }
    }

    suspend fun deleteFinancialPeriod(period: FinancialPeriod) = withContext(ioDispatcher) {
        periodDao.delete(period)
        try {
            period.firebaseDocId?.let { docId ->
                firebasePeriodService.deleteFinancialPeriod(docId)
                Log.i(TAG, "FinancialPeriod deleted from Firebase: $docId")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error deleting FinancialPeriod from Firebase", ex)
        }
    }

    suspend fun getFinancialPeriodsForUser(userId: Long?): List<FinancialPeriod> = withContext(ioDispatcher) {
        periodDao.getPeriodsByUserId(userId)
    }

    suspend fun getSelectedPeriodForUser(userId: Long): FinancialPeriod? = withContext(ioDispatcher) {
        periodDao.getSelectedPeriodByUserId(userId)
    }

    suspend fun getPeriodForUser(userId: Long, periodId: Int): FinancialPeriod? = withContext(ioDispatcher) {
        periodDao.getPeriodByUserIdAndPeriodId(userId, periodId)
    }

    suspend fun getPeriodsForUser(userId: Long): List<FinancialPeriod> = withContext(ioDispatcher) {
        periodDao.getPeriodsByUserId(userId)
    }

}
