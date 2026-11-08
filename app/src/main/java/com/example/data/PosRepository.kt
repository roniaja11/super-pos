package com.example.data

import com.example.core.database.*
import com.example.core.util.FormatUtils
import kotlinx.coroutines.flow.Flow

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val customUnitPrice: Long? = null,
    val itemDiscount: Long = 0L,
    val note: String = ""
) {
    val effectiveUnitPrice: Long
        get() = customUnitPrice ?: if (quantity >= product.minWholesaleQty && product.wholesalePrice > 0) {
            product.wholesalePrice
        } else {
            product.sellPrice
        }

    val isWholesaleApplied: Boolean
        get() = customUnitPrice == null && quantity >= product.minWholesaleQty && product.wholesalePrice > 0

    val lineSubtotal: Long
        get() = ((effectiveUnitPrice - itemDiscount).coerceAtLeast(0L)) * quantity

    val lineCost: Long
        get() = product.buyPrice * quantity
}

class PosRepository(private val dao: PosDao) {
    val branches: Flow<List<BranchEntity>> = dao.getAllBranches()
    val categories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val products: Flow<List<ProductEntity>> = dao.getAllProducts()
    val customers: Flow<List<CustomerEntity>> = dao.getAllCustomers()
    val suppliers: Flow<List<SupplierEntity>> = dao.getAllSuppliers()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allTransactionItems: Flow<List<TransactionItemEntity>> = dao.getAllTransactionItems()
    val stockMovements: Flow<List<StockMovementEntity>> = dao.getAllStockMovements()
    val shifts: Flow<List<CashierShiftEntity>> = dao.getAllShifts()
    val activeShift: Flow<CashierShiftEntity?> = dao.getActiveShift()
    val cashMovements: Flow<List<CashMovementEntity>> = dao.getAllCashMovements()
    val expenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val purchaseOrders: Flow<List<PurchaseOrderEntity>> = dao.getAllPurchaseOrders()
    val employees: Flow<List<EmployeeEntity>> = dao.getAllEmployees()
    val heldOrders: Flow<List<HeldOrderEntity>> = dao.getAllHeldOrders()

    suspend fun initializeSampleData() {
        InitialDataSeeder.seedIfNeeded(dao)
    }

    suspend fun processCheckout(
        branchId: String,
        cashierName: String,
        customer: CustomerEntity?,
        orderType: String,
        tableNumber: String,
        cartItems: List<CartItem>,
        globalDiscount: Long,
        taxAmount: Long,
        serviceFee: Long,
        paymentMethod: String,
        paidAmount: Long,
        paymentReference: String,
        notes: String,
        isOnline: Boolean,
        splits: List<Pair<String, Long>> = emptyList()
    ): TransactionEntity {
        val subtotal = cartItems.sumOf { it.lineSubtotal }
        val totalCost = cartItems.sumOf { it.lineCost }
        val grandTotal = (subtotal - globalDiscount + taxAmount + serviceFee).coerceAtLeast(0L)
        val changeAmount = (paidAmount - grandTotal).coerceAtLeast(0L)
        val invoiceNumber = FormatUtils.generateInvoiceNumber("INV")
        val currentShift = dao.getActiveShiftOnce()

        val paymentStatus = when {
            paymentMethod == "PIUTANG" || paidAmount < grandTotal -> "PIUTANG"
            else -> "LUNAS"
        }

        val trx = TransactionEntity(
            invoiceNumber = invoiceNumber,
            branchId = branchId,
            shiftId = currentShift?.id ?: "shift-default",
            cashierName = cashierName,
            customerId = customer?.id,
            customerName = customer?.name ?: "Pelanggan Umum",
            orderType = orderType,
            tableNumber = tableNumber,
            subtotal = subtotal,
            discountAmount = globalDiscount,
            taxAmount = taxAmount,
            serviceFee = serviceFee,
            grandTotal = grandTotal,
            totalCost = totalCost,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            paymentReference = paymentReference,
            notes = notes,
            syncStatus = if (isOnline) "SYNCED" else "PENDING_SYNC"
        )
        dao.insertTransaction(trx)

        val itemEntities = cartItems.map { item ->
            TransactionItemEntity(
                transactionId = trx.id,
                productId = item.product.id,
                productName = item.product.name,
                sku = item.product.sku,
                unit = item.product.unit,
                quantity = item.quantity,
                buyPrice = item.product.buyPrice,
                unitPrice = item.effectiveUnitPrice,
                discountPerItem = item.itemDiscount,
                subtotal = item.lineSubtotal
            )
        }
        dao.insertTransactionItems(itemEntities)

        if (splits.isNotEmpty()) {
            dao.insertPaymentSplits(
                splits.map { (method, amt) ->
                    PaymentSplitEntity(
                        transactionId = trx.id,
                        method = method,
                        amount = amt
                    )
                }
            )
        }

        // Deduct stock and record audit movement for physical products
        cartItems.forEach { cartItem ->
            val currentProd = dao.getProductById(cartItem.product.id) ?: cartItem.product
            if (!currentProd.isService) {
                val newStock = (currentProd.stock - cartItem.quantity).coerceAtLeast(0)
                dao.updateProduct(currentProd.copy(stock = newStock))
                dao.insertStockMovement(
                    StockMovementEntity(
                        productId = currentProd.id,
                        productName = currentProd.name,
                        type = "SALE",
                        qtyChange = -cartItem.quantity,
                        previousStock = currentProd.stock,
                        currentStock = newStock,
                        referenceNumber = invoiceNumber,
                        notes = "Penjualan Kasir ($orderType)",
                        performedBy = cashierName
                    )
                )
            }
        }

        // Update Active Shift Cash/Non-Cash totals
        if (currentShift != null) {
            val cashPortion = if (paymentMethod == "TUNAI") {
                grandTotal
            } else if (paymentMethod == "SPLIT") {
                splits.filter { it.first == "TUNAI" }.sumOf { it.second }.coerceAtMost(grandTotal)
            } else {
                0L
            }
            val nonCashPortion = (grandTotal - cashPortion).coerceAtLeast(0L)
            val newCashSales = currentShift.totalCashSales + cashPortion
            val newNonCashSales = currentShift.totalNonCashSales + nonCashPortion
            val expectedCash = currentShift.initialCash + newCashSales + currentShift.totalCashIn - currentShift.totalCashOut
            dao.updateShift(
                currentShift.copy(
                    totalCashSales = newCashSales,
                    totalNonCashSales = newNonCashSales,
                    expectedEndingCash = expectedCash
                )
            )
        }

        // Update Customer spend, loyalty points (1 point per Rp 10.000), and receivables
        if (customer != null && customer.id != "cust-umum") {
            val earnedPoints = (grandTotal / 10_000L).toInt()
            val addedDebt = if (paymentStatus == "PIUTANG") (grandTotal - paidAmount).coerceAtLeast(0L) else 0L
            dao.updateCustomer(
                customer.copy(
                    totalSpend = customer.totalSpend + grandTotal,
                    loyaltyPoints = customer.loyaltyPoints + earnedPoints,
                    receivableBalance = customer.receivableBalance + addedDebt
                )
            )
        }

        return trx
    }

