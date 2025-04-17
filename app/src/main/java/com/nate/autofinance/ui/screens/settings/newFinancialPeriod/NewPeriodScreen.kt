package com.nate.autofinance.ui.screens.settings.newFinancialPeriod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.utils.toBrazilianCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartNewPeriodScreen(
    currentBalance: Double,
    onClose: () -> Unit = {},
    onStartPeriod: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Iniciar novo período",
                showBackButton = true,
                onBackClick = onClose
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = currentBalance.toBrazilianCurrency(),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 24.dp)
            )

            Text(
                text = "Seu saldo atual será transportado para o novo período. Comece seu novo período com um saldo de ${ (currentBalance + 1000).toBrazilianCurrency() }.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onStartPeriod,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Começar novo período", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartNewPeriodScreenPreview() {
    StartNewPeriodScreen(
        currentBalance = 4943.00,
        onClose = {},
        onStartPeriod = {}
    )
}
