package com.nate.autofinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.screens.login.LoginScreen
import com.nate.autofinance.ui.screens.register.RegisterScreen
import com.nate.autofinance.ui.screens.transactionList.TransactionListScreen
import com.nate.autofinance.ui.screens.transactionList.AddTransactionScreen
import com.nate.autofinance.ui.screens.transactionList.editTransaction.EditTransactionScreen
import com.nate.autofinance.ui.screens.settings.*
import com.nate.autofinance.ui.screens.settings.appSettings.AppSettingsScreen
import com.nate.autofinance.ui.screens.settings.financialPeriods.FinancialPeriodsScreen
import com.nate.autofinance.ui.screens.settings.info.PersonalInfoScreen
import com.nate.autofinance.ui.screens.settings.newFinancialPeriod.NewPeriodScreen
import com.nate.autofinance.ui.screens.settings.subscription.SubscriptionScreen
import com.nate.autofinance.ui.theme.AutofinanceTheme
import com.nate.autofinance.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Estado para o modo claro/escuro
            var darkMode by rememberSaveable { mutableStateOf(false) }
            AutofinanceTheme(darkTheme = darkMode) {
                MainApp(
                    darkMode = darkMode,
                    onToggleDarkMode = { darkMode = it }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login?snackbarMessage={snackbarMessage}"
    ) {
        // --- LOGIN ---
        composable(
            "login?snackbarMessage={snackbarMessage}",
            arguments = listOf(
                navArgument("snackbarMessage") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val snackbarMessage = backStackEntry.arguments?.getString("snackbarMessage")
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate("transactionList") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                snackbarMessage      = snackbarMessage
            )
        }

        // --- REGISTER ---
        composable("register") {
            val registerVm: RegisterViewModel = viewModel()
            RegisterScreen(
                viewModel         = registerVm,
                onRegisterSuccess = { msg ->
                    navController.navigate("login?snackbarMessage=$msg") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login?snackbarMessage=") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // --- TRANSACTION LIST ---
        composable("transactionList") {
            val txVm: TransactionViewModel = viewModel()
            TransactionListScreen(
                viewModel             = txVm,
                onDashboardClick      = { /* sem ação por enquanto */ },
                onTransactionsClick   = { /* sem ação por enquanto */ },
                onAddTransactionClick = { navController.navigate("addTransaction") },
                onSettingsClick       = { navController.navigate("settings") },
                onTransactionClick    = { tx: Transaction ->
                    navController.navigate("editTransaction/${tx.id}")
                }
            )
        }

        // --- EDIT TRANSACTION ---
        composable(
            route = "editTransaction/{transactionId}",
            arguments = listOf(navArgument("transactionId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            // 1) Extrai o ID da transação
            val txId = backStackEntry.arguments?.getInt("transactionId")!!

            // 2) Injeta o ViewModel e carrega o objeto
            val editVm: EditTransactionViewModel = viewModel()
            LaunchedEffect(txId) { editVm.loadTransaction(txId) }  // carrega via loadTransaction(id) :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}

            // 3) Observa o StateFlow<Transaction?> e só mostra quando não for null
            val tx by editVm.transaction.collectAsState()
            tx?.let { transaction ->
                EditTransactionScreen(
                    transaction     = transaction,
                    viewModel       = editVm,
                    onBack          = { navController.popBackStack() },
                    onSaveSuccess   = { navController.popBackStack() },
                    onDeleteSuccess = { navController.popBackStack() }
                )                                                    // tela de edição :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}
            }
        }

        // --- ADD TRANSACTION ---
        composable("addTransaction") {
            val addVm: AddTransactionViewModel = viewModel()
            AddTransactionScreen(
                viewModel        = addVm,
                onBack           = { navController.popBackStack() },
                onSaveSuccess    = {
                    // a própria AddTransactionScreen já chama addVm.addTransaction()
                    addVm.resetState()     // chama o método correto
                    navController.popBackStack()
                }
            )
        }

        // --- SETTINGS MENU ---
        composable("settings") {
            SettingsMenuScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { /* implementar se necessário */ },
                onNavigateToAppSettings = { navController.navigate("appSettings") },
                onNavigateToCategories = { /* implementar se necessário */ },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToPremium = { navController.navigate("Subscription") },
                onNavigateToNewFinancialPeriod = { navController.navigate("NewFinancialPeriod") },
                onNavigateToFinancialPeriods = { navController.navigate("FinancialPeriods") }
            )
        }
        
        composable("notifications") {
            NotificationImportSettingsScreen(
                onBack = { navController.popBackStack() },
                onSubscribe = {},
            )
        }

        composable("Subscription") {
            SubscriptionScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable("NewFinancialPeriod") {
            NewPeriodScreen(
                onBack = { navController.popBackStack() },
                onStartPeriod = {}
            )
        }

        composable("FinancialPeriods") {
            FinancialPeriodsScreen(
                onBack = { navController.popBackStack() },
            )
        }

    }
}