    suspend fun refundTransaction(transaction: TransactionEntity, performedBy: String) {
        if (transaction.paymentStatus == "REFUNDED") return
        val items = dao.getItemsForTransaction(transaction.id)
        items.forEach { item ->
            val prod = dao.getProductById(item.productId)
            if (prod != null && !prod.isService) {
                val restoredStock = prod.stock + item.quantity
                dao.updateProduct(prod.copy(stock = restoredStock))
                dao.insertStockMovement(
                    StockMovementEntity(
                        productId = prod.id,
                        productName = prod.name,
                        type = "RETURN",
                        qtyChange = item.quantity,
                        previousStock = prod.stock,
                        currentStock = restoredStock,
                        referenceNumber = transaction.invoiceNumber,
                        notes = "Retur / Refund Transaksi",
                        performedBy = performedBy
                    )
                )
            }
        }
        dao.updateTransaction(transaction.copy(paymentStatus = "REFUNDED"))
    }

    suspend fun getTransactionItems(transactionId: String): List<TransactionItemEntity> {
        return dao.getItemsForTransaction(transactionId)
    }

    suspend fun getTransactionSplits(transactionId: String): List<PaymentSplitEntity> {
        return dao.getSplitsForTransaction(transactionId)
    }

    suspend fun saveProduct(product: ProductEntity, isNew: Boolean, performedBy: String) {
        dao.insertProduct(product)
        if (isNew && !product.isService && product.stock > 0) {
            dao.insertStockMovement(
                StockMovementEntity(
                    productId = product.id,
                    productName = product.name,
                    type = "ADJUSTMENT_IN",
                    qtyChange = product.stock,
                    previousStock = 0,
                    currentStock = product.stock,
                    referenceNumber = FormatUtils.generateInvoiceNumber("INIT"),
                    notes = "Stok Awal Produk Baru",
                    performedBy = performedBy
                )
            )
        }
    }

    suspend fun adjustStock(
        product: ProductEntity,
        newStock: Int,
        movementType: String,
        notes: String,
        performedBy: String
    ) {
        val diff = newStock - product.stock
        dao.updateProduct(product.copy(stock = newStock.coerceAtLeast(0)))
        dao.insertStockMovement(
            StockMovementEntity(
                productId = product.id,
                productName = product.name,
                type = movementType,
                qtyChange = diff,
                previousStock = product.stock,
                currentStock = newStock.coerceAtLeast(0),
                referenceNumber = FormatUtils.generateInvoiceNumber("STK"),
                notes = notes,
                performedBy = performedBy
            )
        )
    }

