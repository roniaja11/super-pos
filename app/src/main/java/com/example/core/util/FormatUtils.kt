package com.example.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val indonesiaLocale = Locale("id", "ID")

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(indonesiaLocale)
        return "Rp ${formatter.format(amount)}"
    }

    fun formatCompactRupiah(amount: Long): String {
        return when {
            amount >= 1_000_000_000L -> String.format(indonesiaLocale, "Rp %.1f M", amount / 1_000_000_000.0)
            amount >= 1_000_000L -> String.format(indonesiaLocale, "Rp %.1f Jt", amount / 1_000_000.0)
            amount >= 1_000L -> String.format(indonesiaLocale, "Rp %d Rb", amount / 1_000L)
            else -> formatRupiah(amount)
        }
    }

    fun formatNumber(value: Long): String {
        return NumberFormat.getNumberInstance(indonesiaLocale).format(value)
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesiaLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesiaLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", indonesiaLocale)
        return sdf.format(Date(timestamp))
    }

    fun generateInvoiceNumber(prefix: String = "INV"): String {
        val sdf = SimpleDateFormat("yyMMdd", Locale.US)
        val datePart = sdf.format(Date())
        val randomSuffix = (1000..9999).random()
        return "$prefix-$datePart-$randomSuffix"
    }
}
