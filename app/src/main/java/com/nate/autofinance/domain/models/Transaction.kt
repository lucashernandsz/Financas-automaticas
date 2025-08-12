package com.nate.autofinance.domain.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transaction",
    foreignKeys = [
        ForeignKey(
            entity = FinancialPeriod::class,
            parentColumns = ["id"],
            childColumns = ["financialPeriodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["financialPeriodId"])]
)
data class  Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Date,
    val amount: Double,
    val description: String,
    val category: String,
    val userId: Int? = null,
    val financialPeriodId: Int,
    val imported: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val firebaseDocFinancialPeriodId: String? = null,
    val firebaseDocUserId: String? = null,
    var firebaseDocId: String? = null
) {
    /** Construtor sem argumentos exigido pelo Firestore via reflexão */
    @Suppress("unused")
    constructor() : this(
        id                         = 0,
        date                       = Date(),
        amount                     = 0.0,
        description                = "",
        category                   = "",
        userId                     = null,
        financialPeriodId          = 0,
        imported                   = false,
        syncStatus                 = SyncStatus.PENDING,
        firebaseDocFinancialPeriodId = null,
        firebaseDocUserId          = null,
        firebaseDocId              = null
    )
}
