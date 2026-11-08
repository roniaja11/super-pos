package com.example

import com.example.core.database.ProductEntity
import com.example.core.database.TransactionEntity
import com.example.core.database.TransactionItemEntity
import com.example.core.util.FormatUtils
import com.example.core.util.ReceiptExportHelper
import com.example.data.CartItem
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun cartItem_calculatesRetailAndWholesalePriceCorrectly() {
        val sampleProduct = ProductEntity(
            id = "p-1",
            name = "Es Kopi Susu",
            sku = "KOP-01",
            barcode = "899001",
            categoryId = "cat-1",
            categoryName = "Minuman",
            buyPrice = 10_000L,
            sellPrice = 20_000L,
            wholesalePrice = 17_000L,
            minWholesaleQty = 10,
            stock = 50
        )

        val retailCartItem = CartItem(product = sampleProduct, quantity = 3)
        assertFalse(retailCartItem.isWholesaleApplied)
        assertEquals(20_000L, retailCartItem.effectiveUnitPrice)
        assertEquals(60_000L, retailCartItem.lineSubtotal)
        assertEquals(30_000L, retailCartItem.lineCost)

        val wholesaleCartItem = CartItem(product = sampleProduct, quantity = 10, itemDiscount = 500L)
        assertTrue(wholesaleCartItem.isWholesaleApplied)
        assertEquals(17_000L, wholesaleCartItem.effectiveUnitPrice)
        assertEquals(165_000L, wholesaleCartItem.lineSubtotal)
    }

    @Test
    fun receiptGenerator_includesAllMandatoryFields() {
        val trx = TransactionEntity(
            id = "t-1",
            invoiceNumber = "INV-261108-9999",
            branchId = "b-1",
            shiftId = "s-1",
            cashierName = "Nadia Putri",
            customerId = "c-1",
            customerName = "Budi Santoso",
            subtotal = 50_000L,
            discountAmount = 5_000L,
            taxAmount = 4_950L,
            grandTotal = 49_950L,
            totalCost = 25_000L,
            paidAmount = 50_000L,
            changeAmount = 50L,
            paymentMethod = "TUNAI"
        )
        val items = listOf(
            TransactionItemEntity(
                transactionId = "t-1",
                productId = "p-1",
                productName = "Kopi Aren",
                sku = "KOP-01",
                unit = "Cup",
                quantity = 2,
                buyPrice = 12_500L,
                unitPrice = 25_000L,
                subtotal = 50_000L
            )
        )
        val receipt = ReceiptExportHelper.buildEscPosReceiptText(null, trx, items)
        assertTrue(receipt.contains("INV-261108-9999"))
        assertTrue(receipt.contains("Nadia Putri"))
        assertTrue(receipt.contains("Kopi Aren"))
        assertTrue(FormatUtils.formatRupiah(49_950L).isNotBlank())
    }
}
