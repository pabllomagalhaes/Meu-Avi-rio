package com.example.meuaviario

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meuaviario.data.Batch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScreen(
    navController: NavController,
    viewModel: BatchViewModel = viewModel()
) {
    val batches = viewModel.batches.value
    var showAddBatchDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Lotes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBatchDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Lote")
            }
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(batches) { batch ->
                    BatchItem(batch = batch)
                }
            }

            if (showAddBatchDialog) {
                AddBatchDialog(
                    onDismiss = { showAddBatchDialog = false },
                    onConfirm = { name, breed, numberOfHens ->
                        viewModel.addBatch(name, breed, numberOfHens,
                            onSuccess = { showAddBatchDialog = false },
                            onError = { /* Tratar erro */ }
                        )
                    }
                )
            }
        }
    )
}

@Composable
fun BatchItem(batch: Batch) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(batch.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Raça: ${batch.breed}", style = MaterialTheme.typography.bodyMedium)
            Text("Nº de Aves: ${batch.numberOfHens}", style = MaterialTheme.typography.bodyMedium)
            batch.startDate?.let {
                Text("Início: ${dateFormat.format(it)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddBatchDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var numberOfHens by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Novo Lote") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Lote") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Raça") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numberOfHens,
                    onValueChange = { numberOfHens = it.filter { char -> char.isDigit() } },
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
