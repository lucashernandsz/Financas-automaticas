package com.nate.autofinance

import FetchTransactionsForSelectedPeriodUseCase
import android.annotation.SuppressLint
import android.content.Context
import com.nate.autofinance.data.local.AppDatabase
import com.nate.autofinance.data.remote.FirebasePeriodService
import com.nate.autofinance.data.remote.FirebaseTransactionService
import com.nate.autofinance.data.remote.FirebaseUserService
import com.nate.autofinance.data.repository.PeriodRepository
import com.nate.autofinance.data.repository.TransactionRepository
import com.nate.autofinance.data.repository.UserRepository
import com.nate.autofinance.data.sync.SyncManager
import com.nate.autofinance.domain.usecase.period.*
import com.nate.autofinance.domain.usecase.subscription.ToggleSubscriptionUseCase
import com.nate.autofinance.domain.usecase.transaction.*
import com.nate.autofinance.domain.usecases.period.CreatePredefinedPeriodsUseCase
import com.nate.autofinance.utils.SessionManager

@SuppressLint("StaticFieldLeak")
object ServiceLocator {
    val context: Context = AutoFinanceApp.instance
    val sessionManager: SessionManager = SessionManager

    // Room
    private val database by lazy { AppDatabase.getDatabase(context) }
    internal val transactionDao by lazy { database.transactionDao() }
    internal val financialPeriodDao by lazy { database.financialPeriodDao() }
    private val userDao by lazy { database.userDao() }

    // Firebase
    internal val transactionService by lazy { FirebaseTransactionService() }
    internal val periodService by lazy { FirebasePeriodService() }
    private val userService by lazy { FirebaseUserService(
        session = sessionManager,
        userDao = userDao
    ) }

    // Repositórios
    val transactionRepository by lazy {
        TransactionRepository(transactionDao, transactionService)
    }
    val periodRepository by lazy {
        PeriodRepository(financialPeriodDao, periodService)
    }
    val userRepository by lazy {
        UserRepository(userDao, userService)
    }

    // Use cases: períodos
    val createDefaultPeriodUseCase by lazy {
        CreateDefaultPeriodUseCase(
            periodRepository,
            sessionManager,
            context
        )
    }
    val createNewPeriodUseCase by lazy {
        CreateNewPeriodUseCase(
            periodRepository,
            sessionManager,
            context
        )
    }
    val getAllPeriodsForUserUseCase by lazy {
        GetAllPeriodsForUserUseCase(
            periodRepository,
            sessionManager,
            context
        )
    }
    val selectPeriodUseCase by lazy {
        SelectPeriodUseCase(
            periodRepository,
            sessionManager,
            context
        )
    }
    val deletePeriodsUseCase by lazy {
        DeletePeriodsUseCase(
            periodRepository,
            sessionManager,
            context
        )
    }

    // Use cases: transações
    val fetchTransactionsForSelectedPeriodUseCase by lazy {
        FetchTransactionsForSelectedPeriodUseCase(
            periodRepository,
            transactionRepository,
            sessionManager,
            context
        )
    }
    val getTransactionByIdUseCase by lazy {
        GetTransactionByIdUseCase(
            transactionRepository,
            sessionManager,
            context
        )
    }
    val addTransactionUseCase by lazy {
        AddTransactionUseCase(
            transactionRepository,
            sessionManager,
            context,
            getCurrentActivePeriod = GetCurrentActivePeriodUseCase(
                periodRepository,
                sessionManager,
                context
            )
        )
    }
    val editTransactionUseCase by lazy {
        EditTransactionUseCase(
            transactionRepository,
        )
    }
    val deleteTransactionUseCase by lazy {
        DeleteTransactionUseCase(
            transactionRepository,
        )
    }

    val toggleSubscriptionUseCase by lazy{
        ToggleSubscriptionUseCase(
            userRepository,
            sessionManager,
            context
        )
    }

    val createPredefinedPeriodsUseCase by lazy {
        CreatePredefinedPeriodsUseCase(
            periodRepository,
            sessionManager,
            context
        )
    }



    val syncManager by lazy {
        SyncManager(        )
    }
}
