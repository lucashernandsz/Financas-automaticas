package com.nate.autofinance.data.service

object PurchaseDetector {

    private val purchaseKeywords = listOf(
        "comprar",
        "comprou",
        "compra",
        "débito",
        "débito automático",
        "crédito",
        "R$",
        "pagamento",
        "pago",
        "pagar",
    )

    fun isPurchaseNotification(content: String): Boolean {
        val contentLower = content.lowercase()
        return purchaseKeywords.any { contentLower.contains(it) }
    }
}