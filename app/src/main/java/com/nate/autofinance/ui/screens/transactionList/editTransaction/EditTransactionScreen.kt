package com.nate.autofinance.ui.screens.transactionList.editTransaction

import CategoryFilterRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nate.autofinance.data.models.Transaction
import com.nate.autofinance.ui.components.AppTextField
import com.nate.autofinance.ui.components.AppTopBarPageTitle
import com.nate.autofinance.utils.Categories
import com.nate.autofinance.viewmodel.EditTransactionState
import com.nate.autofinance.viewmodel.EditTransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    viewModel: EditTransactionViewModel = viewModel(),   // injeta o VM
    onBack: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    onDeleteSuccess: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var dateText by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transaction.date))
    }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var description by remember { mutableStateOf(transaction.description) }

    // navega de volta quando a edição ou exclusão for concluída
    LaunchedEffect(state) {
        when (state) {
            EditTransactionState.UpdateSuccess -> onSaveSuccess()
            EditTransactionState.DeleteSuccess -> onDeleteSuccess()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBarPageTitle(
                text            = "Editar Transação",
                showBackButton  = true,
                onBackClick     = onBack
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Categoria", style = MaterialTheme.typography.labelLarge)
                CategoryFilterRow(
                categories         = Categories.fixedCategories,
                selectedCategory   = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            AppTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder    = "Valor",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AppTextField(
                value = dateText,
                onValueChange = { dateText = it },
                placeholder    = "Data (dd/MM/yyyy)"
            )

            AppTextField(
                value      = description,
                onValueChange = { description = it },
                placeholder    = "Descrição",
                singleLine     = false
            )

            if (state is EditTransactionState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            if (state is EditTransactionState.Error) {
                Text(
                    text  = (state as EditTransactionState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val date = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateText)!!
                    } catch (_: Exception) {
                        transaction.date
                    }
                    viewModel.editTransaction(
                        transaction.copy(
                            date        = date,
                            amount      = amt,
                            category    = selectedCategory,
                            description = description
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = state !is EditTransactionState.Loading
            ) {
                Text("Salvar", color = Color.White)
            }

            OutlinedButton(
                onClick = { viewModel.deleteTransaction(transaction) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = state !is EditTransactionState.Loading
            ) {
                Text("Excluir", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
