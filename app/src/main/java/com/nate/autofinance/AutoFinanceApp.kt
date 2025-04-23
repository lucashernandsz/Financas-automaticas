// app/src/main/java/com/nate/autofinance/AutoFinanceApp.kt
package com.nate.autofinance

import SyncWorker
import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.nate.autofinance.ServiceLocator
import java.util.concurrent.TimeUnit

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

            createDefaultPeriodUseCase
            createNewPeriodUseCase

            fetchTransactionsForSelectedPeriodUseCase
            getTransactionByIdUseCase
            addTransactionUseCase
            editTransactionUseCase
            deleteTransactionUseCase

        }

        val syncReq = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "auto_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncReq
            )

    }
}
