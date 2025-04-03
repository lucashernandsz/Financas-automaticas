package com.nate.autofinance.ui.screens.transactionList

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.nate.autofinance.domain.models.Transaction
import com.nate.autofinance.ui.components.AppTextField
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit = {},
    onSave: (Transaction) -> Unit = {}
) {
    // Estados para os campos
    var amount by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Ganho") }
    var description by remember { mutableStateOf("") }

    // Preenche a data com a data atual se não estiver definida
    LaunchedEffect(Unit) {
        if (dateText.isEmpty()) {
            dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }
    }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text = "Adicionar Transação",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Categoria", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("Ganho", "Despesa", "Outros")
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            labelColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
            // Campo de Valor
            AppTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = "Valor",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Campo de Data
            AppTextField(
                value = dateText,
                onValueChange = { dateText = it },
                placeholder = "Data (dd/MM/aaaa)"
            )
            // Seleção de Categoria via chips

            // Campo de Descrição
            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Descrição",
                singleLine = false
            )
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val date = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateText) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }
                    val transaction = Transaction(
                        id = 0,
                        date = date,
                        amount = amountValue,
                        description = description,
                        category = selectedCategory
                    )
                    onSave(transaction)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Salvar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    MaterialTheme {
        AddTransactionScreen()
    }
}
