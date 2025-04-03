package com.nate.autofinance.ui.screens.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nate.autofinance.ui.components.AppPasswordField
import com.nate.autofinance.ui.components.AppTextField

@Composable
fun RegisterFields(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    errorMessage: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Nome"
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Email"
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppPasswordField(
            password = password,
            onPasswordChange = onPasswordChange,
            placeholderText = "Senha",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppPasswordField(
            password = confirmPassword,
            onPasswordChange = onConfirmPasswordChange,
            placeholderText = "Confirmar Senha",
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}