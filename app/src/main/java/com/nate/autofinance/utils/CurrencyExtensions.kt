package com.nate.autofinance.utils

import java.text.NumberFormat
import java.util.Locale

fun Double.toBrazilianCurrency(): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(this)
