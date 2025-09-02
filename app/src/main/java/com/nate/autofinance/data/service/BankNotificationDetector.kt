package com.nate.autofinance.data.service

object BankNotificationDetector {

    private val bankPackageNames = listOf(
        "itau",
        "bradesco",
        "com.nu",
        "santander",
        "caixa",
        "bb",
        "inter",
        "next",
        "c6",
        "picpay",
        "mercadopago",
        "uol.ps.myaccount",
        "sicoob"
    )

    fun isBankNotification(packageName: String): Boolean {
        return bankPackageNames.any { packageName.contains(it, ignoreCase = true) }
    }

}