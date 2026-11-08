package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.ProductEntity
import com.example.core.database.TransactionEntity
import com.example.core.util.FormatUtils
import com.example.core.util.ReceiptExportHelper
import com.example.ui.SuperPosViewModel
import com.example.ui.theme.*

@Composable
fun DashboardReportsScreen(
    viewModel: SuperPosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val allItems by viewModel.allTransactionItems.collectAsState()
    val products by viewModel.products.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val activeShift by viewModel.activeShift.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Semua Waktu") }
    val periods = listOf("Hari Ini", "Minggu Ini", "Bulan Ini", "Semua Waktu")

    val now = System.currentTimeMillis()
    val filteredTransactions = remember(transactions, selectedPeriod) {
        val valid = transactions.filter { it.paymentStatus != "REFUNDED" }
        when (selectedPeriod) {
            "Hari Ini" -> valid.filter { now - it.createdAt <= 24 * 3600 * 1000L }
            "Minggu Ini" -> valid.filter { now - it.createdAt <= 7 * 24 * 3600 * 1000L }
            "Bulan Ini" -> valid.filter { now - it.createdAt <= 30L * 24 * 3600 * 1000L }
            else -> valid
        }
    }

    val totalRevenue = filteredTransactions.sumOf { it.grandTotal }
    val totalCogs = filteredTransactions.sumOf { it.totalCost }
    val grossProfit = (totalRevenue - totalCogs).coerceAtLeast(0L)
    val totalExpenseAmount = expenses.sumOf { it.amount }
    val estimatedNetProfit = grossProfit - totalExpenseAmount
    val inventoryAssetValue = products.filter { !it.isService }.sumOf { it.buyPrice * it.stock }
    val lowStockProducts = products.filter { !it.isService && it.stock <= it.minStock }
    val pendingSyncCount = transactions.count { it.syncStatus == "PENDING_SYNC" }
    val totalReceivables = customers.sumOf { it.receivableBalance }
    val totalSupplierDebt = suppliers.sumOf { it.debtBalance }

    // Payment Method Breakdown
    val methodTotals = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.paymentMethod }
            .mapValues { entry -> entry.value.sumOf { it.grandTotal } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Best Selling Products
    val topProducts = remember(allItems) {
        allItems.groupBy { it.productName }
            .map { (name, list) ->
                Triple(name, list.sumOf { it.quantity }, list.sumOf { it.subtotal })
            }
            .sortedByDescending { it.second }
            .take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Financial Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NavyPrimary, NavySecondary, ElectricBlue)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "RINGKASAN EKSEKUTIF & ANALITIK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF93C5FD)
                                )
                                Text(
                                    FormatUtils.formatRupiah(totalRevenue),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Total Omzet Penjualan (${filteredTransactions.size} Transaksi)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFDBEAFE)
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val csv = ReceiptExportHelper.buildFinancialCsvReport(
                                        transactions = filteredTransactions,
                                        totalRevenue = totalRevenue,
                                        totalGrossProfit = grossProfit,
                                        totalExpenses = totalExpenseAmount,
                                        netProfit = estimatedNetProfit
                                    )
                                    ReceiptExportHelper.shareText(
                                        context = context,
                                        title = "Export Laporan Keuangan SUPER POS (.CSV)",
                                        content = csv
                                    )
                                },
                                modifier = Modifier.testTag("export_report_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HeroMetricItem("Laba Kotor", FormatUtils.formatCompactRupiah(grossProfit), Color(0xFF6EE7B7))
                            HeroMetricItem("Pengeluaran", FormatUtils.formatCompactRupiah(totalExpenseAmount), Color(0xFFFCA5A5))
                            HeroMetricItem("Laba Bersih", FormatUtils.formatCompactRupiah(estimatedNetProfit), Color.White)
                            HeroMetricItem("Nilai Stok", FormatUtils.formatCompactRupiah(inventoryAssetValue), Color(0xFFFDE68A))
                        }
                    }
                }
            }
        }

        // Period Filter & Sync Alert
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(periods) { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period) }
                        )
                    }
                }

                if (pendingSyncCount > 0) {
                    AssistChip(
                        onClick = { viewModel.syncNow() },
                        label = { Text("$pendingSyncCount Offline • Sync") },
                        leadingIcon = {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberWarning)
                        }
                    )
                }
            }
        }

        // Secondary Financial Cards Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Saldo Kas Shift Aktif",
                        value = FormatUtils.formatRupiah(activeShift?.expectedEndingCash ?: 0L),
                        subtitle = "Kasir: ${activeShift?.cashierName ?: "Belum dibuka"}",
                        accentColor = TealSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Produk Hampir Habis",
                        value = "${lowStockProducts.size} SKU",
                        subtitle = "Perlu restock segera",
                        accentColor = if (lowStockProducts.isNotEmpty()) AmberWarning else TealSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Total Piutang Pelanggan",
                        value = FormatUtils.formatRupiah(totalReceivables),
                        subtitle = "${customers.count { it.receivableBalance > 0 }} pelanggan tempo",
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Utang ke Supplier",
                        value = FormatUtils.formatRupiah(totalSupplierDebt),
                        subtitle = "${suppliers.count { it.debtBalance > 0 }} faktur PO tempo",
                        accentColor = CoralError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Visual Revenue vs Cost vs Profit Bar Chart + Payment Method Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Grafik Perbandingan Omzet, HPP, dan Laba", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    FinancialComparisonChart(
                        revenue = totalRevenue,
                        cogs = totalCogs,
                        grossProfit = grossProfit,
                        expenses = totalExpenseAmount
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Distribusi Metode Pembayaran", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (methodTotals.isEmpty()) {
                        Text("Belum ada data metode pembayaran.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        methodTotals.forEach { (method, amt) ->
                            val ratio = if (totalRevenue > 0) (amt.toFloat() / totalRevenue.toFloat()).coerceIn(0f, 1f) else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(method, style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        "${FormatUtils.formatRupiah(amt)} (${(ratio * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = ElectricBlue,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Selling Products & Low Stock Alert
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Produk Terlaris", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    topProducts.forEachIndexed { idx, (name, qty, omzet) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = ElectricBlue.copy(alpha = 0.15f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${idx + 1}", fontWeight = FontWeight.Bold, color = ElectricBlue)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(name, style = MaterialTheme.typography.titleSmall)
                                    Text("$qty Terjual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(FormatUtils.formatRupiah(omzet), fontWeight = FontWeight.Bold, color = TealSuccess)
                        }
                    }
                }
            }
        }

        // Complete Transaction History List
        item {
            Text(
                "Riwayat Transaksi Lengkap (Ketuk untuk Struk / Retur)",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(transactions, key = { it.id }) { trx ->
            TransactionHistoryCard(
                transaction = trx,
                onClick = { viewModel.openReceiptForTransaction(trx) }
            )
        }
    }
}

