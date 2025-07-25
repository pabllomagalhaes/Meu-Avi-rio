package com.example.meuaviario

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleHistoryScreen(
    navController: NavController,
    viewModel: SaleHistoryViewModel = viewModel()
) {
    val sales = viewModel.sales.value
    var showEditDialog by remember { mutableStateOf(false) }
    var saleToEdit by remember { mutableStateOf<Sale?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Histórico de Vendas") })
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
                    onClick = { navController.navigate("batch") },
                    icon = { Icon(Icons.Filled.Inventory, contentDescription = "Lotes") },
                    label = { Text("Lotes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("expense_history") },
                    icon = { Icon(Icons.Filled.ShoppingCartCheckout, contentDescription = "Despesas") },
                    label = { Text("Despesas") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Já estamos aqui */ },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Vendas") },
                    label = { Text("Vendas") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("sale") }) {
                Icon(Icons.Default.Add, contentDescription = "Registar Venda")
            }
        },
        content = { paddingValues ->
            if (sales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma venda registada.\nClique no botão '+' para adicionar a sua primeira.",
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
                    items(sales) { sale ->
                        SaleItem(
                            sale = sale,
                            onEditClick = {
                                saleToEdit = it
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                saleToDelete = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            if (showEditDialog) {
                AddOrEditSaleDialog(
                    saleToEdit = saleToEdit,
                    onDismiss = { showEditDialog = false },
                    onConfirm = { quantity, price ->
                        saleToEdit?.let {
                            viewModel.updateSale(it.id, quantity, price,
                                onSuccess = { showEditDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        }
                    }
                )
            }

            if (showDeleteDialog) {
                DeleteSaleConfirmDialog(
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        saleToDelete?.let {
                            viewModel.deleteSale(it.id,
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
fun SaleItem(sale: Sale, onEditClick: (Sale) -> Unit, onDeleteClick: (Sale) -> Unit) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${sale.quantityInDozens} dúzias vendidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Preço unitário: ${currencyFormat.format(sale.pricePerDozen)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                sale.timestamp?.let {
                    Text(
                        text = dateFormat.format(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = currencyFormat.format(sale.totalAmount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { onEditClick(sale) }) {
                Icon(Icons.Default.Edit, "Editar Venda")
            }
            IconButton(onClick = { onDeleteClick(sale) }) {
                Icon(Icons.Default.Delete, "Eliminar Venda")
            }
        }
    }
}

@Composable
fun AddOrEditSaleDialog(
    saleToEdit: Sale?,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double) -> Unit
) {
    var quantity by remember { mutableStateOf(saleToEdit?.quantityInDozens?.toString() ?: "") }
    var price by remember { mutableStateOf(saleToEdit?.pricePerDozen?.toString() ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Venda") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantidade (dúzias)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Preço por dúzia (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull()
                    val priceDouble = price.replace(',', '.').toDoubleOrNull()
                    if (quantityInt != null && priceDouble != null) {
                        onConfirm(quantityInt, priceDouble)
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
fun DeleteSaleConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminação") },
        text = { Text("Tem a certeza de que deseja eliminar este registo de venda? Esta ação não pode ser desfeita.") },
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
