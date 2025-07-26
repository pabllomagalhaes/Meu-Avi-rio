package com.example.meuaviario

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.meuaviario.ui.theme.MeuAviarioTheme
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    var showEggDialog by remember { mutableStateOf(false) }
    var showFeedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Painel de Controle") }) }, // Título alterado
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Já estamos aqui */ },
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
                    selected = false,
                    onClick = { navController.navigate("batch") { popUpTo("home") { inclusive = true } } },
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
        content = { paddingValues ->
            HomeContent(
                paddingValues = paddingValues,
                summary = homeViewModel.summary,
                monthlyProductionData = homeViewModel.monthlyProductionData,
                feedConversionRatio = homeViewModel.feedConversionRatio,
                monthlyExpenses = homeViewModel.monthlyExpenses,
                monthlySales = homeViewModel.monthlySales,
                alerts = homeViewModel.alerts
            )
        }
    )
}

@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    summary: AviarySummary?,
    monthlyProductionData: List<DailyProduction>,
    feedConversionRatio: Double?,
    monthlyExpenses: Double?,
    monthlySales: Double?,
    alerts: List<String>
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
        // Texto "Painel de Controle" removido daqui
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
                InfoCard(title = "Conversão Alimentar (7d)", value = "${DecimalFormat("0.00").format(feedConversionRatio ?: 0.0)} kg/dúzia", modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp)) {
                InfoCard(title = "Galinhas Ativas", value = summary.activeHens.toString(), modifier = Modifier.weight(1f))
                InfoCard(title = "Ovos Hoje", value = summary.eggsToday.toString(), modifier = Modifier.weight(1f))
            }

            Box(modifier = Modifier.padding(top = 16.dp)) {
                InfoCard(
                    title = "Ração Consumida Hoje",
                    value = "${summary.feedConsumedToday} kg",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Produção (Últimos 30 dias)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProductionChart(modifier = Modifier.fillMaxWidth().height(200.dp), data = monthlyProductionData)
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
fun ProductionChart(modifier: Modifier = Modifier, data: List<DailyProduction>) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Sem dados de produção para exibir o gráfico.")
        }
        return
    }

    Canvas(modifier = modifier) {
        val paddingBottom = 40.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val chartHeight = size.height - paddingBottom - paddingTop

        val eggData = data.map { it.eggs }
        val maxValue = eggData.maxOrNull() ?: 0
        val minValue = 0 // Começa sempre do zero para contexto
        val valueRange = (maxValue - minValue).toFloat().coerceAtLeast(1f)

        val linePath = Path()
        val fillPath = Path()

        // Desenha a grelha de fundo
        val gridLines = 5
        (0..gridLines).forEach { i ->
            val y = chartHeight * (i.toFloat() / gridLines) + paddingTop
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        data.forEachIndexed { index, dailyProd ->
            val x = size.width * (index.toFloat() / (data.size - 1).toFloat().coerceAtLeast(1f))
            val y = chartHeight * (1 - ((dailyProd.eggs - minValue) / valueRange)) + paddingTop

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, size.height - paddingBottom)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height - paddingBottom)
        fillPath.close()

        // Desenha o preenchimento com gradiente
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                endY = chartHeight + paddingTop
            )
        )

        // Desenha a linha do gráfico
        drawPath(path = linePath, color = primaryColor, style = Stroke(width = 6f))

        // Desenha os pontos e as legendas
        data.forEachIndexed { index, dailyProd ->
            val x = size.width * (index.toFloat() / (data.size - 1).toFloat().coerceAtLeast(1f))
            val y = chartHeight * (1 - ((dailyProd.eggs - minValue) / valueRange)) + paddingTop

            drawCircle(color = primaryColor, radius = 10f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = 5f, center = Offset(x, y))

            val eggText = dailyProd.eggs.toString()
            val measuredEggText = textMeasurer.measure(eggText, style = TextStyle(color = onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.Bold))
            drawText(
                textMeasurer = textMeasurer,
                text = eggText,
                style = TextStyle(color = onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                topLeft = Offset(x - (measuredEggText.size.width / 2), y - measuredEggText.size.height - 20f)
            )

            dailyProd.timestamp?.let {
                val dateText = dateFormat.format(it)
                val measuredDateText = textMeasurer.measure(dateText, style = TextStyle(color = onSurfaceColor, fontSize = 10.sp))
                drawText(
                    textMeasurer = textMeasurer,
                    text = dateText,
                    style = TextStyle(color = onSurfaceColor, fontSize = 10.sp),
                    topLeft = Offset(x - (measuredDateText.size.width / 2), size.height - paddingBottom + 10f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MeuAviarioTheme {
        HomeScreen(navController = rememberNavController())
    }
}
