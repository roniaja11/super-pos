package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.BranchEntity
import com.example.core.util.FormatUtils
import com.example.core.util.ReceiptExportHelper
import com.example.ui.ReceiptModalData
import com.example.ui.theme.*

@Composable
fun ReceiptModalDialog(
    receiptData: ReceiptModalData,
    branch: BranchEntity?,
    canRefund: Boolean,
    onDismiss: () -> Unit,
    onRefund: () -> Unit,
    onPrintFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val escPosText = remember(receiptData, branch) {
        ReceiptExportHelper.buildEscPosReceiptText(
            branch = branch,
            transaction = receiptData.transaction,
            items = receiptData.items,
            splits = receiptData.splits
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TealSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Struk Transaksi", style = MaterialTheme.typography.titleMedium)
                        Text(
                            receiptData.transaction.invoiceNumber,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    label = { Text(receiptData.transaction.paymentStatus) }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEFCE8),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = escPosText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onPrintFeedback("Mengirim perintah cetak ESC/POS ke Printer Thermal Bluetooth 58mm (${receiptData.transaction.invoiceNumber})…")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Thermal")
                    }

                    Button(
                        onClick = {
                            ReceiptExportHelper.shareText(
                                context = context,
                                title = "Struk ${receiptData.transaction.invoiceNumber}",
                                content = escPosText
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim WA / PDF")
                    }
                }

                if (canRefund && receiptData.transaction.paymentStatus != "REFUNDED") {
                    OutlinedButton(
                        onClick = onRefund,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retur / Refund Transaksi & Kembalikan Stok")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )
}
