package com.nate.autofinance.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "financial_period",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class FinancialPeriod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Date?,
    val endDate: Date? = null,
    val isSelected: Boolean = false,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val userId: Int?,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val firebaseDocUserId: String? = null,
    val firebaseDocId: String? = null
) {
    /** Construtor sem argumentos exigido pelo Firestore via reflexão */
    @Suppress("unused")
    constructor() : this(
        id               = 0,
        startDate        = null,
        endDate          = null,
        isSelected       = false,
        totalIncome      = 0.0,
        totalExpenses    = 0.0,
        userId           = null,
        syncStatus       = SyncStatus.PENDING,
        firebaseDocUserId = null,
        firebaseDocId     = null
    )
}
