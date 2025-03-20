package com.nate.autofinance.ui.screens.login
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LoginScreenVisual() {
    // Estados locais para email, senha, loading e mensagem de erro
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputFields(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                errorMessage = errorMessage
            )
            Button(
                onClick = {
                    // Simula uma ação de login
                    isLoading = true
                    errorMessage = ""
                    // Exemplo de validação:
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Preencha todos os campos!"
                        isLoading = false
                    } else {
                        // Simula um delay e sucesso
                        // Aqui você chamaria sua função de autenticação.
                        isLoading = false
                    }
                },
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
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { /* Navegar para tela de cadastro */ }) {
                Text("Cadastre-se")
            }
        }
    }
}


@Composable
fun InputFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Senha") },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation()
    )
    Spacer(modifier = Modifier.height(16.dp))
    if (errorMessage.isNotEmpty()) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(8.dp)
        )
    }
}


fun render(){

}

fun handleUserInput(){

}
