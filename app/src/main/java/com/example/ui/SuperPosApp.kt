package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.screens.BusinessOperationsScreen
import com.example.ui.screens.DashboardReportsScreen
import com.example.ui.screens.InventoryProductsScreen
import com.example.ui.screens.PosCashierScreen
import com.example.ui.screens.ReceiptModalDialog
import com.example.ui.theme.*

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    POS("Kasir POS", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale, "nav_pos"),
    DASHBOARD("Laporan", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_dashboard"),
    INVENTORY("Produk & Stok", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_inventory"),
    OPERATIONS("Manajemen", Icons.Filled.BusinessCenter, Icons.Outlined.BusinessCenter, "nav_operations")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperPosApp(viewModel: SuperPosViewModel) {
    var currentTab by remember { mutableStateOf(MainDestination.POS) }

    val branches by viewModel.branches.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()
    val activeEmployee by viewModel.activeEmployee.collectAsState()
    val isOnlineMode by viewModel.isOnlineMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val statusMessage by viewModel.statusBannerMessage.collectAsState()
    val receiptModal by viewModel.receiptModal.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val currentBranch = branches.firstOrNull { it.id == selectedBranchId } ?: branches.firstOrNull()
    val pendingSyncCount = transactions.count { it.syncStatus == "PENDING_SYNC" }

    var showBranchSelector by remember { mutableStateOf(false) }

    // Return to main POS cashier tab when pressing system back from secondary tabs
    BackHandler(enabled = currentTab != MainDestination.POS) {
        currentTab = MainDestination.POS
    }

    SuperPosTheme(darkTheme = isDarkMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showBranchSelector = true }
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SUPER POS",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CyanAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = currentBranch?.businessType ?: "UNIVERSAL",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyanAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${currentBranch?.name ?: "Cabang Pusat"} • ${activeEmployee?.name ?: "Kasir"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        // Online / Offline-First indicator pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isOnlineMode) TealSuccess.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.25f),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { viewModel.toggleOnlineMode() }
                                .testTag("online_offline_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOnlineMode) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = "Status Koneksi",
                                    tint = if (isOnlineMode) TealSuccess else AmberWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnlineMode) "Online" else "Offline ($pendingSyncCount)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Ubah Tema"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    MainDestination.entries.forEach { dest ->
                        val selected = currentTab == dest
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = dest },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.title
                                )
                            },
                            label = {
                                Text(
                                    text = dest.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(dest.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedVisibility(visible = statusMessage != null) {
                    statusMessage?.let { msg ->
                        Surface(
                            color = NavySecondary,
                            contentColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearMessage() }) {
                                    Text("OK", color = CyanAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        MainDestination.POS -> PosCashierScreen(viewModel = viewModel)
                        MainDestination.DASHBOARD -> DashboardReportsScreen(viewModel = viewModel)
                        MainDestination.INVENTORY -> InventoryProductsScreen(viewModel = viewModel)
                        MainDestination.OPERATIONS -> BusinessOperationsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Branch & Business Mode Switcher Dialog
        if (showBranchSelector) {
            AlertDialog(
                onDismissRequest = { showBranchSelector = false },
                title = { Text("Pilih Cabang & Jenis Usaha") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pilih Cabang Aktif:", style = MaterialTheme.typography.labelLarge)
                        branches.forEach { b ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectBranch(b)
                                        showBranchSelector = false
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(b.name, fontWeight = FontWeight.Bold)
                                    Text("${b.address} • Mode: ${b.businessType}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBranchSelector = false }) {
                        Text("Tutup")
                    }
                }
            )
        }

        // Global Receipt Preview & Print Modal
        receiptModal?.let { modalData ->
            ReceiptModalDialog(
                receiptData = modalData,
                branch = currentBranch,
                canRefund = activeEmployee?.canRefund ?: true,
                onDismiss = viewModel::dismissReceiptModal,
                onRefund = { viewModel.refundTransaction(modalData.transaction) },
                onPrintFeedback = viewModel::showMessage
            )
        }
    }
}
