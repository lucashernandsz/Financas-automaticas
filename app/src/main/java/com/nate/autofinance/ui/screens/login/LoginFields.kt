package com.nate.autofinance.ui.screens.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nate.autofinance.R
import com.nate.autofinance.ui.components.AppPasswordField
import com.nate.autofinance.ui.components.AppTextField

@Composable
fun LoginFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String
) {
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.bodySmall
    )
    AppTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(id = R.string.email),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    AppPasswordField(
        password = password,
        onPasswordChange = onPasswordChange,
        placeholderText = stringResource(id = R.string.password),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(32.dp))
}