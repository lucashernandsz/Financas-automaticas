package com.nate.autofinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.nate.autofinance.ui.screens.login.LoginScreen
import com.nate.autofinance.ui.screens.register.RegisterScreen
import com.nate.autofinance.ui.screens.register.RegisterViewModel
import com.nate.autofinance.ui.screens.transactionList.AddTransactionScreen
import com.nate.autofinance.ui.screens.transactionList.TransactionListScreen
import com.nate.autofinance.ui.screens.settings.NotificationImportSettingsScreen
import com.nate.autofinance.ui.screens.settings.SettingsMenuScreen
import com.nate.autofinance.ui.screens.settings.financialPeriods.FinancialPeriodsScreen
import com.nate.autofinance.ui.theme.AutofinanceTheme
import com.nate.autofinance.utils.SessionManager
import com.nate.autofinance.ui.screens.settings.newFinancialPeriod.NewPeriodScreen
import com.nate.autofinance.ui.screens.settings.subscription.SubscriptionScreen
import com.nate.autofinance.ui.screens.transactionList.TransactionViewModel
import com.nate.autofinance.ui.screens.transactionList.editTransaction.EditTransactionScreen
import com.nate.autofinance.ui.screens.transactionList.editTransaction.EditTransactionViewModel
import com.nate.autofinance.utils.Categories

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(false) }
            AutofinanceTheme(darkTheme = darkMode) {
                AutoFinanceApp(
                    darkMode = darkMode,
                    onToggleDarkMode = { darkMode = it }
                )
            }
        }
    }
}

@Composable
fun AutoFinanceApp(
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Rota inicial é null enquanto checamos sessão
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Verifica se há sessão salva
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId(context)            // lê userId persistido :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        startDestination = if (userId != null && firebaseUser != null) {
            "transactionList"
        } else {
            "login?snackbarMessage={snackbarMessage}"
        }
    }

    if (startDestination == null) {
        // Tela de loading enquanto decide a rota inicial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Aqui já sabemos que não é null: capturamos o valor seguro
        val initialRoute = startDestination!!
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = initialRoute
        ) {
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
                val message = backStackEntry.arguments?.getString("snackbarMessage")
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("transactionList") {
                            popUpTo("login?snackbarMessage={snackbarMessage}") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    snackbarMessage = message
                )
            }

            composable("register") {
                val registerVm: RegisterViewModel = viewModel()
                RegisterScreen(
                    viewModel = registerVm,
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

            composable("transactionList") {
                val txVm: TransactionViewModel = viewModel()
                // Removed automatic sync here to avoid duplicate inserts on return
                TransactionListScreen(
                    viewModel = txVm,
                    onDashboardClick = { /* ... */ },
                    onTransactionsClick = { /* ... */ },
                    onAddTransactionClick = { category ->
                        navController.navigate("addTransaction/$category")
                    },
                    onSettingsClick = { navController.navigate("settings") },
                    onTransactionClick = { tx ->
                        navController.navigate("editTransaction/${tx.id}")
                    }
                )
            }

            composable(
                "editTransaction/{transactionId}",
                arguments = listOf(navArgument("transactionId") {
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                val txId = backStackEntry.arguments!!.getInt("transactionId")
                val editVm: EditTransactionViewModel = viewModel()
                LaunchedEffect(txId) { editVm.loadTransaction(txId) }
                val tx by editVm.transaction.collectAsState()
                tx?.let {
                    EditTransactionScreen(
                        transaction = it,
                        viewModel = editVm,
                        onBack = { navController.popBackStack() },
                        onSaveSuccess = { navController.popBackStack() },
                        onDeleteSuccess = { navController.popBackStack() }
                    )
                }
            }

            composable(
                "addTransaction/{initialCategory}",
                arguments = listOf(navArgument("initialCategory") {
                    type = NavType.StringType
                    defaultValue = Categories.INCOME
                })
            ) { backStack ->
                val initialCategory = backStack.arguments?.getString("initialCategory")
                    ?: Categories.INCOME
                AddTransactionScreen(
                    initialCategory = initialCategory,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsMenuScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToPremium       = { navController.navigate("Subscription") },
                    onNavigateToNewFinancialPeriod = { navController.navigate("NewFinancialPeriod") },
                    onNavigateToFinancialPeriods   = { navController.navigate("FinancialPeriods") }
                )
            }

            composable("notifications") {
                NotificationImportSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSubscribe = { /* ... */ }
                )
            }

            composable("Subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }

            composable("NewFinancialPeriod") {
                NewPeriodScreen(onBack = { navController.popBackStack() }, onStartPeriod = { /* ... */ })
            }

            composable("FinancialPeriods") {
                FinancialPeriodsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
