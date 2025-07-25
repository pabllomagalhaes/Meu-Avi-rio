package com.example.meuaviario

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meuaviario.ui.theme.MeuAviarioTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    navController: NavController,
    viewModel: ExpenseHistoryViewModel = viewModel()
) {
    val expenses = viewModel.expenses.value
    var showEditDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Histórico de Despesas") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Filled.SpaceDashboard, contentDescription = "Painel") },
                    label = { Text("Painel") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("batch") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Filled.Inventory, contentDescription = "Lotes") },
                    label = { Text("Lotes") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Já estamos aqui */ },
                    icon = { Icon(Icons.Filled.ShoppingCartCheckout, contentDescription = "Despesas") },
                    label = { Text("Despesas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("sale_history") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Vendas") },
                    label = { Text("Vendas") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("expense") }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Despesa")
            }
        },
        content = { paddingValues ->
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma despesa registada.\nClique no botão '+' para adicionar a sua primeira.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onEditClick = {
                                expenseToEdit = it
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                expenseToDelete = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            if (showEditDialog) {
                AddOrEditExpenseDialog(
                    expenseToEdit = expenseToEdit,
                    onDismiss = { showEditDialog = false },
                    onConfirm = { description, amount, category ->
                        expenseToEdit?.let {
                            viewModel.updateExpense(it.id, description, amount, category,
                                onSuccess = { showEditDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        }
                    }
                )
            }

            if (showDeleteDialog) {
                DeleteExpenseConfirmDialog(
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        expenseToDelete?.let {
                            viewModel.deleteExpense(it.id,
                                onSuccess = { showDeleteDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun ExpenseItem(expense: Expense, onEditClick: (Expense) -> Unit, onDeleteClick: (Expense) -> Unit) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(expense.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                expense.timestamp?.let {
                    Text(dateFormat.format(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                currencyFormat.format(expense.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { onEditClick(expense) }) {
                Icon(Icons.Default.Edit, "Editar Despesa")
            }
            IconButton(onClick = { onDeleteClick(expense) }) {
                Icon(Icons.Default.Delete, "Eliminar Despesa")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditExpenseDialog(
    expenseToEdit: Expense?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var description by remember { mutableStateOf(expenseToEdit?.description ?: "") }
    var amount by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    val categories = listOf("Ração", "Saúde", "Outros")
    var selectedCategory by remember { mutableStateOf(expenseToEdit?.category ?: categories[0]) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expenseToEdit == null) "Adicionar Despesa" else "Editar Despesa") },
        text = {
            Column {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category) }, onClick = {
                                selectedCategory = category
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.replace(',', '.').toDoubleOrNull()
                    if (description.isNotBlank() && amountDouble != null) {
                        onConfirm(description, amountDouble, selectedCategory)
                    } else {
                        Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteExpenseConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminação") },
        text = { Text("Tem a certeza de que deseja eliminar este registo de despesa? Esta ação não pode ser desfeita.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ExpenseHistoryScreenPreview() {
    MeuAviarioTheme {
        ExpenseHistoryScreen(navController = rememberNavController())
    }
}
