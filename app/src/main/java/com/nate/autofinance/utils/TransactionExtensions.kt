package com.nate.autofinance.utils

import kotlin.math.absoluteValue

fun normalizeAmount(amount: Double, category: String): Double {
    return if (category == Categories.Income.name && amount < 0) {
        amount.absoluteValue
    } else {
        amount
    }
}