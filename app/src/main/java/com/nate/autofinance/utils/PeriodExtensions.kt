package com.nate.autofinance.utils

import com.nate.autofinance.data.models.FinancialPeriod
import java.text.SimpleDateFormat
import java.util.*

fun FinancialPeriod.toLabel(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val inicio = startDate?.let { dateFormat.format(it) } ?: "?"
    val fim = endDate?.let { dateFormat.format(it) } ?: "hoje"

    return "De $inicio até $fim"
}
