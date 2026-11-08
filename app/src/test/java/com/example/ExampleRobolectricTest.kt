package com.example

import com.example.core.util.FormatUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `format rupiah and generate invoice number`() {
        val formatted = FormatUtils.formatRupiah(150_000L)
        assertTrue(formatted.contains("150"))
        val invoice = FormatUtils.generateInvoiceNumber("INV")
        assertTrue(invoice.startsWith("INV-"))
    }
}
