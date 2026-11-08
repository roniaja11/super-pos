package com.example.core.database

object InitialDataSeeder {
    suspend fun seedIfNeeded(dao: PosDao) {
        if (dao.getProductCount() > 0) return

        // 1. Branches
        val mainBranch = BranchEntity(
            id = "branch-pusat",
            name = "SUPER POS — Cabang Pusat Sudirman",
            code = "JKT-01",
            address = "Jl. Jend. Sudirman Kav. 52, Jakarta Selatan",
            phone = "021-55889900",
            businessType = "UNIVERSAL",
            taxPercentage = 11,
            serviceChargePercentage = 0,
            receiptFooter = "Barang yang sudah dibeli dapat ditukar maks 3 hari dengan struk asli.",
            isMainBranch = true
        )
        val secondBranch = BranchEntity(
            id = "branch-bandung",
            name = "SUPER POS — Cabang Dago Bandung",
            code = "BDG-02",
            address = "Jl. Ir. H. Juanda No. 112, Bandung",
            phone = "022-4201188",
            businessType = "FNB",
            taxPercentage = 10,
            serviceChargePercentage = 5,
            receiptFooter = "Terima kasih atas kunjungan Anda! Follow IG @superpos.id",
            isMainBranch = false
        )
        dao.insertBranch(mainBranch)
        dao.insertBranch(secondBranch)

        // 2. Categories across Universal Multi-Sector (Retail, F&B, Apotek, Elektronik, Jasa)
        val categories = listOf(
            CategoryEntity("cat-minuman", "Kopi & Minuman", 0xFF2563EB, "FNB"),
            CategoryEntity("cat-makanan", "Makanan & Bakery", 0xFFD97706, "FNB"),
            CategoryEntity("cat-sembako", "Sembako & Minimarket", 0xFF059669, "RETAIL"),
            CategoryEntity("cat-apotek", "Obat & Kesehatan", 0xFFDC2626, "PHARMACY"),
            CategoryEntity("cat-elektronik", "Aksesoris & Elektronik", 0xFF7C3AED, "RETAIL"),
            CategoryEntity("cat-jasa", "Jasa & Layanan Servis", 0xFF0891B2, "SERVICE")
        )
        dao.insertCategories(categories)

        // 3. Rich Catalog of Products with SKU, Barcode, HPP, Wholesale, Batch/Expiry
        val products = listOf(
            ProductEntity(
                id = "prod-1",
                name = "Es Kopi Susu Gula Aren",
                sku = "FNB-KOP-001",
                barcode = "8991002101011",
                categoryId = "cat-minuman",
                categoryName = "Kopi & Minuman",
                buyPrice = 11000L,
                sellPrice = 22000L,
                wholesalePrice = 19500L,
                minWholesaleQty = 10,
                stock = 85,
                minStock = 15,
                unit = "Cup",
                isFavorite = true
            ),
            ProductEntity(
                id = "prod-2",
                name = "Matcha Latte Premium Iced",
                sku = "FNB-MIN-002",
                barcode = "8991002101028",
                categoryId = "cat-minuman",
                categoryName = "Kopi & Minuman",
                buyPrice = 13500L,
                sellPrice = 26000L,
                wholesalePrice = 23000L,
                minWholesaleQty = 10,
                stock = 54,
                minStock = 10,
                unit = "Cup",
                isFavorite = true
            ),
            ProductEntity(
                id = "prod-3",
                name = "Croissant Butter Almond",
                sku = "FNB-ROT-003",
                barcode = "8991002101035",
                categoryId = "cat-makanan",
                categoryName = "Makanan & Bakery",
                buyPrice = 14000L,
                sellPrice = 28000L,
                wholesalePrice = 25000L,
                minWholesaleQty = 6,
                stock = 28,
                minStock = 8,
                unit = "Pcs",
                isFavorite = true
            ),
            ProductEntity(
                id = "prod-4",
                name = "Nasi Goreng Spesial Wagyu",
                sku = "FNB-MAK-004",
                barcode = "8991002101042",
                categoryId = "cat-makanan",
                categoryName = "Makanan & Bakery",
                buyPrice = 24000L,
                sellPrice = 45000L,
                wholesalePrice = 41000L,
                minWholesaleQty = 5,
                stock = 42,
                minStock = 10,
                unit = "Porsi",
                isFavorite = true
            ),
            ProductEntity(
                id = "prod-5",
                name = "Beras Premium Setra Ramos 5Kg",
                sku = "RTL-SMB-005",
                barcode = "8992761134012",
                categoryId = "cat-sembako",
                categoryName = "Sembako & Minimarket",
                buyPrice = 64000L,
                sellPrice = 74500L,
                wholesalePrice = 71000L,
                minWholesaleQty = 10,
                stock = 36,
                minStock = 10,
                unit = "Sak",
                isFavorite = true
            ),
            ProductEntity(
                id = "prod-6",
                name = "Minyak Goreng Sawit 2 Liter",
                sku = "RTL-SMB-006",
                barcode = "8992761134029",
                categoryId = "cat-sembako",
                categoryName = "Sembako & Minimarket",
                buyPrice = 31000L,
                sellPrice = 36500L,
                wholesalePrice = 34800L,
                minWholesaleQty = 12,
                stock = 4, // Stok menipis untuk demo alert
                minStock = 12,
                unit = "Pouch",
                isFavorite = true
            ),
            ProductEntity(
                id = "prod-7",
                name = "Indomie Goreng Spesial Karton (40 Bks)",
                sku = "RTL-SMB-007",
                barcode = "8992761134036",
                categoryId = "cat-sembako",
                categoryName = "Sembako & Minimarket",
                buyPrice = 108000L,
                sellPrice = 122000L,
                wholesalePrice = 117000L,
                minWholesaleQty = 5,
                stock = 22,
                minStock = 5,
                unit = "Karton"
            ),
            ProductEntity(
                id = "prod-8",
                name = "Vitamin C 1000mg Strip (10 Tablet)",
                sku = "APT-VIT-008",
                barcode = "8993112090015",
                categoryId = "cat-apotek",
                categoryName = "Obat & Kesehatan",
                buyPrice = 16500L,
                sellPrice = 24000L,
                wholesalePrice = 21500L,
                minWholesaleQty = 10,
                stock = 60,
                minStock = 15,
                unit = "Strip",
                batchNumber = "BATCH-26A",
                expiryDate = "2027-08-30"
            ),
            ProductEntity(
                id = "prod-9",
                name = "Paracetamol 500mg Kaplet",
                sku = "APT-OBT-009",
                barcode = "8993112090022",
                categoryId = "cat-apotek",
                categoryName = "Obat & Kesehatan",
                buyPrice = 5000L,
                sellPrice = 9500L,
                wholesalePrice = 8000L,
                minWholesaleQty = 20,
                stock = 3, // Stok menipis
                minStock = 15,
                unit = "Strip",
                batchNumber = "BATCH-25K",
                expiryDate = "2027-03-15"
            ),
            ProductEntity(
                id = "prod-10",
                name = "Kabel Fast Charging USB-C 65W",
                sku = "ELK-ACC-010",
                barcode = "8997009510019",
                categoryId = "cat-elektronik",
                categoryName = "Aksesoris & Elektronik",
                buyPrice = 28000L,
                sellPrice = 55000L,
                wholesalePrice = 45000L,
                minWholesaleQty = 10,
                stock = 31,
                minStock = 5,
                unit = "Unit"
            ),
            ProductEntity(
                id = "prod-11",
                name = "Kertas Struk Thermal 58x40mm (Pack 10 Roll)",
                sku = "ELK-POS-011",
                barcode = "8997009510026",
                categoryId = "cat-elektronik",
                categoryName = "Aksesoris & Elektronik",
                buyPrice = 35000L,
                sellPrice = 52000L,
                wholesalePrice = 47000L,
                minWholesaleQty = 5,
                stock = 19,
                minStock = 5,
                unit = "Pack"
            ),
            ProductEntity(
                id = "prod-12",
                name = "Jasa Instalasi & Setup Perangkat Kasir",
                sku = "SRV-IT-012",
                barcode = "8999990000123",
                categoryId = "cat-jasa",
                categoryName = "Jasa & Layanan Servis",
                buyPrice = 25000L,
                sellPrice = 150000L,
                wholesalePrice = 125000L,
                minWholesaleQty = 3,
                stock = 999,
                minStock = 0,
                unit = "Layanan",
                isService = true,
                isFavorite = true
            )
        )
        dao.insertProducts(products)

        // 4. Customers
        val customers = listOf(
            CustomerEntity(
                id = "cust-umum",
                name = "Pelanggan Umum",
                phone = "-",
                address = "Transaksi Langsung",
                tier = "Reguler",
                discountPercent = 0,
                loyaltyPoints = 0,
                totalSpend = 0L,
                receivableBalance = 0L
            ),
            CustomerEntity(
                id = "cust-1",
                name = "Budi Santoso (Member VIP)",
                phone = "0812-8899-7711",
                address = "Jl. Senopati No. 18, Jakarta Selatan",
                tier = "VIP",
                discountPercent = 5,
                loyaltyPoints = 420,
                totalSpend = 4850000L,
                receivableBalance = 0L
            ),
            CustomerEntity(
                id = "cust-2",
                name = "Toko Berkah Jaya (Grosir)",
                phone = "0813-2233-4455",
                address = "Pasar Minggu Blok B No. 12",
                tier = "Gold",
                discountPercent = 3,
                loyaltyPoints = 890,
                totalSpend = 12450000L,
                receivableBalance = 350000L
            ),
            CustomerEntity(
                id = "cust-3",
                name = "Siti Rahmawati",
                phone = "0857-1122-9988",
                address = "Kemang Pratama Blok D4",
                tier = "Silver",
                discountPercent = 2,
                loyaltyPoints = 150,
                totalSpend = 1320000L,
                receivableBalance = 0L
            )
        )
        dao.insertCustomers(customers)

        // 5. Suppliers
        val suppliers = listOf(
            SupplierEntity(
                id = "sup-1",
                name = "PT Sumber Pangan Nusantara",
                contactPerson = "Hendra Wijaya",
                phone = "021-88901122",
                address = "Kawasan Industri Pulogadung Blok J4",
                categorySupply = "Sembako & Bahan Baku",
                debtBalance = 1250000L
            ),
            SupplierEntity(
                id = "sup-2",
                name = "CV Kopi Nusantara Roastery",
                contactPerson = "Dimas Aditya",
                phone = "0811-2299-3344",
                address = "Jl. Braga No. 45, Bandung",
                categorySupply = "Biji Kopi, Susu & Sirup",
                debtBalance = 0L
            ),
            SupplierEntity(
                id = "sup-3",
                name = "PT Medika Farma Distribusi",
                contactPerson = "dr. Ratna Sari",
                phone = "021-77884411",
                address = "Jl. Raya Bogor KM 24, Jakarta Timur",
                categorySupply = "Obat & Vitamin",
                debtBalance = 650000L
            )
        )
        dao.insertSuppliers(suppliers)

        // 6. Employees & Roles
        val employees = listOf(
            EmployeeEntity(
                id = "emp-1",
                name = "Roni Wijaya (Owner)",
                role = "Super Admin",
                pinCode = "123456",
                phone = "0811-9000-1111",
                branchName = "Cabang Pusat Sudirman",
                canEditPrice = true,
                canGiveDiscount = true,
                canRefund = true,
                canViewReports = true
            ),
            EmployeeEntity(
                id = "emp-2",
                name = "Andi Pratama",
                role = "Manager",
                pinCode = "223344",
                phone = "0812-3344-5566",
                branchName = "Cabang Pusat Sudirman",
                canEditPrice = true,
                canGiveDiscount = true,
                canRefund = true,
                canViewReports = true
            ),
            EmployeeEntity(
                id = "emp-3",
                name = "Nadia Putri",
                role = "Kasir",
                pinCode = "111111",
                phone = "0856-7788-9900",
                branchName = "Cabang Pusat Sudirman",
                canEditPrice = false,
                canGiveDiscount = true,
                canRefund = false,
                canViewReports = false
            ),
            EmployeeEntity(
                id = "emp-4",
                name = "Doni Saputra",
                role = "Gudang",
                pinCode = "555555",
                phone = "0878-1234-5678",
                branchName = "Cabang Pusat Sudirman",
                canEditPrice = false,
                canGiveDiscount = false,
                canRefund = false,
                canViewReports = false
            )
        )
        dao.insertEmployees(employees)

        // 7. Active Shift
        val now = System.currentTimeMillis()
        val activeShift = CashierShiftEntity(
            id = "shift-active-1",
            cashierName = "Nadia Putri",
            branchName = "Cabang Pusat Sudirman",
            openedAt = now - 4 * 3600 * 1000L,
            initialCash = 500000L,
            totalCashSales = 285000L,
            totalNonCashSales = 442000L,
            totalCashIn = 100000L,
            totalCashOut = 45000L,
            expectedEndingCash = 500000L + 285000L + 100000L - 45000L,
            status = "OPEN"
        )
        dao.insertShift(activeShift)

        dao.insertCashMovement(
            CashMovementEntity(
                id = "cm-1",
                shiftId = "shift-active-1",
                type = "CASH_IN",
                category = "Modal Tambahan Uang Kecil",
                amount = 100000L,
                description = "Tambahan pecahan Rp 2.000 & Rp 5.000",
                createdBy = "Nadia Putri",
                timestamp = now - 3 * 3600 * 1000L
            )
        )
        dao.insertCashMovement(
            CashMovementEntity(
                id = "cm-2",
                shiftId = "shift-active-1",
                type = "CASH_OUT",
                category = "Biaya Operasional Toko",
                amount = 45000L,
                description = "Beli es batu kristal & galon air mineral",
                createdBy = "Nadia Putri",
                timestamp = now - 2 * 3600 * 1000L
            )
        )

        // 8. Sample Historical Transactions so Dashboard & Analytics are rich from start
        val sampleTransactions = listOf(
            TransactionEntity(
                id = "trx-101",
                invoiceNumber = "INV-261108-1041",
                branchId = "branch-pusat",
                shiftId = "shift-active-1",
                cashierName = "Nadia Putri",
                customerId = "cust-1",
                customerName = "Budi Santoso (Member VIP)",
                orderType = "Meja #04",
                tableNumber = "04",
                subtotal = 117000L,
                discountAmount = 5000L,
                taxAmount = 12320L,
                grandTotal = 124320L,
                totalCost = 62500L,
                paidAmount = 124320L,
                changeAmount = 0L,
                paymentMethod = "QRIS",
                paymentStatus = "LUNAS",
                paymentReference = "QRIS-99281726",
                notes = "Less sugar untuk Es Kopi Susu",
                syncStatus = "SYNCED",
                createdAt = now - 110 * 60 * 1000L
            ),
            TransactionEntity(
                id = "trx-102",
                invoiceNumber = "INV-261108-1088",
                branchId = "branch-pusat",
                shiftId = "shift-active-1",
                cashierName = "Nadia Putri",
                customerId = "cust-umum",
                customerName = "Pelanggan Umum",
                orderType = "Langsung / Retail",
                subtotal = 285000L,
                discountAmount = 0L,
                taxAmount = 0L,
                grandTotal = 285000L,
                totalCost = 204000L,
                paidAmount = 300000L,
                changeAmount = 15000L,
                paymentMethod = "TUNAI",
                paymentStatus = "LUNAS",
                paymentReference = "CASH-DRAWER",
                notes = "Belanja sembako bulanan",
                syncStatus = "SYNCED",
                createdAt = now - 65 * 60 * 1000L
            ),
            TransactionEntity(
                id = "trx-103",
                invoiceNumber = "INV-261108-1129",
                branchId = "branch-pusat",
                shiftId = "shift-active-1",
                cashierName = "Roni Wijaya (Owner)",
                customerId = "cust-2",
                customerName = "Toko Berkah Jaya (Grosir)",
                orderType = "Grosir / B2B",
                subtotal = 317680L,
                discountAmount = 0L,
                taxAmount = 0L,
                grandTotal = 317680L,
                totalCost = 185000L,
                paidAmount = 317680L,
                changeAmount = 0L,
                paymentMethod = "DEBIT",
                paymentStatus = "LUNAS",
                paymentReference = "EDC-BCA-4812",
                notes = "Paket instalasi POS + kertas thermal",
                syncStatus = "PENDING_SYNC",
                createdAt = now - 25 * 60 * 1000L
            )
        )
        sampleTransactions.forEach { dao.insertTransaction(it) }

        val sampleItems = listOf(
            TransactionItemEntity(
                transactionId = "trx-101",
                productId = "prod-1",
                productName = "Es Kopi Susu Gula Aren",
                sku = "FNB-KOP-001",
                unit = "Cup",
                quantity = 2,
                buyPrice = 11000L,
                unitPrice = 22000L,
                subtotal = 44000L
            ),
            TransactionItemEntity(
                transactionId = "trx-101",
                productId = "prod-3",
                productName = "Croissant Butter Almond",
                sku = "FNB-ROT-003",
                unit = "Pcs",
                quantity = 1,
                buyPrice = 14000L,
                unitPrice = 28000L,
                subtotal = 28000L
            ),
            TransactionItemEntity(
                transactionId = "trx-101",
                productId = "prod-4",
                productName = "Nasi Goreng Spesial Wagyu",
                sku = "FNB-MAK-004",
                unit = "Porsi",
                quantity = 1,
                buyPrice = 24000L,
                unitPrice = 45000L,
                subtotal = 45000L
            ),
            TransactionItemEntity(
                transactionId = "trx-102",
                productId = "prod-5",
                productName = "Beras Premium Setra Ramos 5Kg",
                sku = "RTL-SMB-005",
                unit = "Sak",
                quantity = 2,
                buyPrice = 64000L,
                unitPrice = 74500L,
                subtotal = 149000L
            ),
            TransactionItemEntity(
                transactionId = "trx-102",
                productId = "prod-6",
                productName = "Minyak Goreng Sawit 2 Liter",
                sku = "RTL-SMB-006",
                unit = "Pouch",
                quantity = 2,
                buyPrice = 31000L,
                unitPrice = 36500L,
                subtotal = 73000L
            ),
            TransactionItemEntity(
                transactionId = "trx-102",
                productId = "prod-8",
                productName = "Vitamin C 1000mg Strip (10 Tablet)",
                sku = "APT-VIT-008",
                unit = "Strip",
                quantity = 2,
                buyPrice = 16500L,
                unitPrice = 31500L,
                subtotal = 63000L
            ),
            TransactionItemEntity(
                transactionId = "trx-103",
                productId = "prod-12",
                productName = "Jasa Instalasi & Setup Perangkat Kasir",
                sku = "SRV-IT-012",
                unit = "Layanan",
                quantity = 1,
                buyPrice = 25000L,
                unitPrice = 150000L,
                subtotal = 150000L
            ),
            TransactionItemEntity(
                transactionId = "trx-103",
                productId = "prod-10",
                productName = "Kabel Fast Charging USB-C 65W",
                sku = "ELK-ACC-010",
                unit = "Unit",
                quantity = 3,
                buyPrice = 28000L,
                unitPrice = 55893L,
                subtotal = 167680L
            )
        )
        dao.insertTransactionItems(sampleItems)

        // 9. Stock Movements History
        dao.insertStockMovements(
            listOf(
                StockMovementEntity(
                    productId = "prod-5",
                    productName = "Beras Premium Setra Ramos 5Kg",
                    type = "PURCHASE",
                    qtyChange = 40,
                    previousStock = 0,
                    currentStock = 40,
                    referenceNumber = "PO-261101-001",
                    notes = "Stok awal dari PT Sumber Pangan Nusantara",
                    performedBy = "Doni Saputra",
                    timestamp = now - 24 * 3600 * 1000L
                ),
                StockMovementEntity(
                    productId = "prod-5",
                    productName = "Beras Premium Setra Ramos 5Kg",
                    type = "SALE",
                    qtyChange = -2,
                    previousStock = 38,
                    currentStock = 36,
                    referenceNumber = "INV-261108-1088",
                    notes = "Penjualan Kasir",
                    performedBy = "Nadia Putri",
                    timestamp = now - 65 * 60 * 1000L
                ),
                StockMovementEntity(
                    productId = "prod-1",
                    productName = "Es Kopi Susu Gula Aren",
                    type = "SALE",
                    qtyChange = -2,
                    previousStock = 87,
                    currentStock = 85,
                    referenceNumber = "INV-261108-1041",
                    notes = "Penjualan Meja #04",
                    performedBy = "Nadia Putri",
                    timestamp = now - 110 * 60 * 1000L
                )
            )
        )

        // 10. Expenses
        dao.insertExpenses(
            listOf(
                ExpenseEntity(
                    category = "Listrik & Internet",
                    title = "Tagihan Token Listrik & WiFi Fiber Bisnis",
                    amount = 850000L,
                    paymentMethod = "TRANSFER",
                    recordedBy = "Roni Wijaya (Owner)",
                    notes = "Pembayaran rutin bulanan",
                    timestamp = now - 18 * 3600 * 1000L
                ),
                ExpenseEntity(
                    category = "Operasional Toko",
                    title = "Kemasan Paper Cup, Kantong Ramah Lingkungan & Tisu",
                    amount = 320000L,
                    paymentMethod = "TUNAI",
                    recordedBy = "Andi Pratama",
                    notes = "Stok perlengkapan kasir",
                    timestamp = now - 8 * 3600 * 1000L
                )
            )
        )

        // 11. Purchase Orders
        dao.insertPurchaseOrders(
            listOf(
                PurchaseOrderEntity(
                    poNumber = "PO-261107-019",
                    supplierId = "sup-1",
                    supplierName = "PT Sumber Pangan Nusantara",
                    productId = "prod-5",
                    productName = "Beras Premium Setra Ramos 5Kg",
                    quantity = 25,
                    buyPricePerUnit = 64000L,
                    totalAmount = 1600000L,
                    paymentType = "LUNAS",
                    status = "DITERIMA",
                    timestamp = now - 20 * 3600 * 1000L
                ),
                PurchaseOrderEntity(
                    poNumber = "PO-261108-024",
                    supplierId = "sup-2",
                    supplierName = "CV Kopi Nusantara Roastery",
                    productId = "prod-1",
                    productName = "Es Kopi Susu Gula Aren (Bahan Baku)",
                    quantity = 50,
                    buyPricePerUnit = 11000L,
                    totalAmount = 550000L,
                    paymentType = "TEMPO",
                    status = "DITERIMA",
                    timestamp = now - 5 * 3600 * 1000L
                )
            )
        )
    }
}
