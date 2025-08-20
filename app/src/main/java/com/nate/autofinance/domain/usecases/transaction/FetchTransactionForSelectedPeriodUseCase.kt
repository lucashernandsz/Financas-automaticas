import android.content.Context
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.utils.SessionManager

class FetchTransactionsForSelectedPeriodUseCase(
    private val periodRepo: PeriodRepository,
    private val transactionRepo: TransactionRepository,
    private val session: SessionManager,
    private val context: Context
) {
    suspend operator fun invoke(): List<Transaction> {
        val userId = session.getUserId(context)
            ?: throw IllegalStateException("Nenhum usuário logado")

        val selected = periodRepo.getSelectedPeriodForUser(userId)
            ?: throw IllegalStateException("Nenhum período selecionado")

        return transactionRepo.getTransactionsByPeriodId(selected.id)
    }
}
