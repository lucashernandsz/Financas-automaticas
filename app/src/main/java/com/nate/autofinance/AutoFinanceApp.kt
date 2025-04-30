// app/src/main/java/com/nate/autofinance/AutoFinanceApp.kt
package com.nate.autofinance

import SyncWorker
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.nate.autofinance.ServiceLocator
import java.util.concurrent.TimeUnit

class AutoFinanceApp : Application() {

    companion object {
        lateinit var instance: AutoFinanceApp
            private set
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseApp.initializeApp(this)

        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }

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
            toggleSubscriptionUseCase

        }

        val channel = NotificationChannel(
            "autofinance_channel",
            "AutoFinance Ativo",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

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
