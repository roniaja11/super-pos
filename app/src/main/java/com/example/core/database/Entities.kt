package com.example.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val code: String,
    val address: String,
    val phone: String,
    val businessType: String, // "UNIVERSAL", "RETAIL", "FNB", "SERVICE", "PHARMACY"
    val taxPercentage: Int = 11,
    val serviceChargePercentage: Int = 0,
    val receiptFooter: String = "Terima kasih telah berbelanja di toko kami!",
    val isMainBranch: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: Long,
    val businessSector: String = "ALL" // ALL, RETAIL, FNB, SERVICE, PHARMACY
)

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"]), Index(value = ["barcode"]), Index(value = ["categoryId"])]
)
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sku: String,
    val barcode: String,
    val categoryId: String,
    val categoryName: String,
    val buyPrice: Long,        // Harga Modal (HPP) dalam Rupiah
    val sellPrice: Long,       // Harga Jual Eceran
    val wholesalePrice: Long,  // Harga Grosir
    val minWholesaleQty: Int = 12,
    val stock: Int,
    val minStock: Int = 5,
    val unit: String = "Pcs",
    val isService: Boolean = false, // True untuk jasa tanpa pengurangan stok fisik
    val isFavorite: Boolean = false,
    val batchNumber: String = "",
    val expiryDate: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val address: String,
    val tier: String = "Reguler", // Reguler, Silver, Gold, VIP
    val discountPercent: Int = 0,
    val loyaltyPoints: Int = 0,
    val totalSpend: Long = 0L,
    val receivableBalance: Long = 0L // Saldo Piutang Pelanggan
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val contactPerson: String,
    val phone: String,
    val address: String,
    val categorySupply: String,
    val debtBalance: Long = 0L // Saldo Utang ke Supplier
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["invoiceNumber"]), Index(value = ["createdAt"])]
)
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val branchId: String,
    val shiftId: String,
    val cashierName: String,
    val customerId: String?,
    val customerName: String,
    val orderType: String = "Dine-In / Langsung", // Langsung, Bungkus, Meja #1..#10, Delivery
    val tableNumber: String = "",
    val subtotal: Long,
    val discountAmount: Long,
    val taxAmount: Long,
    val serviceFee: Long = 0L,
    val grandTotal: Long,
    val totalCost: Long, // Total HPP untuk kalkulasi laba kotor akurat
    val paidAmount: Long,
    val changeAmount: Long,
    val paymentMethod: String, // TUNAI, QRIS, TRANSFER, DEBIT, KREDIT, E-WALLET, SPLIT, PIUTANG
    val paymentStatus: String = "LUNAS", // LUNAS, PENDING, PIUTANG, REFUNDED
    val paymentReference: String = "",
    val notes: String = "",
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_SYNC
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transaction_items",
    indices = [Index(value = ["transactionId"]), Index(value = ["productId"])]
)
data class TransactionItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val productId: String,
    val productName: String,
    val sku: String,
    val unit: String,
    val quantity: Int,
    val buyPrice: Long,
    val unitPrice: Long,
    val discountPerItem: Long = 0L,
    val subtotal: Long
)

@Entity(tableName = "payment_splits")
data class PaymentSplitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val method: String,
    val amount: Long,
    val referenceNote: String = ""
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val productName: String,
    val type: String, // SALE, PURCHASE, OPNAME, ADJUSTMENT_IN, ADJUSTMENT_OUT, TRANSFER, RETURN
    val qtyChange: Int,
    val previousStock: Int,
    val currentStock: Int,
    val referenceNumber: String,
    val notes: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cashier_shifts")
data class CashierShiftEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val cashierName: String,
    val branchName: String,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val initialCash: Long,
    val totalCashSales: Long = 0L,
    val totalNonCashSales: Long = 0L,
    val totalCashIn: Long = 0L,
    val totalCashOut: Long = 0L,
    val expectedEndingCash: Long = initialCash,
    val actualPhysicalCash: Long? = null,
    val cashDifference: Long = 0L,
    val status: String = "OPEN", // OPEN, CLOSED
    val closingNotes: String = ""
)

@Entity(tableName = "cash_movements")
data class CashMovementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val shiftId: String,
    val type: String, // CASH_IN, CASH_OUT
    val category: String, // Modal Tambahan, Biaya Operasional, Listrik, Gaji, Setor Bank, Lainnya
    val amount: Long,
    val description: String,
    val createdBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val category: String, // Operasional, Listrik & Air, Sewa Tempat, Gaji Karyawan, Transportasi, Pemasaran
    val title: String,
    val amount: Long,
    val paymentMethod: String = "TUNAI",
    val recordedBy: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_orders")
data class PurchaseOrderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val poNumber: String,
    val supplierId: String,
    val supplierName: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val buyPricePerUnit: Long,
    val totalAmount: Long,
    val paymentType: String, // TUNAI, TEMPO (UTANG)
    val status: String, // DITERIMA, DIPESAN, RETUR
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String, // Super Admin, Owner, Manager, Kasir, Gudang
    val pinCode: String,
    val phone: String,
    val branchName: String,
    val canEditPrice: Boolean = false,
    val canGiveDiscount: Boolean = true,
    val canRefund: Boolean = false,
    val canViewReports: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "held_orders")
data class HeldOrderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val customerName: String,
    val tableOrNote: String,
    val itemsJson: String, // Serialized summary of cart items
    val totalAmount: Long,
    val createdAt: Long = System.currentTimeMillis()
)
