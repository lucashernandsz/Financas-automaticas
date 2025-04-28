import android.content.Context
import android.util.Log
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
    companion object {
        private const val TAG = "SyncManager"
    }

    suspend fun syncAll() {
        Log.i(TAG, ">>> Iniciando syncAll")

        // 1) PUSH: transações pendentes
        txDao.getPendingTransactions().forEach { tx ->
            if (tx.firebaseDocId == null) {
                Log.i(TAG, "  → push NOVA tx localId=${tx.id}")
                txRepo.addTransaction(tx)
            } else {
                Log.i(TAG, "  → update tx remoteId=${tx.firebaseDocId}")
                txRepo.updateTransaction(tx)
            }
        }

        // 2) PUSH: períodos pendentes
        periodDao.getPendingPeriods().forEach { p ->
            if (p.firebaseDocId == null) {
                Log.i(TAG, "  → push NOVO period localId=${p.id}")
                periodRepo.addFinancialPeriod(p)
            } else {
                Log.i(TAG, "  → update period remoteId=${p.firebaseDocId}")
                periodRepo.updateFinancialPeriod(p)
            }
        }

        // 3) PULL: só se tivermos usuário local e firebaseDocId
        val localUserId = session.getUserId(context)
        if (localUserId == null) {
            Log.w(TAG, "Nenhum usuário local logado. Pulll remoto SKIPPED.")
        } else {
            val localUser = userRepo.getUserById(localUserId)
            val firebaseUserId = localUser?.firebaseDocId
            if (firebaseUserId.isNullOrEmpty()) {
                Log.w(TAG, "Usuário local sem firebaseDocId. Pull remoto SKIPPED.")
            } else {
                // 4) PULL: períodos remotos
                try {
                    Log.i(TAG, "  → pull periods para firebaseUserId=$firebaseUserId")
                    val remotePeriods = periodRepo.fetchRemotePeriods(firebaseUserId)
                    periodDao.insertAll(
                        remotePeriods.map { it.copy(syncStatus = SyncStatus.SYNCED) }
                    )
                } catch (ex: Exception) {
                    Log.e(TAG, "Erro ao puxar períodos remotos", ex)
                }

                // 5) Constroi mapa firebaseDocId → id local
                val periodIdMap: Map<String, Int> = periodDao
                    .getPeriodsByUserId(localUserId)
                    .mapNotNull { p -> p.firebaseDocId?.let { fid -> fid to p.id } }
                    .toMap()

                Log.i(TAG, "  → pull txs para firebaseUserId=$firebaseUserId")
                val remoteTxs = txRepo.fetchRemoteTransactions(firebaseUserId)
                Log.i(TAG, "    × remoteTxs.size=${remoteTxs.size}")


                // 6) PULL: transações remotas
                try {
                    Log.i(TAG, "  → pull txs para firebaseUserId=$firebaseUserId")
                    val remoteTxs = txRepo.fetchRemoteTransactions(firebaseUserId)
                    val toInsert = remoteTxs.mapNotNull { remote ->
                        val localPeriodId = remote.firebaseDocFinancialPeriodId
                            ?.let { periodIdMap[it] }
                        if (localPeriodId == null) {
                            Log.w(
                                TAG,
                                "    × sem período local para tx remoteId=${remote.firebaseDocId}"
                            )
                            null
                        } else {
                            remote.copy(
                                id = 0,  // Room autogerará
                                financialPeriodId = localPeriodId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        }
                    }
                    txDao.insertAll(toInsert)
                } catch (ex: Exception) {
                    Log.e(TAG, "Erro ao puxar transações remotas", ex)
                }
            }
        }

        Log.i(TAG, "<<< syncAll concluído")
    }
}


