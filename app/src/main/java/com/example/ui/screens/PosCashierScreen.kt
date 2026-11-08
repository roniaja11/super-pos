package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.CategoryEntity
import com.example.core.database.CustomerEntity
import com.example.core.database.HeldOrderEntity
import com.example.core.database.ProductEntity
import com.example.core.util.FormatUtils
import com.example.data.CartItem
import com.example.ui.SuperPosViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosCashierScreen(
    viewModel: SuperPosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val globalDiscount by viewModel.globalDiscount.collectAsState()
    val isTaxEnabled by viewModel.isTaxEnabled.collectAsState()
    val heldOrders by viewModel.heldOrders.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val activeEmployee by viewModel.activeEmployee.collectAsState()

    var showCartModal by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showHeldOrdersDialog by remember { mutableStateOf(false) }
    var editingCartItem by remember { mutableStateOf<CartItem?>(null) }

    val activeBranch = branches.firstOrNull { it.id == selectedBranchId } ?: branches.firstOrNull()
    val filteredProducts = remember(products, searchQuery, selectedCategoryId) {
        products.filter { prod ->
            val matchesCat = selectedCategoryId == "ALL" || prod.categoryId == selectedCategoryId
            val matchesQuery = searchQuery.isBlank() ||
                prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.sku.contains(searchQuery, ignoreCase = true) ||
                prod.barcode.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    val subtotal = cartItems.sumOf { it.lineSubtotal }
    val memberDiscount = selectedCustomer?.let { (subtotal * it.discountPercent) / 100L } ?: 0L
    val totalDiscount = globalDiscount + memberDiscount
    val taxableBase = (subtotal - totalDiscount).coerceAtLeast(0L)
    val taxRate = if (isTaxEnabled) (activeBranch?.taxPercentage ?: 11) else 0
    val taxAmount = (taxableBase * taxRate) / 100L
    val grandTotal = (taxableBase + taxAmount).coerceAtLeast(0L)
    val totalQty = cartItems.sumOf { it.quantity }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    PosTopControls(
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::updateSearchQuery,
                        onOpenScanner = { showBarcodeDialog = true },
                        heldCount = heldOrders.size,
                        onOpenHeldOrders = { showHeldOrdersDialog = true },
                        orderType = orderType,
                        onSelectOrderType = { viewModel.setOrderType(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryFilterRow(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onSelectCategory = viewModel::selectCategory
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ProductCatalogGrid(
                        products = filteredProducts,
                        cartItems = cartItems,
                        onProductClick = viewModel::addToCart,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight(),
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp
                ) {
                    CartPanelContent(
                        cartItems = cartItems,
                        customers = customers,
                        selectedCustomer = selectedCustomer,
                        onSelectCustomer = viewModel::selectCustomer,
                        subtotal = subtotal,
                        totalDiscount = totalDiscount,
                        taxRate = taxRate,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        isTaxEnabled = isTaxEnabled,
                        onToggleTax = viewModel::toggleTax,
                        onUpdateQty = viewModel::updateCartItemQuantity,
                        onEditItem = { editingCartItem = it },
                        onClearCart = viewModel::clearCart,
                        onHoldOrder = viewModel::holdCurrentOrder,
                        onPayClick = { showPaymentDialog = true }
                    )
                }
            }
        } else {
            // Compact / Phone layout with floating bottom checkout bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                PosTopControls(
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::updateSearchQuery,
                    onOpenScanner = { showBarcodeDialog = true },
                    heldCount = heldOrders.size,
                    onOpenHeldOrders = { showHeldOrdersDialog = true },
                    orderType = orderType,
                    onSelectOrderType = { viewModel.setOrderType(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryFilterRow(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = viewModel::selectCategory
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProductCatalogGrid(
                    products = filteredProducts,
                    cartItems = cartItems,
                    onProductClick = viewModel::addToCart,
                    modifier = Modifier.weight(1f)
                )

                AnimatedVisibility(visible = cartItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                            .testTag("checkout_bottom_bar"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCartModal = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(
                                    containerColor = CyanAccent,
                                    contentColor = NavyPrimary,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text(
                                        text = "$totalQty",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = FormatUtils.formatRupiah(grandTotal),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isTaxEnabled) "Termasuk PPN $taxRate% • Ketuk rincian" else "Tanpa PPN • Ketuk rincian",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF93C5FD)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showCartModal = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Keranjang")
                                }
                                Button(
                                    onClick = { showPaymentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealSuccess),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("pay_now_button")
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bayar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCartModal) {
        ModalBottomSheet(
            onDismissRequest = { showCartModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.88f)) {
                CartPanelContent(
                    cartItems = cartItems,
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    onSelectCustomer = viewModel::selectCustomer,
                    subtotal = subtotal,
                    totalDiscount = totalDiscount,
                    taxRate = taxRate,
                    taxAmount = taxAmount,
                    grandTotal = grandTotal,
                    isTaxEnabled = isTaxEnabled,
                    onToggleTax = viewModel::toggleTax,
                    onUpdateQty = viewModel::updateCartItemQuantity,
                    onEditItem = { editingCartItem = it },
                    onClearCart = {
                        viewModel.clearCart()
                        showCartModal = false
                    },
                    onHoldOrder = {
                        viewModel.holdCurrentOrder()
                        showCartModal = false
                    },
                    onPayClick = {
                        showCartModal = false
                        showPaymentDialog = true
                    }
                )
            }
        }
    }

    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            products = products,
            onDismiss = { showBarcodeDialog = false },
            onScanCode = { code ->
                viewModel.scanBarcodeOrSku(code)
                showBarcodeDialog = false
            }
        )
    }

    if (showHeldOrdersDialog) {
        HeldOrdersDialog(
            heldOrders = heldOrders,
            onDismiss = { showHeldOrdersDialog = false },
            onResume = {
                viewModel.resumeHeldOrder(it)
                showHeldOrdersDialog = false
            }
        )
    }

    editingCartItem?.let { item ->
        EditCartItemDialog(
            cartItem = item,
            canEditPrice = activeEmployee?.canEditPrice ?: true,
            globalDiscount = globalDiscount,
            onDismiss = { editingCartItem = null },
            onSave = { customPrice, itemDisc, newGlobalDisc ->
                viewModel.updateCartItemPriceAndDiscount(item.product.id, customPrice, itemDisc)
                viewModel.setGlobalDiscount(newGlobalDisc)
                editingCartItem = null
            }
        )
    }

    if (showPaymentDialog && cartItems.isNotEmpty()) {
        PaymentCheckoutDialog(
            grandTotal = grandTotal,
            subtotal = subtotal,
            discountAmount = totalDiscount,
            taxAmount = taxAmount,
            customer = selectedCustomer,
            onDismiss = { showPaymentDialog = false },
            onConfirmPayment = { method, paid, ref, splits ->
                viewModel.completeCheckout(method, paid, ref, splits)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun PosTopControls(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenScanner: () -> Unit,
    heldCount: Int,
    onOpenHeldOrders: () -> Unit,
    orderType: String,
    onSelectOrderType: (String) -> Unit
) {
    val orderModes = listOf("Langsung / Retail", "Meja #01", "Meja #02", "Meja #05", "Bungkus / Takeaway", "Grosir / B2B")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pos_search_input"),
                placeholder = { Text("Cari nama, SKU, atau scan barcode…", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            FilledIconButton(
                onClick = onOpenScanner,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("barcode_scan_button"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ElectricBlue)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode")
            }

            BadgedBox(
                badge = {
                    if (heldCount > 0) {
                        Badge(containerColor = AmberWarning, contentColor = Color.Black) {
                            Text("$heldCount")
                        }
                    }
                }
            ) {
                FilledTonalIconButton(
                    onClick = onOpenHeldOrders,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("held_orders_button")
                ) {
                    Icon(Icons.Default.PauseCircleOutline, contentDescription = "Transaksi Hold")
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(orderModes) { mode ->
                val selected = orderType == mode
                FilterChip(
                    selected = selected,
                    onClick = { onSelectOrderType(mode) },
                    label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<CategoryEntity>,
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedCategoryId == "ALL",
                onClick = { onSelectCategory("ALL") },
                label = { Text("Semua Produk", fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }
        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategoryId == cat.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat.id) },
                label = { Text(cat.name) }
            )
        }
    }
}

@Composable
private fun ProductCatalogGrid(
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    onProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val qtyMap = remember(cartItems) {
        cartItems.associate { it.product.id to it.quantity }
    }

    if (products.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tidak ada produk yang cocok dengan filter pencarian", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 152.dp),
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(products, key = { it.id }) { product ->
            val inCartQty = qtyMap[product.id] ?: 0
            val isLowStock = !product.isService && product.stock <= product.minStock

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick(product) }
                    .testTag("product_card_${product.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (inCartQty > 0) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (product.isService) {
                                CyanAccent.copy(alpha = 0.15f)
                            } else if (isLowStock) {
                                AmberWarning.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = if (product.isService) "JASA" else "Stok: ${product.stock} ${product.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (product.isService) CyanAccent else if (isLowStock) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (inCartQty > 0) {
                            Badge(containerColor = ElectricBlue, contentColor = Color.White) {
                                Text("${inCartQty}x", modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.sku,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = FormatUtils.formatRupiah(product.sellPrice),
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.wholesalePrice > 0) {
                        Text(
                            text = "Grosir (≥${product.minWholesaleQty}): ${FormatUtils.formatRupiah(product.wholesalePrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealSuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartPanelContent(
    cartItems: List<CartItem>,
    customers: List<CustomerEntity>,
    selectedCustomer: CustomerEntity?,
    onSelectCustomer: (CustomerEntity?) -> Unit,
    subtotal: Long,
    totalDiscount: Long,
    taxRate: Int,
    taxAmount: Long,
    grandTotal: Long,
    isTaxEnabled: Boolean,
    onToggleTax: (Boolean) -> Unit,
    onUpdateQty: (String, Int) -> Unit,
    onEditItem: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onHoldOrder: () -> Unit,
    onPayClick: () -> Unit
) {
    var showCustomerMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keranjang Kasir (${cartItems.sumOf { it.quantity }} Item)",
                style = MaterialTheme.typography.titleLarge
            )
            if (cartItems.isNotEmpty()) {
                Row {
                    TextButton(onClick = onHoldOrder) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hold")
                    }
                    TextButton(
                        onClick = onClearCart,
                        colors = ButtonDefaults.textButtonColors(contentColor = CoralError)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }

        // Customer selector
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomerMenu = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = selectedCustomer?.name ?: "Pelanggan Umum (Tanpa Member)",
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (selectedCustomer != null && selectedCustomer.discountPercent > 0) {
                                Text(
                                    text = "Tier ${selectedCustomer.tier} • Diskon Member ${selectedCustomer.discountPercent}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TealSuccess
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Pelanggan")
                }
            }

            DropdownMenu(
                expanded = showCustomerMenu,
                onDismissRequest = { showCustomerMenu = false }
            ) {
                customers.forEach { cust ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(cust.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Tier: ${cust.tier} • Diskon ${cust.discountPercent}% • Poin: ${cust.loyaltyPoints}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        onClick = {
                            onSelectCustomer(cust)
                            showCustomerMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.ShoppingCartCheckout,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Keranjang belanja masih kosong", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ketuk produk atau scan barcode untuk memulai transaksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, style = MaterialTheme.typography.titleSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "${FormatUtils.formatRupiah(item.effectiveUnitPrice)} / ${item.product.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (item.isWholesaleApplied) {
                                            Text(
                                                text = "• Harga Grosir",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TealSuccess
                                            )
                                        }
                                    }
                                    if (item.itemDiscount > 0) {
                                        Text(
                                            text = "Diskon item: -${FormatUtils.formatRupiah(item.itemDiscount)}/pcs",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AmberWarning
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onEditItem(item) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Ubah Diskon/Harga", modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalIconButton(
                                        onClick = { onUpdateQty(item.product.id, item.quantity - 1) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Kurangi", modifier = Modifier.size(18.dp))
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledIconButton(
                                        onClick = { onUpdateQty(item.product.id, item.quantity + 1) },
                                        modifier = Modifier.size(36.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = ElectricBlue)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(18.dp))
                                    }
                                }

                                Text(
                                    text = FormatUtils.formatRupiah(item.lineSubtotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Summary Calculation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
            Text(FormatUtils.formatRupiah(subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        if (totalDiscount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Diskon", style = MaterialTheme.typography.bodyMedium, color = TealSuccess)
                Text("-${FormatUtils.formatRupiah(totalDiscount)}", style = MaterialTheme.typography.bodyMedium, color = TealSuccess)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isTaxEnabled, onCheckedChange = onToggleTax)
                Text("PPN ($taxRate%)", style = MaterialTheme.typography.bodyMedium)
            }
            Text(FormatUtils.formatRupiah(taxAmount), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL TAGIHAN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = FormatUtils.formatRupiah(grandTotal),
                style = MaterialTheme.typography.headlineMedium,
                color = ElectricBlue,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onPayClick,
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("checkout_pay_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealSuccess)
        ) {
            Icon(Icons.Default.Payments, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PROSES PEMBAYARAN (${FormatUtils.formatRupiah(grandTotal)})", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BarcodeScannerDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onScanCode: (String) -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scanner Barcode & SKU")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Masukkan kode barcode/SKU dari scanner Bluetooth eksternal atau pilih barcode produk sampel di bawah:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Kode Barcode / SKU") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Quick-Scan Produk Katalog:", style = MaterialTheme.typography.labelLarge)
                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(products.take(8)) { prod ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScanCode(prod.barcode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, style = MaterialTheme.typography.titleSmall)
                                    Text("${prod.barcode} • ${prod.sku}", style = MaterialTheme.typography.labelMedium)
                                }
                                Text(FormatUtils.formatRupiah(prod.sellPrice), fontWeight = FontWeight.Bold, color = ElectricBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (manualBarcode.isNotBlank()) onScanCode(manualBarcode) },
                enabled = manualBarcode.isNotBlank()
            ) {
                Text("Tambah ke Keranjang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
private fun HeldOrdersDialog(
    heldOrders: List<HeldOrderEntity>,
    onDismiss: () -> Unit,
    onResume: (HeldOrderEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daftar Pesanan Tertunda (Hold)") },
        text = {
            if (heldOrders.isEmpty()) {
                Text("Belum ada pesanan yang di-Hold.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(heldOrders, key = { it.id }) { held ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(held.customerName, style = MaterialTheme.typography.titleSmall)
                                    Text(held.tableOrNote, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        FormatUtils.formatRupiah(held.totalAmount),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ElectricBlue
                                    )
                                }
                                Button(onClick = { onResume(held) }) {
                                    Text("Lanjutkan")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
private fun EditCartItemDialog(
    cartItem: CartItem,
    canEditPrice: Boolean,
    globalDiscount: Long,
    onDismiss: () -> Unit,
    onSave: (Long?, Long, Long) -> Unit
) {
    var priceText by remember { mutableStateOf((cartItem.customUnitPrice ?: cartItem.product.sellPrice).toString()) }
    var itemDiscountText by remember { mutableStateOf(cartItem.itemDiscount.toString()) }
    var globalDiscountText by remember { mutableStateOf(globalDiscount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Harga & Diskon: ${cartItem.product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Satuan (Rp)") },
                    enabled = canEditPrice,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!canEditPrice) {
                    Text(
                        "Role kasir saat ini tidak memiliki izin mengubah harga satuan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberWarning
                    )
                }
                OutlinedTextField(
                    value = itemDiscountText,
                    onValueChange = { itemDiscountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Potongan Diskon per Item (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = globalDiscountText,
                    onValueChange = { globalDiscountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Diskon Seluruh Transaksi / Voucher (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customP = priceText.toLongOrNull()
                    val itemD = itemDiscountText.toLongOrNull() ?: 0L
                    val globD = globalDiscountText.toLongOrNull() ?: 0L
                    onSave(customP, itemD, globD)
                }
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentCheckoutDialog(
    grandTotal: Long,
    subtotal: Long,
    discountAmount: Long,
    taxAmount: Long,
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onConfirmPayment: (String, Long, String, List<Pair<String, Long>>) -> Unit
) {
    val paymentMethods = listOf("TUNAI", "QRIS", "TRANSFER", "DEBIT", "KREDIT", "E-WALLET", "SPLIT", "PIUTANG")
    var selectedMethod by remember { mutableStateOf("TUNAI") }
    var paidInput by remember(grandTotal) { mutableStateOf(grandTotal.toString()) }
    var referenceInput by remember { mutableStateOf("") }
    var splitMethod1 by remember { mutableStateOf("TUNAI") }
    var splitAmount1 by remember(grandTotal) { mutableStateOf((grandTotal / 2).toString()) }
    var splitMethod2 by remember { mutableStateOf("QRIS") }
    var qrisVerified by remember { mutableStateOf(false) }

    val paidAmount = when (selectedMethod) {
        "TUNAI" -> paidInput.toLongOrNull() ?: 0L
        "PIUTANG" -> paidInput.toLongOrNull() ?: 0L
        else -> grandTotal
    }
    val changeAmount = (paidAmount - grandTotal).coerceAtLeast(0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Pembayaran Kasir", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Total Tagihan: ${FormatUtils.formatRupiah(grandTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih Metode Pembayaran:", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(paymentMethods) { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = {
                                selectedMethod = method
                                if (method == "PIUTANG") paidInput = "0" else paidInput = grandTotal.toString()
                            },
                            label = { Text(method, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                when (selectedMethod) {
                    "TUNAI" -> {
                        OutlinedTextField(
                            value = paidInput,
                            onValueChange = { paidInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Uang Diterima (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickAmounts = listOf(grandTotal, 50_000L, 100_000L, 200_000L).distinct()
                            quickAmounts.forEach { amt ->
                                OutlinedButton(
                                    onClick = { paidInput = amt.toString() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (amt == grandTotal) "Uang Pas" else FormatUtils.formatCompactRupiah(amt),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Surface(
                            color = TealSuccess.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Uang Kembalian:", fontWeight = FontWeight.SemiBold)
                                Text(
                                    FormatUtils.formatRupiah(changeAmount),
                                    fontWeight = FontWeight.Bold,
                                    color = TealSuccess
                                )
                            }
                        }
                    }

                    "QRIS" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("QRIS DINAMIS — STANDAR NASIONAL", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    "NMID: ID1026099881122 • Nominal: ${FormatUtils.formatRupiah(grandTotal)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(2.dp, NavyPrimary, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.QrCode2,
                                        contentDescription = "QRIS Code",
                                        modifier = Modifier.size(94.dp),
                                        tint = NavyPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = qrisVerified, onCheckedChange = { qrisVerified = it })
                                    Text(
                                        "Konfirmasi callback pembayaran QRIS telah diterima",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    "SPLIT" -> {
                        Text("Split Payment (2 Metode Pembayaran):", style = MaterialTheme.typography.titleSmall)
                        val amt1 = splitAmount1.toLongOrNull() ?: 0L
                        val amt2 = (grandTotal - amt1).coerceAtLeast(0L)
                        OutlinedTextField(
                            value = splitAmount1,
                            onValueChange = { splitAmount1 = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Nominal Pembayaran 1 ($splitMethod1)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("TUNAI", "QRIS", "DEBIT").forEach { m ->
                                FilterChip(
                                    selected = splitMethod1 == m,
                                    onClick = { splitMethod1 = m },
                                    label = { Text("1: $m") }
                                )
                            }
                        }
                        Text(
                            "Sisa Pembayaran 2 ($splitMethod2): ${FormatUtils.formatRupiah(amt2)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = ElectricBlue
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("QRIS", "TRANSFER", "DEBIT", "E-WALLET").forEach { m ->
                                FilterChip(
                                    selected = splitMethod2 == m,
                                    onClick = { splitMethod2 = m },
                                    label = { Text("2: $m") }
                                )
                            }
                        }
                    }

                    "PIUTANG" -> {
                        Text(
                            "Pelanggan: ${customer?.name ?: "Pelanggan Umum"}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = paidInput,
                            onValueChange = { paidInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("DP / Pembayaran Awal (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        val sisaPiutang = (grandTotal - paidAmount).coerceAtLeast(0L)
                        Text(
                            "Dicatat ke Saldo Piutang: ${FormatUtils.formatRupiah(sisaPiutang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberWarning
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            value = referenceInput,
                            onValueChange = { referenceInput = it },
                            label = { Text("Nomor Referensi EDC / Approval / Rekening") },
                            placeholder = { Text("Contoh: APPR-882910 / BCA") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canComplete = when (selectedMethod) {
                "TUNAI" -> paidAmount >= grandTotal
                "QRIS" -> qrisVerified
                else -> true
            }
            Button(
                onClick = {
                    val splits = if (selectedMethod == "SPLIT") {
                        val a1 = (splitAmount1.toLongOrNull() ?: 0L).coerceAtMost(grandTotal)
                        val a2 = (grandTotal - a1).coerceAtLeast(0L)
                        listOf(splitMethod1 to a1, splitMethod2 to a2)
                    } else emptyList()

                    onConfirmPayment(
                        selectedMethod,
                        if (selectedMethod == "SPLIT") grandTotal else paidAmount,
                        referenceInput.ifBlank { "$selectedMethod-VERIFIED" },
                        splits
                    )
                },
                enabled = canComplete,
                colors = ButtonDefaults.buttonColors(containerColor = TealSuccess),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Selesaikan & Cetak Struk")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
