import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nate.autofinance.ServiceLocator

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        ServiceLocator.syncManager.syncAll()
        return Result.success()
    }
}
