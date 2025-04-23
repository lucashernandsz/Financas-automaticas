// app/src/main/java/com/nate/autofinance/AutoFinanceApp.kt
package com.nate.autofinance

import android.app.Application
import com.google.firebase.FirebaseApp
import com.nate.autofinance.ServiceLocator

class AutoFinanceApp : Application() {

    companion object {
        lateinit var instance: AutoFinanceApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseApp.initializeApp(this)

        ServiceLocator.apply {
            userRepository
            transactionRepository
            periodRepository

            // casos de uso de períodos
            createDefaultPeriodUseCase
            createNewPeriodUseCase

            // casos de uso de transações
            fetchTransactionsForSelectedPeriodUseCase
            getTransactionByIdUseCase
            addTransactionUseCase
            editTransactionUseCase
            deleteTransactionUseCase
        }
    }
}
