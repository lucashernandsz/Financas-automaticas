package com.nate.autofinance.data.sync

import com.nate.autofinance.data.models.FinancialPeriod
import com.nate.autofinance.data.models.SyncStatus
import com.nate.autofinance.data.models.Transaction

fun FinancialPeriod.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "startDate" to this.startDate,
        "endDate" to this.endDate,
        "isSelected" to this.isSelected,
        "totalIncome" to this.totalIncome,
        "totalExpenses" to this.totalExpenses,
        "syncStatus" to this.syncStatus.name,
        "firebaseDocUserId" to this.firebaseDocUserId
        // firebaseDocId é o ID do documento e não deve ser incluído nos dados do documento.
    )
}

fun Transaction.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "date" to this.date,
        "amount" to this.amount,
        "description" to this.description,
        "category" to this.category,
        "imported" to this.imported,
        "syncStatus" to this.syncStatus.name,
        "firebaseDocUserId" to this.firebaseDocUserId,
        "firebaseDocFinancialPeriodId" to this.firebaseDocFinancialPeriodId
        // firebaseDocId é o ID do documento e não deve ser incluído nos dados do documento.
    )
}

fun FinancialPeriod.asSynced(): FinancialPeriod {
    return this.copy(syncStatus = SyncStatus.SYNCED)
}

fun FinancialPeriod.asFailed(): FinancialPeriod {
    return this.copy(syncStatus = SyncStatus.FAILED)
}

fun FinancialPeriod.asPending(): FinancialPeriod {
    return this.copy(syncStatus = SyncStatus.PENDING)
}

fun Transaction.asSynced(): Transaction {
    return this.copy(syncStatus = SyncStatus.SYNCED)
}

fun Transaction.asFailed(): Transaction {
    return this.copy(syncStatus = SyncStatus.FAILED)
}

fun Transaction.asPending(): Transaction {
    return this.copy(syncStatus = SyncStatus.PENDING)
}