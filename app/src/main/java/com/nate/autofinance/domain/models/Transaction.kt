package com.nate.autofinance.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Date,
    val amount: Double,
    val description: String,
    val category: String,
    val userId: String? = null,
    val financialPeriodId: Int? = null,
    val imported: Boolean = false,

)
