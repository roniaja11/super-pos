package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.database.*
import com.example.core.util.FormatUtils
import com.example.ui.SuperPosViewModel
import com.example.ui.theme.*

@Composable
fun BusinessOperationsScreen(
    viewModel: SuperPosViewModel,
    modifier: Modifier = Modifier
) {
    val activeShift by viewModel.activeShift.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val cashMovements by viewModel.cashMovements.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val activeEmployee by viewModel.activeEmployee.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isOnlineMode by viewModel.isOnlineMode.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Shift & Kas",
        "Pembelian & Supplier",
        "Pelanggan & Piutang",
        "Biaya & Keuangan",
        "Karyawan & Cabang"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> ShiftAndCashModule(
                activeShift = activeShift,
                shifts = shifts,
                cashMovements = cashMovements,
                onOpenShift = viewModel::openShift,
                onRecordCash = viewModel::recordShiftCashMovement,
                onCloseShift = viewModel::closeShift
            )

            1 -> PurchasingAndSupplierModule(
                suppliers = suppliers,
                products = products,
                purchaseOrders = purchaseOrders,
                onCreatePo = viewModel::createPurchaseOrder,
                onPaySupplierDebt = viewModel::paySupplierDebt,
                onSaveSupplier = viewModel::saveSupplier
            )

            2 -> CustomersAndLoyaltyModule(
                customers = customers,
                onSaveCustomer = viewModel::saveCustomer,
                onPayReceivable = viewModel::payCustomerReceivable
            )

            3 -> ExpensesAndFinanceModule(
                expenses = expenses,
                onAddExpense = viewModel::addExpense
            )

            4 -> EmployeesAndBranchesModule(
                employees = employees,
                activeEmployee = activeEmployee,
                branches = branches,
                selectedBranchId = selectedBranchId,
                isDarkMode = isDarkMode,
                isOnlineMode = isOnlineMode,
                onSwitchEmployee = viewModel::switchActiveEmployee,
                onSaveEmployee = viewModel::saveEmployee,
                onSelectBranch = viewModel::selectBranch,
                onSaveBranch = viewModel::saveBranch,
                onToggleDarkMode = viewModel::toggleDarkMode,
                onToggleOnlineMode = viewModel::toggleOnlineMode
            )
        }
    }
}

