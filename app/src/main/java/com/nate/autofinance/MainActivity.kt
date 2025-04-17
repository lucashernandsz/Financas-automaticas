package com.nate.autofinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.screens.login.LoginScreen
import com.nate.autofinance.ui.screens.register.RegisterScreen
import com.nate.autofinance.ui.screens.transactionList.TransactionListScreen
import com.nate.autofinance.ui.screens.transactionList.AddTransactionScreen
import com.nate.autofinance.ui.screens.transactionList.editTransaction.EditTransactionScreen
import com.nate.autofinance.ui.screens.settings.SettingsMenuScreen
import com.nate.autofinance.ui.screens.settings.appSettings.AppSettingsScreen
import com.nate.autofinance.ui.screens.settings.financialPeriods.FinancialPeriodsScreen
import com.nate.autofinance.ui.screens.settings.info.PersonalInfoScreen
import com.nate.autofinance.ui.screens.settings.newFinancialPeriod.StartNewPeriodScreen
import com.nate.autofinance.ui.screens.settings.subscription.SubscriptionScreen
import com.nate.autofinance.ui.theme.AutofinanceTheme
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutofinanceTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login?snackbarMessage={snackbarMessage}"
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
            val snackbarMessage = backStackEntry.arguments?.getString("snackbarMessage")
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("transactionList") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                snackbarMessage = if (snackbarMessage.isNullOrEmpty()) null else snackbarMessage
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { message ->
                    navController.navigate("login?snackbarMessage=$message") {
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
            TransactionListScreen(
                onDashboardClick = { navController.navigate("dashboard") },
                onTransactionsClick = { /* ação se necessário */ },
                onAddTransactionClick = { navController.navigate("addTransaction") },
                onSettingsClick = { navController.navigate("settings") },
                onTransactionClick = { transaction: Transaction ->
                    // Exemplo: passando dados ou utilizando ViewModel para transferir o objeto
                    navController.navigate("editTransaction")
                },
                viewModel = TODO()
            )
        }
        composable("addTransaction") {
            AddTransactionScreen(
                onBack = { navController.popBackStack() },
                onSave = { transaction : Transaction ->
                    // Lógica para salvar a transação
                    navController.popBackStack()
                }
            )
        }
        composable("editTransaction") {
            EditTransactionScreen(
                transaction = Transaction(
                    id = 1,
                    date = Date(),
                    amount = 1500.0,
                    description = "Salário",
                    category = "Ganho",
                    userId = 0,
                    financialPeriodId = 0,
                    imported = false,
                    firebaseDocId = "documentId"
                ),
                onBack = { navController.popBackStack() },
                onSave = { updatedTransaction ->
                    // Lógica para atualizar a transação
                    navController.popBackStack()
                }
            )
        }
        // Rota para o menu de configurações
        composable("settings") {
            SettingsMenuScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate("personalInfo") },
                onNavigateToAppSettings = { navController.navigate("appSettings") },
                onNavigateToPremium = { navController.navigate("subscription") },
                onNavigateToNewFinancialPeriod = { navController.navigate("newFinancialPeriod") },
                onNavigateToFinancialPeriods = { navController.navigate("financialPeriods") }
            )
        }
        // Rota para Informações Pessoais
        composable("personalInfo") {
            PersonalInfoScreen(
                email = "user@example.com",
                password = "********",
                onBack = { navController.popBackStack() },
                onEmailChange = { /* lógica para atualizar email */ },
                onPasswordChange = { /* lógica para atualizar senha */ },
                onSaveAndExit = { navController.popBackStack() },
                onExitWithoutSave = { navController.popBackStack() }
            )
        }
        // Rota para Configurações do App
        composable("appSettings") {
            AppSettingsScreen(
                isDarkMode = false,
                onBack = { navController.popBackStack() },
                onToggleDarkMode = { /* lógica para alternar o modo escuro */ },
                onSaveAndBack = { navController.popBackStack() },
                onExit = { navController.popBackStack() }
            )
        }
        // Rota para Assinatura/Premium
        composable("subscription") {
            SubscriptionScreen(
                onClose = { navController.popBackStack() },
                onStartTrial = { /* lógica para iniciar o teste gratuito */ }
            )
        }
        // Rota para Iniciar Novo Período Financeiro
        composable("newFinancialPeriod") {
            StartNewPeriodScreen(
                currentBalance = 0.0,
                onClose = { navController.popBackStack() },
                onStartPeriod = {
                    // Lógica para iniciar o novo período
                    navController.popBackStack()
                }
            )
        }
        // Rota para Navegar entre Períodos Financeiros
        composable("financialPeriods") {
            FinancialPeriodsScreen(
                periods = listOf("Janeiro", "Fevereiro", "Março"),
                selected = emptySet(),
                onBack = { navController.popBackStack() },
                onSelectPeriod = { index: Int ->
                    // Lógica para selecionar um período, ex: navegar para detalhes do período selecionado.
                },
                onDelete = {
                    // Lógica para deletar os períodos selecionados.
                }
            )
        }
    }
}
