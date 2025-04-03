package com.nate.autofinance.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "financial_period")
data class FinancialPeriod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Date,
    val endDate: Date,
    val initialBalance: Double,
    val finalBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val userId: String
)
