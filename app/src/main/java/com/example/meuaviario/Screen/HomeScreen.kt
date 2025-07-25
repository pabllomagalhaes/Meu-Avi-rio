package com.example.meuaviario

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meuaviario.ui.theme.MeuAviarioTheme
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    var showEggDialog by remember { mutableStateOf(false) }
    var showFeedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Meu Aviário") }) },
        // --- DESIGN MELHORADO: NavigationBar para mais clareza ---
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Já estamos aqui */ },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Painel") },
                    label = { Text("Painel") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("batch") },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Lotes") },
                    label = { Text("Lotes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("expense_history") },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Despesas") },
                    label = { Text("Despesas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("sale_history") },
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Vendas") },
                    label = { Text("Vendas") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEggDialog = true }) {
                Icon(Icons.Filled.Add, "Registar Coleta de Ovos")
            }
        },
        content = { paddingValues ->
            HomeContent(
                paddingValues = paddingValues,
                summary = homeViewModel.summary,
                weeklyProduction = homeViewModel.weeklyProduction,
                feedConversionRatio = homeViewModel.feedConversionRatio,
                monthlyExpenses = homeViewModel.monthlyExpenses,
                monthlySales = homeViewModel.monthlySales,
                alerts = homeViewModel.alerts,
                onFeedCardClick = { showFeedDialog = true }
            )

            if (showEggDialog) { EggCollectionDialog(onDismiss = { showEggDialog = false }, onConfirm = { homeViewModel.updateEggsToday(it); showEggDialog = false }) }
            if (showFeedDialog) { FeedConsumptionDialog(onDismiss = { showFeedDialog = false }, onConfirm = { homeViewModel.updateFeedConsumption(it); showFeedDialog = false }) }
        }
    )
}

@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    summary: AviarySummary?,
    weeklyProduction: List<Int>,
    feedConversionRatio: Double?,
    monthlyExpenses: Double?,
    monthlySales: Double?,
    alerts: List<String>,
    onFeedCardClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Painel de Controle", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (summary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (alerts.isNotEmpty()) {
                AlertCard(alerts = alerts)
            }

            val netProfit = if (monthlySales != null && monthlyExpenses != null) monthlySales - monthlyExpenses else null
            if (netProfit != null) {
                val profitColor = if (netProfit >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Lucro Líquido (Mês):", style = MaterialTheme.typography.titleMedium)
                        Text(
                            currencyFormat.format(netProfit),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = profitColor
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(title = "Vendas (Mês)", value = currencyFormat.format(monthlySales ?: 0.0), modifier = Modifier.weight(1f))
                InfoCard(title = "Despesas (Mês)", value = currencyFormat.format(monthlyExpenses ?: 0.0), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Métricas de Produção", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val postureRate = if (summary.activeHens > 0) (summary.eggsToday.toDouble() / summary.activeHens) * 100 else 0.0
                InfoCard(title = "Taxa de Postura", value = "${DecimalFormat("0.0").format(postureRate)} %", modifier = Modifier.weight(1f))
                InfoCard(title = "Conversão Alimentar", value = "${DecimalFormat("0.00").format(feedConversionRatio ?: 0.0)} kg/dúzia", modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                InfoCard(title = "Galinhas Ativas", value = summary.activeHens.toString(), modifier = Modifier.weight(1f))
                InfoCard(title = "Ovos Hoje", value = summary.eggsToday.toString(), modifier = Modifier.weight(1f))
            }

            // --- DESIGN MELHORADO: InfoCard clicável para consistência visual ---
            Box(modifier = Modifier.padding(top = 16.dp)) {
                InfoCard(
                    title = "Ração Consumida Hoje",
                    value = "${summary.feedConsumedToday} kg",
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onFeedCardClick)
                )
            }

            Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Produção (Últimos 7 dias)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProductionChart(modifier = Modifier.fillMaxWidth().height(150.dp), data = weeklyProduction)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AlertCard(alerts: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Alerta",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                alerts.forEach { alert ->
                    Text(
                        text = alert,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EggCollectionDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var eggCountInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registar Coleta de Ovos") },
        text = {
            OutlinedTextField(
                value = eggCountInput,
                onValueChange = { eggCountInput = it.filter { char -> char.isDigit() } },
                label = { Text("Quantidade de ovos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                val count = eggCountInput.toIntOrNull()
                if (count != null) onConfirm(count)
                else android.widget.Toast.makeText(context, "Por favor, insira um número.", android.widget.Toast.LENGTH_SHORT).show()
            }) { Text("Confirmar") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun FeedConsumptionDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var feedInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registar Consumo de Ração") },
        text = {
            OutlinedTextField(
                value = feedInput,
                onValueChange = { feedInput = it },
                label = { Text("Consumo em kg (ex: 4.5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                val amount = feedInput.replace(',', '.').toDoubleOrNull()
                if (amount != null) onConfirm(amount)
                else android.widget.Toast.makeText(context, "Por favor, insira um valor válido.", android.widget.Toast.LENGTH_SHORT).show()
            }) { Text("Confirmar") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ProductionChart(modifier: Modifier = Modifier, data: List<Int>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Sem dados de produção para exibir o gráfico.")
        }
        return
    }
    Canvas(modifier = modifier) {
        val maxValue = data.maxOrNull() ?: 0
        val minValue = data.minOrNull() ?: 0
        val valueRange = (maxValue - minValue).toFloat().coerceAtLeast(1f)
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = size.width * (index.toFloat() / (data.size - 1).toFloat().coerceAtLeast(1f))
            val y = size.height * (1 - ((value - minValue) / valueRange))
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = primaryColor, radius = 8f, center = Offset(x, y))
        }
        drawPath(path = path, color = primaryColor, style = Stroke(width = 5f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MeuAviarioTheme {
        HomeScreen(navController = rememberNavController())
    }
}
