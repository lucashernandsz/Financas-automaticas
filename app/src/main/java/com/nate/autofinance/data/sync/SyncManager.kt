import android.content.Context
import com.nate.autofinance.data.local.FinancialPeriodDao
import com.nate.autofinance.data.local.TransactionDao
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.domain.models.SyncStatus
import com.nate.autofinance.utils.SessionManager

// app/src/main/java/com/nate/autofinance/data/sync/SyncManager.kt

class SyncManager(
    private val txRepo: TransactionRepository,
    private val periodRepo: PeriodRepository,
    private val userRepo: UserRepository,
    private val txDao: TransactionDao,
    private val periodDao: FinancialPeriodDao,
    private val session: SessionManager,
    private val context: Context
) {
    suspend fun syncAll() {
        // PUSH: envia pendentes
        txDao.getPendingTransactions().forEach { tx ->
            if (tx.firebaseDocId == null) txRepo.addTransaction(tx)
            else                          txRepo.updateTransaction(tx)
        }
        periodDao.getPendingPeriods().forEach { p ->
            if (p.firebaseDocId == null) periodRepo.addFinancialPeriod(p)
            else                         periodRepo.updateFinancialPeriod(p)
        }

        // PULL: baixa do Firebase via métodos públicos no repo
        val localUserId    = session.getUserId(context)!!
        val firebaseUserId = userRepo.getUserById(localUserId)!!.firebaseDocId!!

        val remotePeriods = periodRepo.fetchRemotePeriods(firebaseUserId)
        periodDao.insertAll(remotePeriods.map { it.copy(syncStatus = SyncStatus.SYNCED) })

        val remoteTxs = txRepo.fetchRemoteTransactions(firebaseUserId)
        txDao.insertAll(remoteTxs.map { it.copy(syncStatus = SyncStatus.SYNCED) })
    }
}

