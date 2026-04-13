package com.example.gerenciadordedespesas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GerenciadorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ExpenseManagerApp()
                }
            }
        }
    }
}

private data class FinanceEntry(
    val month: String,
    val description: String,
    val amount: Double,
)

private data class Dream(
    val title: String,
    val targetAmount: Double,
)

private enum class AppScreen(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Home("Início", Icons.Outlined.Home),
    Gains("Ganhos", Icons.Outlined.AttachMoney),
    Expenses("Gastos", Icons.AutoMirrored.Outlined.ListAlt),
    Dreams("Sonhos", Icons.Outlined.Flag),
}

@Composable
private fun ExpenseManagerApp() {
    val gains = remember {
        mutableStateListOf(
            FinanceEntry("Abr/2026", "Salário", 4800.0),
            FinanceEntry("Abr/2026", "Freelance", 950.0),
        )
    }
    val expenses = remember {
        mutableStateListOf(
            FinanceEntry("Abr/2026", "Aluguel", 1800.0),
            FinanceEntry("Abr/2026", "Mercado", 820.0),
        )
    }
    val dreams = remember {
        mutableStateListOf(
            Dream("Viagem de férias", 3500.0),
            Dream("Reserva para aposentadoria", 15000.0),
        )
    }
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Home) }

    val totalGains = gains.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val balance = totalGains - totalExpenses

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { androidx.compose.material3.Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { padding ->
        when (currentScreen) {
            AppScreen.Home -> HomeScreen(
                balance = balance,
                totalGains = totalGains,
                totalExpenses = totalExpenses,
                dreams = dreams,
                padding = padding,
            )

            AppScreen.Gains -> FinanceRegisterScreen(
                title = "Ganhos do mês",
                helperText = "Registre todas as entradas recebidas no mês desejado.",
                entries = gains,
                buttonText = "Adicionar ganho",
                padding = padding,
                onAddEntry = { month, description, amount ->
                    gains.add(FinanceEntry(month, description, amount))
                },
            )

            AppScreen.Expenses -> FinanceRegisterScreen(
                title = "Gastos do mês",
                helperText = "Liste todos os gastos realizados no mês desejado.",
                entries = expenses,
                buttonText = "Adicionar gasto",
                padding = padding,
                onAddEntry = { month, description, amount ->
                    expenses.add(FinanceEntry(month, description, amount))
                },
            )

            AppScreen.Dreams -> DreamsScreen(
                balance = balance,
                dreams = dreams,
                padding = padding,
                onAddDream = { title, targetAmount ->
                    dreams.add(Dream(title, targetAmount))
                },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    balance: Double,
    totalGains: Double,
    totalExpenses: Double,
    dreams: List<Dream>,
    padding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SummaryCard(
                title = "Valor em conta",
                value = balance.formatCurrency(),
                subtitle = "Saldo calculado por ganhos - gastos",
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "Ganhos",
                    value = totalGains.formatCurrency(),
                )
                SummaryMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "Gastos",
                    value = totalExpenses.formatCurrency(),
                )
            }
        }
        item {
            Text(
                text = "Sonhos em acompanhamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        items(dreams) { dream ->
            DreamStatusCard(dream = dream, balance = balance)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SummaryMiniCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinanceRegisterScreen(
    title: String,
    helperText: String,
    entries: List<FinanceEntry>,
    buttonText: String,
    padding: PaddingValues,
    onAddEntry: (String, String, Double) -> Unit,
) {
    var month by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Text(text = helperText, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mês") },
                        placeholder = { Text("Ex.: Abr/2026") },
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Descrição") },
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Button(
                        onClick = {
                            val amount = amountText.toBrazilianDoubleOrNull()
                            if (month.isNotBlank() && description.isNotBlank() && amount != null) {
                                onAddEntry(month.trim(), description.trim(), amount)
                                month = ""
                                description = ""
                                amountText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(buttonText)
                    }
                }
            }
        }
        item {
            Text(text = "Lançamentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(entries) { entry ->
            FinanceEntryCard(entry = entry)
        }
    }
}

@Composable
private fun FinanceEntryCard(entry: FinanceEntry) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = entry.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = entry.month, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = entry.amount.formatCurrency(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DreamsScreen(
    balance: Double,
    dreams: List<Dream>,
    padding: PaddingValues,
    onAddDream: (String, Double) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var targetAmountText by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(text = "Sonhos e desejos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Text(
                text = "Cadastre objetivos e acompanhe se o saldo atual já cobre cada meta.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            SummaryCard(
                title = "Saldo disponível",
                value = balance.formatCurrency(),
                subtitle = "Esse valor é usado como base para avaliar seus objetivos.",
            )
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Objetivo") },
                        placeholder = { Text("Ex.: Comprar um imóvel") },
                    )
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Valor necessário") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Button(
                        onClick = {
                            val targetAmount = targetAmountText.toBrazilianDoubleOrNull()
                            if (title.isNotBlank() && targetAmount != null) {
                                onAddDream(title.trim(), targetAmount)
                                title = ""
                                targetAmountText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Adicionar sonho")
                    }
                }
            }
        }
        items(dreams) { dream ->
            DreamStatusCard(dream = dream, balance = balance)
        }
    }
}

@Composable
private fun DreamStatusCard(dream: Dream, balance: Double) {
    val remaining = dream.targetAmount - balance
    val achieved = remaining <= 0

    Card(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = dream.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "Meta: ${dream.targetAmount.formatCurrency()}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (achieved) {
                    "Seu saldo atual já cobre esse objetivo."
                } else {
                    "Faltam ${remaining.formatCurrency()} para atingir essa meta."
                },
                color = if (achieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun GerenciadorTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

private fun Double.formatCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(this)
}

private fun String.toBrazilianDoubleOrNull(): Double? {
    return replace(".", "").replace(",", ".").toDoubleOrNull()
}

@Preview(showBackground = true)
@Composable
private fun AppPreview() {
    GerenciadorTheme {
        ExpenseManagerApp()
    }
}
