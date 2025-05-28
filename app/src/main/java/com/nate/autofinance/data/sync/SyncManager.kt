package com.nate.autofinance.data.sync

import android.content.Context
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.data.remote.FirebasePeriodService
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.data.remote.FirebaseUserService
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.utils.SessionManager

class SyncManager(
    private val userService: FirebaseUserService,
    private val periodService: FirebasePeriodService,
    private val transactionService: FirebaseTransactionService,
    private val userRepo: UserRepository,
    private val periodDao: FinancialPeriodDao,
    private val txDao: TransactionDao,
    private val session: SessionManager,
    private val context: Context
) {
    suspend fun syncAll() {
        // 1) Garante que o usuário exista/remoto e obtém sempre o mesmo Auth-UID
        val authUid = userService.getOrCreateUser(context)

        // 2) PUSH: envia períodos pendentes
        periodDao.getPendingPeriods().forEach { period ->
            try {
                val docId = period.firebaseDocId
                    ?: throw IllegalStateException("firebaseDocId não definido para este período")

                periodService.updateFinancialPeriod(
                    docId,
                    mapOf(
                        "startDate"     to period.startDate,
                        "endDate"       to period.endDate,
                        "isSelected"    to period.isSelected,
                        "totalIncome"   to period.totalIncome,
                        "totalExpenses" to period.totalExpenses
                    )
                )
                periodDao.update(
                    period.copy(
                        firebaseDocId = docId,
                        syncStatus     = SyncStatus.SYNCED
                    )
                )
            } catch (e: Exception) {
                periodDao.update(
                    period.copy(syncStatus = SyncStatus.FAILED)
                )
            }
        }

        // 3) PUSH: envia transações pendentes
        txDao.getPendingTransactions().forEach { tx ->
            try {
                // Dentro do loop de push de transações, antes de chamar updateTransaction:
                val docId = tx.firebaseDocId
                    ?: throw IllegalStateException("firebaseDocId não definido para esta transação")

                transactionService.updateTransaction(
                    docId,
                    mapOf(
                        "date"        to tx.date,
                        "amount"      to tx.amount,
                        "description" to tx.description,
                        "category"    to tx.category,
                        "imported"    to tx.imported
                    )
                )
                txDao.update(
                    tx.copy(
                        firebaseDocId = docId,
                        syncStatus     = SyncStatus.SYNCED
                    )
                )
            } catch (e: Exception) {
                txDao.update(
                    tx.copy(syncStatus = SyncStatus.FAILED)
                )
            }
        }

        // 4) PULL: baixa todos os períodos do usuário remoto e grava no Room
        val localUserId = session.getUserId(context)!!
        val remotePeriods = periodService.getFinancialPeriodsForUser()
        periodDao.insertAll(
            remotePeriods.map {
                it.copy(
                    userId     = localUserId,
                    syncStatus = SyncStatus.SYNCED
                )
            }
        )

        // 5) PULL: baixa todas as transações do usuário remoto e grava no Room
        val remoteTxs = transactionService.getTransactionsForUser()
        txDao.insertAll(
            remoteTxs.map {
                it.copy(
                    userId     = localUserId,
                    syncStatus = SyncStatus.SYNCED
                )
            }
        )
    }
}