    suspend fun createPurchaseOrderAndReceiveStock(
        supplier: SupplierEntity,
        product: ProductEntity,
        quantity: Int,
        buyPricePerUnit: Long,
        paymentType: String,
        performedBy: String
    ) {
        val total = quantity * buyPricePerUnit
        val poNumber = FormatUtils.generateInvoiceNumber("PO")
        val po = PurchaseOrderEntity(
            poNumber = poNumber,
            supplierId = supplier.id,
            supplierName = supplier.name,
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            buyPricePerUnit = buyPricePerUnit,
            totalAmount = total,
            paymentType = paymentType,
            status = "DITERIMA"
        )
        dao.insertPurchaseOrder(po)

        // Increase product stock & update buyPrice
        val updatedStock = product.stock + quantity
        dao.updateProduct(product.copy(stock = updatedStock, buyPrice = buyPricePerUnit))
        dao.insertStockMovement(
            StockMovementEntity(
                productId = product.id,
                productName = product.name,
                type = "PURCHASE",
                qtyChange = quantity,
                previousStock = product.stock,
                currentStock = updatedStock,
                referenceNumber = poNumber,
                notes = "Pembelian dari ${supplier.name} ($paymentType)",
                performedBy = performedBy
            )
        )

        if (paymentType == "TEMPO") {
            dao.updateSupplier(supplier.copy(debtBalance = supplier.debtBalance + total))
        } else {
            dao.insertExpense(
                ExpenseEntity(
                    category = "Pembelian Stok",
                    title = "PO $poNumber - ${product.name} (${quantity} ${product.unit})",
                    amount = total,
                    paymentMethod = "TUNAI",
                    recordedBy = performedBy,
                    notes = "Supplier: ${supplier.name}"
                )
            )
        }
    }

    suspend fun paySupplierDebt(supplier: SupplierEntity, payAmount: Long, performedBy: String) {
        val actualPay = payAmount.coerceAtMost(supplier.debtBalance)
        if (actualPay <= 0L) return
        dao.updateSupplier(supplier.copy(debtBalance = supplier.debtBalance - actualPay))
        dao.insertExpense(
            ExpenseEntity(
                category = "Pelunasan Utang Supplier",
                title = "Pembayaran Utang ke ${supplier.name}",
                amount = actualPay,
                paymentMethod = "TRANSFER",
                recordedBy = performedBy,
                notes = "Sisa utang: ${FormatUtils.formatRupiah(supplier.debtBalance - actualPay)}"
            )
        )
    }

    suspend fun payCustomerReceivable(customer: CustomerEntity, payAmount: Long) {
        val actualPay = payAmount.coerceAtMost(customer.receivableBalance)
        if (actualPay <= 0L) return
        dao.updateCustomer(customer.copy(receivableBalance = customer.receivableBalance - actualPay))
    }

    suspend fun openNewShift(cashierName: String, branchName: String, initialCash: Long) {
        val current = dao.getActiveShiftOnce()
        if (current != null) return
        dao.insertShift(
            CashierShiftEntity(
                cashierName = cashierName,
                branchName = branchName,
                initialCash = initialCash,
                expectedEndingCash = initialCash,
                status = "OPEN"
            )
        )
    }

    suspend fun recordCashMovement(
        type: String,
        category: String,
        amount: Long,
        description: String,
        createdBy: String
    ) {
        val shift = dao.getActiveShiftOnce() ?: return
        dao.insertCashMovement(
            CashMovementEntity(
                shiftId = shift.id,
                type = type,
                category = category,
                amount = amount,
                description = description,
                createdBy = createdBy
            )
        )
        val newCashIn = if (type == "CASH_IN") shift.totalCashIn + amount else shift.totalCashIn
        val newCashOut = if (type == "CASH_OUT") shift.totalCashOut + amount else shift.totalCashOut
        val expected = shift.initialCash + shift.totalCashSales + newCashIn - newCashOut
        dao.updateShift(
            shift.copy(
                totalCashIn = newCashIn,
                totalCashOut = newCashOut,
                expectedEndingCash = expected
            )
        )
    }

    suspend fun closeActiveShift(actualPhysicalCash: Long, closingNotes: String) {
        val shift = dao.getActiveShiftOnce() ?: return
        val diff = actualPhysicalCash - shift.expectedEndingCash
        dao.updateShift(
            shift.copy(
                closedAt = System.currentTimeMillis(),
                actualPhysicalCash = actualPhysicalCash,
                cashDifference = diff,
                status = "CLOSED",
                closingNotes = closingNotes
            )
        )
    }

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    suspend fun saveCustomer(customer: CustomerEntity) = dao.insertCustomer(customer)
    suspend fun saveSupplier(supplier: SupplierEntity) = dao.insertSupplier(supplier)
    suspend fun saveCategory(category: CategoryEntity) = dao.insertCategory(category)
    suspend fun saveEmployee(employee: EmployeeEntity) = dao.insertEmployee(employee)
    suspend fun saveBranch(branch: BranchEntity) = dao.insertBranch(branch)
    suspend fun holdOrder(heldOrder: HeldOrderEntity) = dao.insertHeldOrder(heldOrder)
    suspend fun deleteHeldOrder(id: String) = dao.deleteHeldOrder(id)
    suspend fun syncPendingTransactions() = dao.markAllTransactionsSynced()
    suspend fun archiveProduct(productId: String) = dao.archiveProduct(productId)
}
