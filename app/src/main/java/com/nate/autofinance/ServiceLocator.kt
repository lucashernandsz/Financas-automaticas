// app/src/main/java/com/nate/autofinance/ServiceLocator.kt
package com.nate.autofinance

import FetchTransactionsForSelectedPeriodUseCase
import SyncManager
import com.nate.autofinance.data.local.AppDatabase
import com.nate.autofinance.data.remote.FirebasePeriodService
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.data.remote.FirebaseUserService
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.domain.usecases.period.CreateDefaultPeriodUseCase
import com.nate.autofinance.domain.usecases.period.CreateNewPeriodUseCase
import com.nate.autofinance.domain.usecases.transaction.*
import com.nate.autofinance.utils.SessionManager
import kotlin.getValue

object ServiceLocator {
    // pega o contexto da aplicação
    private val context = AutoFinanceApp.instance

    // Room
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val transactionDao by lazy { database.transactionDao() }
    private val financialPeriodDao by lazy { database.financialPeriodDao() }
    private val userDao by lazy { database.userDao() }

    // Firebase
    private val transactionService by lazy { FirebaseTransactionService() }
    private val periodService by lazy { FirebasePeriodService() }
    private val userService by lazy { FirebaseUserService() }

    // Repositórios
    val transactionRepository by lazy {
        TransactionRepository(transactionDao, transactionService)
    }
    val periodRepository by lazy {
        PeriodRepository(financialPeriodDao, periodService)
    }

    val userRepository by lazy {
        UserRepository(
            userDao = userDao,
            firebaseUserService = userService,
        )
    }

    // Use cases: períodos
    val createDefaultPeriodUseCase by lazy {
        CreateDefaultPeriodUseCase(periodRepository)
    }
    val createNewPeriodUseCase by lazy {
        CreateNewPeriodUseCase(periodRepository)
    }

    // Use cases: transações
    val fetchTransactionsForSelectedPeriodUseCase by lazy {
        FetchTransactionsForSelectedPeriodUseCase(
            periodRepository,
            transactionRepository,
            SessionManager,
            context
        )
    }
    val getTransactionByIdUseCase by lazy {
        GetTransactionByIdUseCase(transactionRepository)
    }

    val addTransactionUseCase by lazy {
        AddTransactionUseCase(
            transactionRepository,
            periodRepository,
            SessionManager,
            context
        )
    }
    val editTransactionUseCase by lazy {
        EditTransactionUseCase(
            transactionRepository,
            periodRepository,
            SessionManager,
            context
        )
    }
    val deleteTransactionUseCase by lazy {
        DeleteTransactionUseCase(
            transactionRepository,
            periodRepository,
            SessionManager,
            context
        )
    }

    val syncManager by lazy {
        SyncManager(
            txRepo     = transactionRepository,
            periodRepo = periodRepository,
            userRepo   = userRepository,
            txDao      = transactionDao,
            periodDao  = financialPeriodDao,
            session    = SessionManager,
            context    = context
        )
    }
}
