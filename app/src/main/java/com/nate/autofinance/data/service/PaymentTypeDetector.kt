package com.nate.autofinance.data.categorization

object PaymentTypeDetector {

    fun isCredit(text: String): Boolean {
        return when {
            text.contains("crédito", ignoreCase = true) -> true

            text.contains("débito", ignoreCase = true) ||
                    text.contains("pix", ignoreCase = true) ||
                    text.contains("transferência", ignoreCase = true) ||
                    text.contains("boleto", ignoreCase = true) -> false

            else -> false
        }
    }
}
