package com.nate.autofinance.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppLargeCenteredTopBar
import com.nate.autofinance.viewmodel.LoginState
import com.nate.autofinance.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,           // Callback para redirecionar à tela de transações
    onNavigateToRegister: () -> Unit,       // Callback para navegar para a tela de cadastro
    snackbarMessage: String? = null         // Mensagem opcional (vinda do cadastro)
) {
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.loginState.collectAsState()

    // Estados locais para os campos de entrada
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val errorMessage = if (loginState is LoginState.Error) {
        (loginState as LoginState.Error).message
    } else {
        ""
    }
    val isLoading = loginState is LoginState.Loading

    // Cria o host do Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Exibe o snackbar se uma mensagem for passada (vindo, por exemplo, do cadastro)
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Quando o login for bem-sucedido, chama o callback para redirecionar à tela de transações
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppLargeCenteredTopBar(stringResource(id = R.string.welcome_message))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Estamos comprometidos em te ajudar a organizar suas finanças. Vamos começar?\n",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            LoginFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                errorMessage = errorMessage
            )
            // Botão para login com Google (lógica futura)
            Button(
                onClick = { /* Ação para login com o Google */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar com o Google")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Botão de login que delega a ação para a ViewModel
            Button(
                onClick = { loginViewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Entrar")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            // Botão para navegar à tela de cadastro
            TextButton(onClick = { onNavigateToRegister() }) {
                Text("Não possui uma conta? Crie aqui")
            }
        }
    }
}
