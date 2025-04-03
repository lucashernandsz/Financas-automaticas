package com.nate.autofinance.ui.screens.settings.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.nate.autofinance.ui.components.AppTopBarPageTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    monthlyPrice: String = "R$10.00",
    annualPrice: String = "R$3.99",
    onClose: () -> Unit = {},
    onStartTrial: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Subscription",
                showBackButton = true,
                onBackClick = onClose
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "É como ter um assistente pessoal que anotasse tudo para você...",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Com nossa tecnologia, conseguimos automatizar o processo de inserção e categorização de todas suas transações",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            listOf(
                "Categorização automática de transações",
                "Inserção automática de transações",
                "Categorias ilimitadas"
            ).forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(feature, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Monthly plan card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Mensal", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Text(
                        text = "$monthlyPrice /month",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White
                    )
                }
            }

            // Annual plan card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Anual (R\$47.88)", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "$annualPrice /month",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStartTrial,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start 7‑day free trial", color = Color.White)
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionScreenPreview() {
    SubscriptionScreen()
}
