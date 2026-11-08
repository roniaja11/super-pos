package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.*
import com.example.data.CartItem
import com.example.data.PosRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReceiptModalData(
    val transaction: TransactionEntity,
    val items: List<TransactionItemEntity>,
    val splits: List<PaymentSplitEntity>
)

class SuperPosViewModel(private val repository: PosRepository) : ViewModel() {

    // Database Flows
    val branches: StateFlow<List<BranchEntity>> = repository.branches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.suppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionItems: StateFlow<List<TransactionItemEntity>> = repository.allTransactionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements: StateFlow<List<StockMovementEntity>> = repository.stockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<CashierShiftEntity>> = repository.shifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeShift: StateFlow<CashierShiftEntity?> = repository.activeShift
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cashMovements: StateFlow<List<CashMovementEntity>> = repository.cashMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseOrders: StateFlow<List<PurchaseOrderEntity>> = repository.purchaseOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heldOrders: StateFlow<List<HeldOrderEntity>> = repository.heldOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global App & Session State
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(true)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _selectedBranchId = MutableStateFlow("branch-pusat")
    val selectedBranchId: StateFlow<String> = _selectedBranchId.asStateFlow()

    private val _activeEmployee = MutableStateFlow<EmployeeEntity?>(null)
    val activeEmployee: StateFlow<EmployeeEntity?> = _activeEmployee.asStateFlow()

    private val _businessMode = MutableStateFlow("UNIVERSAL") // UNIVERSAL, RETAIL, FNB, SERVICE
    val businessMode: StateFlow<String> = _businessMode.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // POS Cashier State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("ALL")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    private val _orderType = MutableStateFlow("Langsung / Retail")
    val orderType: StateFlow<String> = _orderType.asStateFlow()

    private val _tableNumber = MutableStateFlow("")
    val tableNumber: StateFlow<String> = _tableNumber.asStateFlow()

    private val _globalDiscount = MutableStateFlow(0L)
    val globalDiscount: StateFlow<Long> = _globalDiscount.asStateFlow()

    private val _isTaxEnabled = MutableStateFlow(true)
    val isTaxEnabled: StateFlow<Boolean> = _isTaxEnabled.asStateFlow()

    private val _orderNotes = MutableStateFlow("")
    val orderNotes: StateFlow<String> = _orderNotes.asStateFlow()

