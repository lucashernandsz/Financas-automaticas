package com.nate.autofinance.domain.models

import com.nate.autofinance.domain.models.SyncStatus
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
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Date,
    val amount: Double,
    val description: String,
    val category: String,
    val userId: Int? = null,             // Se necessário; você pode remover se a associação for indireta via FinancialPeriod
    val financialPeriodId: Int,          // Não nulo: cada transação deve pertencer a um período
    val imported: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val firebaseDocFinancialPeriodId: String? = null,
    val firebaseDocUserId: String? = null,
    var firebaseDocId: String? = null
)
