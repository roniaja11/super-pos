package com.example.core.util

import android.content.Context
import android.content.Intent
import com.example.core.database.BranchEntity
import com.example.core.database.PaymentSplitEntity
import com.example.core.database.TransactionEntity
import com.example.core.database.TransactionItemEntity

object ReceiptExportHelper {

    fun buildEscPosReceiptText(
        branch: BranchEntity?,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        splits: List<PaymentSplitEntity> = emptyList()
    ): String {
        val storeName = branch?.name ?: "SUPER POS INDONESIA"
        val storeAddress = branch?.address ?: "Jl. Jend. Sudirman Kav. 52, Jakarta"
        val storePhone = branch?.phone ?: "021-55889900"
        val footer = branch?.receiptFooter ?: "Terima kasih telah berbelanja!"
        val line = "========================================"
        val dash = "----------------------------------------"

        val sb = StringBuilder()
        sb.appendLine(line)
        sb.appendLine(storeName.uppercase())
        sb.appendLine(storeAddress)
        sb.appendLine("Telp: $storePhone")
        sb.appendLine(line)
        sb.appendLine("No Struk  : ${transaction.invoiceNumber}")
        sb.appendLine("Tanggal   : ${FormatUtils.formatDateTime(transaction.createdAt)}")
        sb.appendLine("Kasir     : ${transaction.cashierName}")
        sb.appendLine("Pelanggan : ${transaction.customerName}")
        if (transaction.orderType.isNotBlank()) {
            sb.appendLine("Tipe Order: ${transaction.orderType}")
        }
        sb.appendLine(dash)

        items.forEach { item ->
            sb.appendLine(item.productName)
            val qtyPrice = "${item.quantity} ${item.unit} x ${FormatUtils.formatRupiah(item.unitPrice)}"
            val sub = FormatUtils.formatRupiah(item.subtotal)
            sb.appendLine(String.format("%-24s %15s", qtyPrice, sub))
            if (item.discountPerItem > 0) {
                sb.appendLine("  Diskon Item: -${FormatUtils.formatRupiah(item.discountPerItem * item.quantity)}")
            }
        }

        sb.appendLine(dash)
        sb.appendLine(String.format("%-20s %19s", "Subtotal", FormatUtils.formatRupiah(transaction.subtotal)))
        if (transaction.discountAmount > 0) {
            sb.appendLine(String.format("%-20s %19s", "Diskon Transaksi", "-${FormatUtils.formatRupiah(transaction.discountAmount)}"))
        }
        if (transaction.taxAmount > 0) {
            sb.appendLine(String.format("%-20s %19s", "PPN / Pajak", FormatUtils.formatRupiah(transaction.taxAmount)))
        }
        if (transaction.serviceFee > 0) {
            sb.appendLine(String.format("%-20s %19s", "Service Charge", FormatUtils.formatRupiah(transaction.serviceFee)))
        }
        sb.appendLine(dash)
        sb.appendLine(String.format("%-20s %19s", "TOTAL BAYAR", FormatUtils.formatRupiah(transaction.grandTotal)))

        if (splits.isNotEmpty()) {
            sb.appendLine("Metode Split Payment:")
            splits.forEach { split ->
                sb.appendLine(String.format(" - %-17s %19s", split.method, FormatUtils.formatRupiah(split.amount)))
            }
        } else {
            sb.appendLine(String.format("%-20s %19s", "Metode (${transaction.paymentMethod})", FormatUtils.formatRupiah(transaction.paidAmount)))
        }

        sb.appendLine(String.format("%-20s %19s", "Kembalian", FormatUtils.formatRupiah(transaction.changeAmount)))
        sb.appendLine("Status    : ${transaction.paymentStatus}")
        if (transaction.notes.isNotBlank()) {
            sb.appendLine("Catatan   : ${transaction.notes}")
        }
        sb.appendLine(line)
        sb.appendLine(footer)
        sb.appendLine("Powered by SUPER POS Commercial Engine")
        sb.appendLine(line)
        return sb.toString()
    }

    fun buildFinancialCsvReport(
        transactions: List<TransactionEntity>,
        totalRevenue: Long,
        totalGrossProfit: Long,
        totalExpenses: Long,
        netProfit: Long
    ): String {
        val sb = StringBuilder()
        sb.appendLine("LAPORAN KEUANGAN & TRANSAKSI SUPER POS")
        sb.appendLine("Total Omzet,${totalRevenue}")
        sb.appendLine("Total Laba Kotor,${totalGrossProfit}")
        sb.appendLine("Total Pengeluaran,${totalExpenses}")
        sb.appendLine("Estimasi Laba Bersih,${netProfit}")
        sb.appendLine()
        sb.appendLine("No Invoice,Waktu,Kasir,Pelanggan,Metode,Status,Subtotal,Diskon,Pajak,Total Bayar,HPP")
        transactions.forEach { trx ->
            sb.appendLine(
                "${trx.invoiceNumber},\"${FormatUtils.formatDateTime(trx.createdAt)}\",\"${trx.cashierName}\",\"${trx.customerName}\",${trx.paymentMethod},${trx.paymentStatus},${trx.subtotal},${trx.discountAmount},${trx.taxAmount},${trx.grandTotal},${trx.totalCost}"
            )
        }
        return sb.toString()
    }

    fun shareText(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