    private val _receiptModal = MutableStateFlow<ReceiptModalData?>(null)
    val receiptModal: StateFlow<ReceiptModalData?> = _receiptModal.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSampleData()
        }
        viewModelScope.launch {
            employees.collect { list ->
                if (_activeEmployee.value == null && list.isNotEmpty()) {
                    _activeEmployee.value = list.first()
                }
            }
        }
    }

    fun showMessage(msg: String) {
        _statusBannerMessage.value = msg
    }

    fun clearMessage() {
        _statusBannerMessage.value = null
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleOnlineMode() {
        _isOnlineMode.value = !_isOnlineMode.value
        val modeText = if (_isOnlineMode.value) {
            "Mode Online Aktif: Sinkronisasi cloud otomatis menyala"
        } else {
            "Mode Offline-First Aktif: Transaksi disimpan di Room Database lokal"
        }
        showMessage(modeText)
    }

    fun syncNow() {
        viewModelScope.launch {
            repository.syncPendingTransactions()
            _isOnlineMode.value = true
            showMessage("Seluruh transaksi offline berhasil disinkronkan ke server cloud!")
        }
    }

    fun selectBranch(branch: BranchEntity) {
        _selectedBranchId.value = branch.id
        _businessMode.value = branch.businessType
        showMessage("Beralih ke ${branch.name} (Mode: ${branch.businessType})")
    }

    fun setBusinessMode(mode: String) {
        _businessMode.value = mode
        showMessage("Mode Operasional Kasir diubah ke: $mode")
    }

    fun switchActiveEmployee(employee: EmployeeEntity) {
        _activeEmployee.value = employee
        showMessage("Login aktif: ${employee.name} (${employee.role})")
    }

    // --- POS Cashier Actions ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _selectedCustomer.value = customer
    }

    fun setOrderType(type: String, table: String = "") {
        _orderType.value = type
        _tableNumber.value = table
    }

    fun setGlobalDiscount(discount: Long) {
        _globalDiscount.value = discount.coerceAtLeast(0L)
    }

    fun toggleTax(enabled: Boolean) {
        _isTaxEnabled.value = enabled
    }

    fun setOrderNotes(notes: String) {
        _orderNotes.value = notes
    }

    fun addToCart(product: ProductEntity) {
        if (!product.isService && product.stock <= 0) {
            showMessage("Stok ${product.name} habis!")
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = currentList[existingIndex]
            if (!product.isService && item.quantity + 1 > product.stock) {
                showMessage("Melebihi sisa stok tersedia (${product.stock} ${product.unit})")
                return
            }
            currentList[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            currentList.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = currentList
    }

    fun scanBarcodeOrSku(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return
        val matched = products.value.firstOrNull {
            it.barcode.equals(trimmed, ignoreCase = true) || it.sku.equals(trimmed, ignoreCase = true)
        }
        if (matched != null) {
            addToCart(matched)
            showMessage("Barcode ditemukan: ${matched.name}")
        } else {
            showMessage("Produk dengan Barcode/SKU '$trimmed' tidak ditemukan")
        }
    }

    fun updateCartItemQuantity(productId: String, newQty: Int) {
        val currentList = _cartItems.value.toMutableList()
        val idx = currentList.indexOfFirst { it.product.id == productId }
        if (idx < 0) return
        if (newQty <= 0) {
            currentList.removeAt(idx)
        } else {
            val item = currentList[idx]
            if (!item.product.isService && newQty > item.product.stock) {
                showMessage("Stok maksimal ${item.product.stock} ${item.product.unit}")
                return
            }
            currentList[idx] = item.copy(quantity = newQty)
        }
        _cartItems.value = currentList
    }

    fun updateCartItemPriceAndDiscount(productId: String, customPrice: Long?, itemDiscount: Long) {
        val currentList = _cartItems.value.toMutableList()
        val idx = currentList.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            currentList[idx] = currentList[idx].copy(
                customUnitPrice = customPrice,
                itemDiscount = itemDiscount.coerceAtLeast(0L)
            )
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _globalDiscount.value = 0L
        _orderNotes.value = ""
        _tableNumber.value = ""
    }

    fun holdCurrentOrder() {
        val items = _cartItems.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            val encoded = items.joinToString("||") { "${it.product.id}::${it.quantity}::${it.itemDiscount}" }
            val subtotal = items.sumOf { it.lineSubtotal }
            repository.holdOrder(
                HeldOrderEntity(
                    customerName = _selectedCustomer.value?.name ?: "Pelanggan Umum",
                    tableOrNote = if (_tableNumber.value.isNotBlank()) "Meja #${_tableNumber.value}" else _orderType.value,
                    itemsJson = encoded,
                    totalAmount = subtotal
                )
            )
            clearCart()
            showMessage("Pesanan berhasil di-Hold ke daftar antrean")
        }
    }

    fun resumeHeldOrder(heldOrder: HeldOrderEntity) {
        viewModelScope.launch {
            val currentCatalog = products.value.associateBy { it.id }
            val restored = heldOrder.itemsJson.split("||").mapNotNull { token ->
                val parts = token.split("::")
                if (parts.size >= 2) {
                    val prod = currentCatalog[parts[0]]
                    val qty = parts[1].toIntOrNull() ?: 1
                    val disc = parts.getOrNull(2)?.toLongOrNull() ?: 0L
                    if (prod != null) CartItem(prod, qty, itemDiscount = disc) else null
                } else null
            }
            if (restored.isNotEmpty()) {
                _cartItems.value = restored
                repository.deleteHeldOrder(heldOrder.id)
                showMessage("Pesanan ${heldOrder.customerName} dilanjutkan ke keranjang")
            }
        }
    }

    fun completeCheckout(
        paymentMethod: String,
        paidAmount: Long,
        paymentReference: String,
        splits: List<Pair<String, Long>> = emptyList()
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            val branch = branches.value.firstOrNull { it.id == _selectedBranchId.value } ?: branches.value.firstOrNull()
            val subtotal = items.sumOf { it.lineSubtotal }
            val customerDiscount = _selectedCustomer.value?.let { (subtotal * it.discountPercent) / 100L } ?: 0L
            val totalDiscount = _globalDiscount.value + customerDiscount
            val taxableBase = (subtotal - totalDiscount).coerceAtLeast(0L)
            val taxRate = if (_isTaxEnabled.value) (branch?.taxPercentage ?: 11) else 0
            val taxAmount = (taxableBase * taxRate) / 100L
            val serviceRate = if (_orderType.value.startsWith("Meja")) (branch?.serviceChargePercentage ?: 0) else 0
            val serviceFee = (taxableBase * serviceRate) / 100L

            val cashierName = _activeEmployee.value?.name ?: "Kasir Utama"
            val trx = repository.processCheckout(
                branchId = branch?.id ?: "branch-pusat",
                cashierName = cashierName,
                customer = _selectedCustomer.value,
                orderType = if (_tableNumber.value.isNotBlank()) "Meja #${_tableNumber.value}" else _orderType.value,
                tableNumber = _tableNumber.value,
                cartItems = items,
                globalDiscount = totalDiscount,
                taxAmount = taxAmount,
                serviceFee = serviceFee,
                paymentMethod = paymentMethod,
                paidAmount = paidAmount,
                paymentReference = paymentReference,
                notes = _orderNotes.value,
                isOnline = _isOnlineMode.value,
                splits = splits
            )
            val savedItems = repository.getTransactionItems(trx.id)
            val savedSplits = repository.getTransactionSplits(trx.id)
            _receiptModal.value = ReceiptModalData(trx, savedItems, savedSplits)
            clearCart()
            showMessage("Transaksi ${trx.invoiceNumber} berhasil disimpan!")
        }
    }

    fun openReceiptForTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val items = repository.getTransactionItems(transaction.id)
            val splits = repository.getTransactionSplits(transaction.id)
            _receiptModal.value = ReceiptModalData(transaction, items, splits)
        }
    }

    fun dismissReceiptModal() {
        _receiptModal.value = null
    }

    fun refundTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Owner"
            repository.refundTransaction(transaction, actor)
            _receiptModal.value = null
            showMessage("Transaksi ${transaction.invoiceNumber} telah di-Refund dan stok dikembalikan.")
        }
    }

    // --- Inventory & Product Actions ---
    fun saveProduct(product: ProductEntity, isNew: Boolean) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Admin"
            repository.saveProduct(product, isNew, actor)
            showMessage("Produk '${product.name}' berhasil disimpan")
        }
    }

    fun adjustStock(product: ProductEntity, newStock: Int, movementType: String, notes: String) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Admin Gudang"
            repository.adjustStock(product, newStock, movementType, notes, actor)
            showMessage("Stok '${product.name}' diperbarui menjadi $newStock ${product.unit}")
        }
    }

    fun archiveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.archiveProduct(product.id)
            showMessage("Produk '${product.name}' diarsipkan")
        }
    }

    fun addCategory(name: String, colorHex: Long, sector: String) {
        viewModelScope.launch {
            repository.saveCategory(CategoryEntity(name = name, colorHex = colorHex, businessSector = sector))
            showMessage("Kategori '$name' ditambahkan")
        }
    }

    // --- Purchasing, Suppliers, Customers, Finance, Shift ---
    fun createPurchaseOrder(
        supplier: SupplierEntity,
        product: ProductEntity,
        quantity: Int,
        buyPrice: Long,
        paymentType: String
    ) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Manager"
            repository.createPurchaseOrderAndReceiveStock(supplier, product, quantity, buyPrice, paymentType, actor)
            showMessage("Pembelian ${product.name} (+$quantity ${product.unit}) diterima ke gudang!")
        }
    }

    fun paySupplierDebt(supplier: SupplierEntity, amount: Long) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Owner"
            repository.paySupplierDebt(supplier, amount, actor)
            showMessage("Pembayaran utang ke ${supplier.name} berhasil dicatat")
        }
    }

    fun payCustomerReceivable(customer: CustomerEntity, amount: Long) {
        viewModelScope.launch {
            repository.payCustomerReceivable(customer, amount)
            showMessage("Pelunasan piutang ${customer.name} berhasil dicatat")
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            showMessage("Pelanggan '${customer.name}' berhasil disimpan")
        }
    }

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.saveSupplier(supplier)
            showMessage("Supplier '${supplier.name}' berhasil disimpan")
        }
    }

    fun addExpense(category: String, title: String, amount: Long, method: String, notes: String) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Owner"
            repository.addExpense(
                ExpenseEntity(
                    category = category,
                    title = title,
                    amount = amount,
                    paymentMethod = method,
                    recordedBy = actor,
                    notes = notes
                )
            )
            showMessage("Pengeluaran '$title' berhasil dicatat")
        }
    }

    fun openShift(initialCash: Long) {
        viewModelScope.launch {
            val cashier = _activeEmployee.value?.name ?: "Kasir Utama"
            val branch = branches.value.firstOrNull { it.id == _selectedBranchId.value }?.name ?: "Cabang Pusat"
            repository.openNewShift(cashier, branch, initialCash)
            showMessage("Shift Kasir baru dibuka oleh $cashier")
        }
    }

    fun recordShiftCashMovement(type: String, category: String, amount: Long, description: String) {
        viewModelScope.launch {
            val actor = _activeEmployee.value?.name ?: "Kasir"
            repository.recordCashMovement(type, category, amount, description, actor)
            showMessage("Mutasi kas shift berhasil dicatat")
        }
    }

    fun closeShift(actualPhysicalCash: Long, notes: String) {
        viewModelScope.launch {
            repository.closeActiveShift(actualPhysicalCash, notes)
            showMessage("Shift Kasir ditutup dan diaudit")
        }
    }

    fun saveEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.saveEmployee(employee)
            showMessage("Data karyawan '${employee.name}' disimpan")
        }
    }

    fun saveBranch(branch: BranchEntity) {
        viewModelScope.launch {
            repository.saveBranch(branch)
            showMessage("Profil cabang '${branch.name}' diperbarui")
        }
    }
}

class SuperPosViewModelFactory(private val repository: PosRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SuperPosViewModel::class.java)) {
            return SuperPosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
