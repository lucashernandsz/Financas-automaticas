package com.nate.autofinance.data.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.nate.autofinance.ServiceLocator
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.data.models.FinancialPeriod
import com.nate.autofinance.data.models.SyncStatus
import com.nate.autofinance.data.models.Transaction
import com.nate.autofinance.utils.SessionManager

class SyncManager(
) {


    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val periodDao: FinancialPeriodDao = ServiceLocator.financialPeriodDao
    private val transactionDao: TransactionDao = ServiceLocator.transactionDao
    private val session: SessionManager = ServiceLocator.sessionManager
    private val context: Context = ServiceLocator.context
    private val firebaseTransactionService: FirebaseTransactionService =
        ServiceLocator.transactionService
    private val firebasePeriodService = ServiceLocator.periodService
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser
        ?: throw IllegalStateException("Nenhum usuário autenticado")
    private val firebaseUserId: String
        get() = firebaseUser.uid
    private val localUserId: Int = session.getUserId(context)
        ?: throw IllegalStateException("Usuário local não encontrado")

    suspend fun syncAll() {
        pushPendingPeriods()
        pushPendingTransactions()
        pullRemotePeriods()
        pullRemoteTransactions()
    }

    suspend fun pushPendingPeriods() {
        val pendingPeriods = periodDao.getPendingPeriods()
        pendingPeriods.forEach { period -> pushOnePendingPeriod(period) }
    }

    suspend fun pushOnePendingPeriod(period: FinancialPeriod) {
        try {
            val firebaseId = if (period.firebaseDocId.isNullOrBlank()) {
                firebasePeriodService.add(period)
            } else {
                firebasePeriodService.update(period.firebaseDocId, period.toFirestoreMap())
                period.firebaseDocId
            }

            val finalPeriod = period.copy(firebaseDocId = firebaseId).asSynced()
            periodDao.update(finalPeriod)

        } catch (e: Exception) {
            periodDao.update(period.asFailed())
        }
    }

    suspend fun pushPendingTransactions() {
        val pendingTransactions = transactionDao.getPendingTransactions()
        pendingTransactions.forEach { transaction -> pushOnePendingTransaction(transaction) }
    }

    suspend fun pushOnePendingTransaction(transaction: Transaction) {
        try {
            val firebaseId = if (transaction.firebaseDocId.isNullOrBlank()) {
                firebaseTransactionService.insert(transaction)
            } else {
                firebaseTransactionService.update(
                    transaction.firebaseDocId!!,
                    transaction.toFirestoreMap()
                )
                transaction.firebaseDocId
            }

            val finalTransaction = transaction.copy(firebaseDocId = firebaseId).asSynced()
            transactionDao.update(finalTransaction)

        } catch (e: Exception) {
            transactionDao.update(transaction.asFailed())
        }

    }

    suspend fun pullRemoteTransactions () {
        val remoteTransactions = firebaseTransactionService.getTransactionsForUser()
        remoteTransactions.forEach { remoteTransaction -> pullRemoteTransaction(remoteTransaction)}

    }

    suspend fun pullRemoteTransaction(remoteTransaction: Transaction) {
        val localTransaction = transactionDao.getTransactionByFirebaseDocId(remoteTransaction.firebaseDocId.toString())
        if (localTransaction == null) {
            transactionDao.insert(remoteTransaction.copy(
                userId = localUserId,
                syncStatus = SyncStatus.SYNCED
            ))
        } else {
            transactionDao.update(remoteTransaction.copy(
                id = localTransaction.id,
                userId = localUserId,
                syncStatus = SyncStatus.SYNCED
            ))
        }
    }

    suspend fun pullRemotePeriods() {
        val remotePeriods = firebasePeriodService.getFinancialPeriodsForUser()
        remotePeriods.forEach { remotePeriod -> pullRemotePeriod(remotePeriod) }
    }

    suspend fun pullRemotePeriod(remotePeriod: FinancialPeriod) {
        val localPeriod = periodDao.getPeriodByFirebaseDocId(remotePeriod.firebaseDocId.toString())
        if (localPeriod == null) {
            periodDao.insert(remotePeriod.copy(
                userId = localUserId,
                syncStatus = SyncStatus.SYNCED
            ))
        } else {
            periodDao.update(remotePeriod.copy(
                id = localPeriod.id,
                userId = localUserId,
                syncStatus = SyncStatus.SYNCED
            ))
        }
    }


}
