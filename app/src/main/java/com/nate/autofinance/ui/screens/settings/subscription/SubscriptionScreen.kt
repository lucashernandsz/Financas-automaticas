package com.nate.autofinance.ui.screens.settings.subscription

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    // 1) Pega o Application
    val app = LocalContext.current.applicationContext as Application

    // 2) Cria o ViewModel
    val vm: SubscriptionViewModel = viewModel(
        modelClass = SubscriptionViewModel::class.java,
        factory = AndroidViewModelFactory.getInstance(app)
    )

    // 3) Lê o estado
    val isPremium by vm.isSubscribed.collectAsState()

    // 4) UI com o ViewModel “dentro”
    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Subscription",
                showBackButton = true,
                onBackClick = onBack
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
            // ... demais textos/cards/features ...

            Spacer(Modifier.weight(1f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.activatePremium { onBack() } },
                    modifier = Modifier.weight(1f),
                    enabled = !isPremium,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isPremium) "Premium Ativado" else "Ativar Premium",
                        color = Color.White
                    )
                }
            }
        }
    }
}