@Composable
private fun HeroMetricItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFCBD5E1))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FinancialComparisonChart(
    revenue: Long,
    cogs: Long,
    grossProfit: Long,
    expenses: Long
) {
    val maxVal = maxOf(revenue, cogs, grossProfit, expenses, 1L).toFloat()
    val bars = listOf(
        Triple("Omzet", revenue, ElectricBlue),
        Triple("Modal HPP", cogs, AmberWarning),
        Triple("Laba Kotor", grossProfit, TealSuccess),
        Triple("Biaya Ops", expenses, CoralError)
    )

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 8.dp)
        ) {
            val barWidth = size.width / (bars.size * 2.2f)
            val spacing = size.width / bars.size
            bars.forEachIndexed { index, (_, amount, color) ->
                val ratio = (amount.toFloat() / maxVal).coerceIn(0.05f, 1f)
                val barHeight = size.height * ratio
                val left = index * spacing + (spacing - barWidth) / 2f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bars.forEach { (label, amount, color) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                    Text(
                        FormatUtils.formatCompactRupiah(amount),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionHistoryCard(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(transaction.invoiceNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (transaction.paymentStatus) {
                            "LUNAS" -> TealSuccess.copy(alpha = 0.15f)
                            "REFUNDED" -> CoralError.copy(alpha = 0.15f)
                            else -> AmberWarning.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = transaction.paymentStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (transaction.paymentStatus) {
                                "LUNAS" -> TealSuccess
                                "REFUNDED" -> CoralError
                                else -> Color(0xFFB45309)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (transaction.syncStatus == "PENDING_SYNC") {
                        Text("• Offline", style = MaterialTheme.typography.labelSmall, color = AmberWarning)
                    }
                }
                Text(
                    "${transaction.customerName} • ${transaction.orderType} • Kasir: ${transaction.cashierName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    FormatUtils.formatDateTime(transaction.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    FormatUtils.formatRupiah(transaction.grandTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                Text(
                    transaction.paymentMethod,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
