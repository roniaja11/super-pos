package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.database.CategoryEntity
import com.example.core.database.ProductEntity
import com.example.core.database.StockMovementEntity
import com.example.core.util.FormatUtils
import com.example.ui.SuperPosViewModel
import com.example.ui.theme.*

@Composable
fun InventoryProductsScreen(
    viewModel: SuperPosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val stockMovements by viewModel.stockMovements.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Katalog Produk", "Stok & Opname", "Riwayat Kartu Stok", "Kategori")

    var showProductForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var adjustingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (activeSubTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductForm = true
                    },
                    containerColor = ElectricBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Produk") },
                    modifier = Modifier.testTag("add_product_fab")
                )
            } else if (activeSubTab == 3) {
                ExtendedFloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    containerColor = ElectricBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                    text = { Text("Kategori Baru") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                edgePadding = 0.dp
            ) {
                subTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeSubTab == index,
                        onClick = { activeSubTab = index },
                        text = { Text(title, fontWeight = if (activeSubTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeSubTab) {
                0 -> ProductCatalogManageList(
                    products = products,
                    onEdit = {
                        editingProduct = it
                        showProductForm = true
                    },
                    onAdjustStock = { adjustingProduct = it },
                    onPrintLabel = { prod ->
                        viewModel.showMessage("Mencetak Label Harga & Barcode ${prod.barcode} (${prod.name})…")
                    }
                )

                1 -> StockOpnameList(
                    products = products,
                    onAdjustStock = { adjustingProduct = it }
                )

                2 -> StockMovementsHistoryList(movements = stockMovements)

                3 -> CategoryManageList(categories = categories)
            }
        }
    }

    if (showProductForm) {
        ProductFormDialog(
            initialProduct = editingProduct,
            categories = categories,
            onDismiss = { showProductForm = false },
            onSave = { prod, isNew ->
                viewModel.saveProduct(prod, isNew)
                showProductForm = false
            },
            onArchive = { prod ->
                viewModel.archiveProduct(prod)
                showProductForm = false
            }
        )
    }

    adjustingProduct?.let { prod ->
        StockAdjustDialog(
            product = prod,
            onDismiss = { adjustingProduct = null },
            onConfirm = { newStock, type, notes ->
                viewModel.adjustStock(prod, newStock, type, notes)
                adjustingProduct = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, sector ->
                viewModel.addCategory(name, 0xFF2563EB, sector)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun ProductCatalogManageList(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onAdjustStock: (ProductEntity) -> Unit,
    onPrintLabel: (ProductEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 84.dp)
    ) {
        items(products, key = { it.id }) { prod ->
            val margin = prod.sellPrice - prod.buyPrice
            val marginPct = if (prod.sellPrice > 0) ((margin * 100) / prod.sellPrice) else 0L
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "SKU: ${prod.sku} • Barcode: ${prod.barcode} • ${prod.categoryName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (prod.batchNumber.isNotBlank() || prod.expiryDate.isNotBlank()) {
                                Text(
                                    "Batch: ${prod.batchNumber} • Exp: ${prod.expiryDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberWarning
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (prod.isService) CyanAccent.copy(alpha = 0.15f)
                            else if (prod.stock <= prod.minStock) AmberWarning.copy(alpha = 0.2f)
                            else TealSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (prod.isService) "Layanan Jasa" else "${prod.stock} ${prod.unit}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (prod.isService) CyanAccent
                                else if (prod.stock <= prod.minStock) Color(0xFFB45309)
                                else TealSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Harga Modal (HPP)", style = MaterialTheme.typography.labelSmall)
                            Text(FormatUtils.formatRupiah(prod.buyPrice), style = MaterialTheme.typography.bodyMedium)
                        }
                        Column {
                            Text("Harga Jual Ecer", style = MaterialTheme.typography.labelSmall)
                            Text(
                                FormatUtils.formatRupiah(prod.sellPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }
                        Column {
                            Text("Margin Laba", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "+${FormatUtils.formatRupiah(margin)} ($marginPct%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TealSuccess
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onPrintLabel(prod) }) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cetak Label")
                        }
                        if (!prod.isService) {
                            TextButton(onClick = { onAdjustStock(prod) }) {
                                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sesuaikan Stok")
                            }
                        }
                        FilledTonalButton(onClick = { onEdit(prod) }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockOpnameList(
    products: List<ProductEntity>,
    onAdjustStock: (ProductEntity) -> Unit
) {
    val physicalProducts = products.filter { !it.isService }
    val lowStockCount = physicalProducts.count { it.stock <= it.minStock }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.14f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFB45309))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Peringatan Stok Minimum ($lowStockCount Produk Menipis)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Ketuk tombol Opname / Sesuaikan pada produk untuk mencatat stok masuk, rusak, atau hasil hitung fisik.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        items(physicalProducts, key = { it.id }) { prod ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Stok Saat Ini: ${prod.stock} ${prod.unit} (Min: ${prod.minStock} ${prod.unit})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (prod.stock <= prod.minStock) CoralError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = { onAdjustStock(prod) }) {
                        Text("Opname / Mutasi")
                    }
                }
            }
        }
    }
}

@Composable
private fun StockMovementsHistoryList(movements: List<StockMovementEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(movements, key = { it.id }) { mv ->
            val isPositive = mv.qtyChange >= 0
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mv.productName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${mv.type} • Ref: ${mv.referenceNumber} • Oleh: ${mv.performedBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(mv.notes, style = MaterialTheme.typography.bodySmall)
                        Text(
                            FormatUtils.formatDateTime(mv.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPositive) "+${mv.qtyChange}" else "${mv.qtyChange}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) TealSuccess else CoralError
                        )
                        Text(
                            "Saldo: ${mv.currentStock}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryManageList(categories: List<CategoryEntity>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 84.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Sektor Bisnis: ${cat.businessSector}", style = MaterialTheme.typography.bodySmall)
                    }
                    AssistChip(onClick = {}, label = { Text(cat.businessSector) })
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    initialProduct: ProductEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity, Boolean) -> Unit,
    onArchive: (ProductEntity) -> Unit
) {
    val isNew = initialProduct == null
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "PRD-${(100..999).random()}") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "899${(1000000000L..9999999999L).random()}") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull { it.id == initialProduct?.categoryId } ?: categories.firstOrNull()) }
    var buyPrice by remember { mutableStateOf((initialProduct?.buyPrice ?: 10000L).toString()) }
    var sellPrice by remember { mutableStateOf((initialProduct?.sellPrice ?: 15000L).toString()) }
    var wholesalePrice by remember { mutableStateOf((initialProduct?.wholesalePrice ?: 13500L).toString()) }
    var stock by remember { mutableStateOf((initialProduct?.stock ?: 25).toString()) }
    var minStock by remember { mutableStateOf((initialProduct?.minStock ?: 5).toString()) }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var isService by remember { mutableStateOf(initialProduct?.isService ?: false) }
    var batchNumber by remember { mutableStateOf(initialProduct?.batchNumber ?: "") }
    var expiryDate by remember { mutableStateOf(initialProduct?.expiryDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Tambah Produk / Jasa Baru" else "Edit Produk") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk / Layanan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("Kode SKU") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Satuan (Pcs/Cup/Kg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Nomor Barcode EAN-13") },
                    trailingIcon = {
                        IconButton(onClick = { barcode = "899${(1000000000L..9999999999L).random()}" }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Generate Barcode")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Pilih Kategori:", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory?.id == cat.id,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { buyPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Modal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = wholesalePrice,
                    onValueChange = { wholesalePrice = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Grosir Otomatis (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isService, onCheckedChange = { isService = it })
                    Text("Produk Jasa / Non-Stok Fisik")
                }
                if (!isService) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Stok Saat Ini") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { minStock = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Batas Stok Minimum") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = batchNumber,
                            onValueChange = { batchNumber = it },
                            label = { Text("Nomor Batch (Opsional)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Kedaluwarsa (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (!isNew && initialProduct != null) {
                    OutlinedButton(
                        onClick = { onArchive(initialProduct) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Arsipkan / Nonaktifkan Produk")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cat = selectedCategory
                    val entity = ProductEntity(
                        id = initialProduct?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name.trim(),
                        sku = sku.trim(),
                        barcode = barcode.trim(),
                        categoryId = cat?.id ?: "cat-sembako",
                        categoryName = cat?.name ?: "Umum",
                        buyPrice = buyPrice.toLongOrNull() ?: 0L,
                        sellPrice = sellPrice.toLongOrNull() ?: 0L,
                        wholesalePrice = wholesalePrice.toLongOrNull() ?: 0L,
                        stock = if (isService) 999 else (stock.toIntOrNull() ?: 0),
                        minStock = minStock.toIntOrNull() ?: 5,
                        unit = unit.ifBlank { "Pcs" },
                        isService = isService,
                        batchNumber = batchNumber.trim(),
                        expiryDate = expiryDate.trim()
                    )
                    onSave(entity, isNew)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Simpan Produk")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun StockAdjustDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var newStockInput by remember { mutableStateOf(product.stock.toString()) }
    var selectedType by remember { mutableStateOf("OPNAME") }
    var notesInput by remember { mutableStateOf("Penyesuaian hasil cek fisik gudang") }
    val types = listOf("OPNAME", "ADJUSTMENT_IN", "ADJUSTMENT_OUT", "TRANSFER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stok Opname & Penyesuaian: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Stok tercatat di sistem: ${product.stock} ${product.unit}")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t) }
                        )
                    }
                }
                OutlinedTextField(
                    value = newStockInput,
                    onValueChange = { newStockInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Stok Fisik Aktual (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan Audit Pergerakan Stok") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = newStockInput.toIntOrNull() ?: product.stock
                    onConfirm(s, selectedType, notesInput)
                }
            ) {
                Text("Simpan Mutasi Stok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("RETAIL") }
    val sectors = listOf("RETAIL", "FNB", "PHARMACY", "SERVICE", "ALL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kategori Produk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Sektor Usaha:", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sectors) { s ->
                        FilterChip(
                            selected = sector == s,
                            onClick = { sector = s },
                            label = { Text(s) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), sector) },
                enabled = name.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
