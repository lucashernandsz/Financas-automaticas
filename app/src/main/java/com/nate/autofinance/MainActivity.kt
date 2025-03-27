package com.nate.autofinance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nate.autofinance.ui.screens.register.RegisterScreenVisual
import com.nate.autofinance.ui.screens.transactionList.TransactionListScreen
import com.nate.autofinance.ui.theme.AutofinanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutofinanceTheme {
                TransactionListScreen()
            }
        }
    }
}
