package com.example.meuaviario

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScreen(
    navController: NavController,
    viewModel: BatchViewModel = viewModel()
) {
    val batches = viewModel.batches.value
    var showDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<Batch?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var batchToDelete by remember { mutableStateOf<Batch?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Lotes") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Painel") },
                    label = { Text("Painel") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("production") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Outlined.EditNote, contentDescription = "Produção") },
                    label = { Text("Produção") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Já está nesta tela, não faz nada */ },
                    icon = { Icon(Icons.Outlined.ContentPaste, contentDescription = "Lotes") },
                    label = { Text("Lotes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("expense_history") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Outlined.Store, contentDescription = "Despesas") },
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
            FloatingActionButton(onClick = {
                batchToEdit = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, "Adicionar Lote")
            }
        },
        content = { paddingValues ->
            if (batches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum lote registado.\nClique no botão '+' para adicionar o seu primeiro.",
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
                    items(batches) { batch ->
                        BatchItem(
                            batch = batch,
                            onEditClick = {
                                batchToEdit = it
                                showDialog = true
                            },
                            onDeleteClick = {
                                batchToDelete = it
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            if (showDialog) {
                AddBatchDialog(
                    batchToEdit = batchToEdit,
                    onDismiss = { showDialog = false },
                    onConfirm = { name, breed, numberOfHens ->
                        if (batchToEdit == null) {
                            viewModel.addBatch(name, breed, numberOfHens,
                                onSuccess = { showDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        } else {
                            viewModel.updateBatch(batchToEdit!!.id, name, breed, numberOfHens,
                                onSuccess = { showDialog = false },
                                onError = { /* Tratar erro */ }
                            )
                        }
                    }
                )
            }

            if (showDeleteConfirmDialog) {
                DeleteConfirmDialog(
                    onDismiss = { showDeleteConfirmDialog = false },
                    onConfirm = {
                        batchToDelete?.let {
                            viewModel.deleteBatch(it.id,
                                onSuccess = { showDeleteConfirmDialog = false },
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
fun BatchItem(batch: Batch, onEditClick: (Batch) -> Unit, onDeleteClick: (Batch) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = batch.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Raça: ${batch.breed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Nº de Aves: ${batch.numberOfHens}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                batch.startDate?.let {
                    Text(
                        text = "Início: ${dateFormat.format(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { onEditClick(batch) }) {
                Icon(Icons.Default.Edit, "Editar Lote")
            }
            IconButton(onClick = { onDeleteClick(batch) }) {
                Icon(Icons.Default.Delete, "Eliminar Lote")
            }
        }
    }
}

@Composable
fun AddBatchDialog(
    batchToEdit: Batch?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(batchToEdit?.name ?: "") }
    var breed by remember { mutableStateOf(batchToEdit?.breed ?: "") }
    var numberOfHens by remember { mutableStateOf(batchToEdit?.numberOfHens?.toString() ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (batchToEdit == null) "Adicionar Novo Lote" else "Editar Lote") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Lote") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raça") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numberOfHens,
                    onValueChange = { numberOfHens = it.filter { c -> c.isDigit() } },
                    label = { Text("Nº de Aves") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hensInt = numberOfHens.toIntOrNull()
                    if (name.isNotBlank() && breed.isNotBlank() && hensInt != null) {
                        onConfirm(name, breed, hensInt)
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
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminação") },
        text = { Text("Tem a certeza de que deseja eliminar este lote? Esta ação não pode ser desfeita.") },
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
