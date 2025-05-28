package com.nate.autofinance.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nate.autofinance.ServiceLocator

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.i("SyncWorker", "doWork() chamado")
        ServiceLocator.syncManager.syncAll()
        return Result.success()
    }
}