@Composable
private fun ShiftAndCashModule(
    activeShift: CashierShiftEntity?,
    shifts: List<CashierShiftEntity>,
    cashMovements: List<CashMovementEntity>,
    onOpenShift: (Long) -> Unit,
    onRecordCash: (String, String, Long, String) -> Unit,
    onCloseShift: (Long, String) -> Unit
) {
    var initialCashInput by remember { mutableStateOf("500000") }
    var showCashDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            if (activeShift == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Buka Shift Kasir Baru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = initialCashInput,
                            onValueChange = { initialCashInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Modal Kas Awal di Laci (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onOpenShift(initialCashInput.toLongOrNull() ?: 500000L) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Shift Sekarang")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SHIFT KASIR AKTIF", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                                Text(activeShift.cashierName, style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Text(activeShift.branchName, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                            }
                            Badge(containerColor = TealSuccess, contentColor = Color.White) {
                                Text("OPEN", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Modal Awal:", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                            Text(FormatUtils.formatRupiah(activeShift.initialCash), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Penjualan Tunai:", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                            Text("+${FormatUtils.formatRupiah(activeShift.totalCashSales)}", color = TealSuccess, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Penjualan Non-Tunai (QRIS/EDC):", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                            Text(FormatUtils.formatRupiah(activeShift.totalNonCashSales), color = Color(0xFF93C5FD), fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mutasi Kas Masuk / Keluar:", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                            Text(
                                "+${FormatUtils.formatCompactRupiah(activeShift.totalCashIn)} / -${FormatUtils.formatCompactRupiah(activeShift.totalCashOut)}",
                                color = Color.White
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saldo Kas Fisik Seharusnya:", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                FormatUtils.formatRupiah(activeShift.expectedEndingCash),
                                style = MaterialTheme.typography.titleMedium,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCashDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kas Masuk/Keluar")
                            }
                            Button(
                                onClick = { showCloseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tutup Shift")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Riwayat Kas Masuk / Kas Keluar Laci", style = MaterialTheme.typography.titleMedium)
        }

        items(cashMovements, key = { it.id }) { cm ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cm.category, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(cm.description, style = MaterialTheme.typography.bodySmall)
                        Text("${cm.createdBy} • ${FormatUtils.formatDateTime(cm.timestamp)}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        text = (if (cm.type == "CASH_IN") "+" else "-") + FormatUtils.formatRupiah(cm.amount),
                        fontWeight = FontWeight.Bold,
                        color = if (cm.type == "CASH_IN") TealSuccess else CoralError
                    )
                }
            }
        }

        item {
            Text("Riwayat Penutupan & Audit Shift Kasir", style = MaterialTheme.typography.titleMedium)
        }

        items(shifts.filter { it.status == "CLOSED" }, key = { it.id }) { s ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${s.cashierName} • ${s.branchName}", fontWeight = FontWeight.Bold)
                    Text(
                        "Saldo Sistem: ${FormatUtils.formatRupiah(s.expectedEndingCash)} • Fisik Aktual: ${FormatUtils.formatRupiah(s.actualPhysicalCash ?: 0L)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Selisih Kas: ${FormatUtils.formatRupiah(s.cashDifference)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (s.cashDifference == 0L) TealSuccess else CoralError
                    )
                }
            }
        }
    }

    if (showCashDialog) {
        var type by remember { mutableStateOf("CASH_OUT") }
        var category by remember { mutableStateOf("Biaya Operasional Toko") }
        var amount by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCashDialog = false },
            title = { Text("Catat Kas Masuk / Kas Keluar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = type == "CASH_IN", onClick = { type = "CASH_IN" }, label = { Text("Kas Masuk") })
                        FilterChip(selected = type == "CASH_OUT", onClick = { type = "CASH_OUT" }, label = { Text("Kas Keluar") })
                    }
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") })
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Keterangan") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toLongOrNull() ?: 0L
                        if (amt > 0) onRecordCash(type, category, amt, desc)
                        showCashDialog = false
                    }
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showCashDialog = false }) { Text("Batal") } }
        )
    }

    if (showCloseDialog && activeShift != null) {
        var physicalInput by remember { mutableStateOf(activeShift.expectedEndingCash.toString()) }
        var notes by remember { mutableStateOf("Setoran kasir sesuai") }
        val actual = physicalInput.toLongOrNull() ?: 0L
        val diff = actual - activeShift.expectedEndingCash

        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Tutup Shift & Audit Uang Fisik") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saldo Sistem Seharusnya: ${FormatUtils.formatRupiah(activeShift.expectedEndingCash)}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = physicalInput,
                        onValueChange = { physicalInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Hasil Hitung Uang Tunai Fisik (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Selisih Kas: ${FormatUtils.formatRupiah(diff)}",
                        color = if (diff == 0L) TealSuccess else CoralError,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Penutupan Shift") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCloseShift(actual, notes)
                        showCloseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) { Text("Konfirmasi Tutup Shift") }
            },
            dismissButton = { TextButton(onClick = { showCloseDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun PurchasingAndSupplierModule(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    purchaseOrders: List<PurchaseOrderEntity>,
    onCreatePo: (SupplierEntity, ProductEntity, Int, Long, String) -> Unit,
    onPaySupplierDebt: (SupplierEntity, Long) -> Unit,
    onSaveSupplier: (SupplierEntity) -> Unit
) {
    var showCreatePoDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showCreatePoDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Terima Barang (PO)")
                }
                OutlinedButton(
                    onClick = { showAddSupplierDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Supplier")
                }
            }
        }

        item { Text("Daftar Supplier & Saldo Utang", style = MaterialTheme.typography.titleMedium) }

        items(suppliers, key = { it.id }) { sup ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sup.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${sup.contactPerson} • ${sup.phone}", style = MaterialTheme.typography.bodySmall)
                        Text(sup.categorySupply, style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Utang: ${FormatUtils.formatRupiah(sup.debtBalance)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (sup.debtBalance > 0) CoralError else TealSuccess,
                            fontWeight = FontWeight.Bold
                        )
                        if (sup.debtBalance > 0) {
                            TextButton(onClick = { onPaySupplierDebt(sup, sup.debtBalance) }) {
                                Text("Lunasi Utang")
                            }
                        }
                    }
                }
            }
        }

        item { Text("Riwayat Purchase Order & Penerimaan Stok", style = MaterialTheme.typography.titleMedium) }

        items(purchaseOrders, key = { it.id }) { po ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${po.poNumber} • ${po.productName}", fontWeight = FontWeight.SemiBold)
                        Text("Supplier: ${po.supplierName} • Qty: +${po.quantity}", style = MaterialTheme.typography.bodySmall)
                        Text(FormatUtils.formatDateTime(po.timestamp), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(FormatUtils.formatRupiah(po.totalAmount), fontWeight = FontWeight.Bold, color = ElectricBlue)
                        Text("${po.paymentType} • ${po.status}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showCreatePoDialog && suppliers.isNotEmpty() && products.isNotEmpty()) {
        var selectedSup by remember { mutableStateOf(suppliers.first()) }
        var selectedProd by remember { mutableStateOf(products.first { !it.isService }) }
        var qtyInput by remember { mutableStateOf("20") }
        var buyPriceInput by remember(selectedProd) { mutableStateOf(selectedProd.buyPrice.toString()) }
        var paymentType by remember { mutableStateOf("TUNAI") }

        AlertDialog(
            onDismissRequest = { showCreatePoDialog = false },
            title = { Text("Purchase Order & Penerimaan Barang") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih Supplier:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suppliers) { s ->
                            FilterChip(selected = selectedSup.id == s.id, onClick = { selectedSup = s }, label = { Text(s.name) })
                        }
                    }
                    Text("Pilih Produk:", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(products.filter { !it.isService }) { p ->
                            FilterChip(
                                selected = selectedProd.id == p.id,
                                onClick = {
                                    selectedProd = p
                                    buyPriceInput = p.buyPrice.toString()
                                },
                                label = { Text(p.name) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Kuantitas Masuk (${selectedProd.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = buyPriceInput,
                        onValueChange = { buyPriceInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Beli Satuan (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = paymentType == "TUNAI", onClick = { paymentType = "TUNAI" }, label = { Text("TUNAI / LUNAS") })
                        FilterChip(selected = paymentType == "TEMPO", onClick = { paymentType = "TEMPO" }, label = { Text("TEMPO (UTANG)") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = qtyInput.toIntOrNull() ?: 1
                        val bp = buyPriceInput.toLongOrNull() ?: selectedProd.buyPrice
                        onCreatePo(selectedSup, selectedProd, q, bp, paymentType)
                        showCreatePoDialog = false
                    }
                ) { Text("Terima Barang & Tambah Stok") }
            },
            dismissButton = { TextButton(onClick = { showCreatePoDialog = false }) { Text("Batal") } }
        )
    }

    if (showAddSupplierDialog) {
        var name by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var supply by remember { mutableStateOf("Sembako & Barang Grosir") }

        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text("Tambah Supplier Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Perusahaan Supplier") })
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Nama Sales / Kontak") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor Telepon / WA") })
                    OutlinedTextField(value = supply, onValueChange = { supply = it }, label = { Text("Kategori Barang") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSupplier(
                            SupplierEntity(
                                name = name.trim(),
                                contactPerson = contact.trim(),
                                phone = phone.trim(),
                                address = "Indonesia",
                                categorySupply = supply.trim()
                            )
                        )
                        showAddSupplierDialog = false
                    },
                    enabled = name.isNotBlank()
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showAddSupplierDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun CustomersAndLoyaltyModule(
    customers: List<CustomerEntity>,
    onSaveCustomer: (CustomerEntity) -> Unit,
    onPayReceivable: (CustomerEntity, Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Pelanggan / Member Loyalty")
            }
        }

        items(customers, key = { it.id }) { cust ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cust.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${cust.phone} • ${cust.address}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Tier: ${cust.tier} (Diskon ${cust.discountPercent}%) • Poin: ${cust.loyaltyPoints} pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                        Text(
                            "Total Belanja: ${FormatUtils.formatRupiah(cust.totalSpend)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (cust.receivableBalance > 0) {
                            Text(
                                "Piutang: ${FormatUtils.formatRupiah(cust.receivableBalance)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = CoralError,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { onPayReceivable(cust, cust.receivableBalance) }) {
                                Text("Terima Pelunasan")
                            }
                        } else {
                            Text("Bebas Piutang", style = MaterialTheme.typography.labelSmall, color = TealSuccess)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var tier by remember { mutableStateOf("Silver") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Daftar Member Pelanggan Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Pelanggan") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor WhatsApp") })
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat") })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Reguler" to 0, "Silver" to 2, "Gold" to 3, "VIP" to 5).forEach { (t, _) ->
                            FilterChip(selected = tier == t, onClick = { tier = t }, label = { Text(t) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val disc = when (tier) {
                            "VIP" -> 5
                            "Gold" -> 3
                            "Silver" -> 2
                            else -> 0
                        }
                        onSaveCustomer(
                            CustomerEntity(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                tier = tier,
                                discountPercent = disc
                            )
                        )
                        showAddDialog = false
                    },
                    enabled = name.isNotBlank()
                ) { Text("Simpan Member") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun ExpensesAndFinanceModule(
    expenses: List<ExpenseEntity>,
    onAddExpense: (String, String, Long, String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CoralError)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Catat Biaya Operasional / Pengeluaran Bisnis")
            }
        }

        items(expenses, key = { it.id }) { exp ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(exp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${exp.category} • Metode: ${exp.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                        Text("${exp.recordedBy} • ${FormatUtils.formatDateTime(exp.timestamp)}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        "-${FormatUtils.formatRupiah(exp.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CoralError
                    )
                }
            }
        }
    }

    if (showDialog) {
        var category by remember { mutableStateOf("Biaya Operasional") }
        var title by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var method by remember { mutableStateOf("TUNAI") }
        val categories = listOf("Biaya Operasional", "Listrik & Air", "Sewa Tempat", "Gaji Karyawan", "Transportasi")

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Input Pengeluaran Bisnis") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { c ->
                            FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Pengeluaran") })
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Biaya (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amount.toLongOrNull() ?: 0L
                        if (amt > 0 && title.isNotBlank()) {
                            onAddExpense(category, title.trim(), amt, method, "")
                            showDialog = false
                        }
                    }
                ) { Text("Simpan Pengeluaran") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun EmployeesAndBranchesModule(
    employees: List<EmployeeEntity>,
    activeEmployee: EmployeeEntity?,
    branches: List<BranchEntity>,
    selectedBranchId: String,
    isDarkMode: Boolean,
    isOnlineMode: Boolean,
    onSwitchEmployee: (EmployeeEntity) -> Unit,
    onSaveEmployee: (EmployeeEntity) -> Unit,
    onSelectBranch: (BranchEntity) -> Unit,
    onSaveBranch: (BranchEntity) -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleOnlineMode: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pengaturan Sistem & Tema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tema Gelap (Dark Mode Kasir Malam)", style = MaterialTheme.typography.titleSmall)
                            Text("Navy gelap & Charcoal kontras tinggi", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = isDarkMode, onCheckedChange = { onToggleDarkMode() })
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sinkronisasi Cloud Real-Time", style = MaterialTheme.typography.titleSmall)
                            Text("Matikan untuk menguji antrean transaksi Offline-First", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = isOnlineMode, onCheckedChange = { onToggleOnlineMode() })
                    }
                }
            }
        }

        item {
            Text("Multi-Cabang & Konfigurasi Jenis Usaha", style = MaterialTheme.typography.titleMedium)
        }

        items(branches, key = { it.id }) { branch ->
            val isSelected = branch.id == selectedBranchId
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${branch.name} (${branch.code})", fontWeight = FontWeight.Bold)
                        Text(branch.address, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Mode: ${branch.businessType} • PPN: ${branch.taxPercentage}% • Service: ${branch.serviceChargePercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )
                    }
                    Button(
                        onClick = { onSelectBranch(branch) },
                        enabled = !isSelected
                    ) {
                        Text(if (isSelected) "Aktif" else "Pilih")
                    }
                }
            }
        }

        item {
            Text("Manajemen Karyawan & Hak Akses (RBAC)", style = MaterialTheme.typography.titleMedium)
        }

        items(employees, key = { it.id }) { emp ->
            val isCurrent = activeEmployee?.id == emp.id
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${emp.name} (${emp.role})", fontWeight = FontWeight.Bold)
                        Text(
                            "Izin: ${if (emp.canEditPrice) "Ubah Harga • " else ""}${if (emp.canGiveDiscount) "Diskon • " else ""}${if (emp.canRefund) "Refund • " else ""}${if (emp.canViewReports) "Laporan" else "Kasir"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(
                        onClick = { onSwitchEmployee(emp) },
                        enabled = !isCurrent
                    ) {
                        Text(if (isCurrent) "Sedang Login" else "Gunakan Akun")
                    }
                }
            }
        }
    }
}
