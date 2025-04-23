package com.nate.autofinance.domain.models

import com.nate.autofinance.domain.models.SyncStatus
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
    val endDate: Date? = null,           // Permite período aberto
    val isSelected: Boolean = false,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val userId: Int?,                     // Alterado para Int para garantir consistência com User
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val firebaseDocUserId: String? = null, // ID do usuário no Firebase
    val firebaseDocId: String? = null
)
