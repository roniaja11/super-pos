# SUPER POS — Sistem Kasir dan Manajemen Bisnis All-in-One

SUPER POS adalah aplikasi kasir (Point of Sale), manajemen stok/inventori, pencatatan keuangan, dan analitik bisnis all-in-one untuk Android yang dirancang dengan pendekatan **Offline-First**, performa tinggi, dan antarmuka modern Material 3 bertema Navy & Electric Blue. Aplikasi ini mendukung operasional universal (Retail, F&B, Jasa, Grosir) dengan transaksi kasir cepat, pemindaian barcode, split payment, struk digital/cetak ESC-POS, shift kasir, dan laporan laba-rugi otomatis.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan jawaban klarifikasi yang telah dikonfirmasi:
> 1. **Profil Bisnis Awal**: Universal Multi-bidang usaha (dapat beralih antara mode Retail/Minimarket, Restoran/Kafe dengan pesanan meja, dan Jasa kapan saja).
> 2. **Data Awal**: Memuat data katalog awal yang realistis (kategori, produk bervariasi harga/barcode/stok, pelanggan, supplier, serta riwayat penjualan dan pengeluaran) agar dashboard dan laporan analitik langsung berfungsi penuh sejak peluncuran pertama.
> 3. **Arah Desain UI**: Navy & Electric Blue modern responsif dengan tombol kasir berukuran sentuh optimal (min 48dp), navigasi cepat, dan kartu informasi finansial yang jernih.

---

## 1. Overview & Core Concept

- **Apa yang Dibangun**: Aplikasi Android lengkap dengan Room SQLite Database untuk menjamin kasir tetap dapat bertransaksi 100% tanpa internet. Dilengkapi keranjang belanja real-time, kalkulasi pajak, diskon fleksibel, pencatatan kas masuk/keluar, shift kasir dengan audit selisih kas fisik, hingga ekspor struk transaksi.
- **Target Pengguna**: Pemilik usaha (Owner/Super Admin), Manajer toko, dan Kasir yang membutuhkan kecepatan transaksi di meja kasir serta visibilitas keuangan yang presisi.
- **Nilai Utama**:
  - Nol downtime: Tetap berfungsi saat jaringan putus (Offline-First).
  - Kecepatan checkout: Tambah item via quick-tap grid, pencarian instan, atau scan barcode.
  - Akurasi stok & uang: Pergerakan stok terhubung langsung ke mutasi penjualan dan pembelian.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Kasir / POS Flow**:
   - Kasir membuka aplikasi -> Memilih shift aktif (atau input modal kas awal).
   - Memilih produk dari grid kategori / mengetik pencarian / scan barcode.
   - Menyesuaikan qty, diskon item, atau catatan pesanan.
   - Menekan tombol **Bayar** (Total otomatis: Subtotal - Diskon + Pajak).
   - Memilih metode pembayaran: Tunai (pilihan nominal cepat + kembalian otomatis), QRIS Dinamis, Transfer Bank, atau Debit/Kredit (Split Payment didukung).
   - Menyelesaikan transaksi -> Mencetak / membagikan struk transaksi (ESC/POS preview & share teks/gambar). Stok otomatis terpotong di database.
2. **Manajemen Produk & Stok**:
   - Menambah/mengedit produk dengan barcode, SKU, harga modal, harga jual, stok minimum, dan kategori.
   - Riwayat kartu stok (Stock Movement): Masuk, Keluar, Penyesuaian, dan Opname.
3. **Shift Kasir & Tutup Buku**:
   - Buka kasir dengan uang modal.
   - Catat pengeluaran operasional mendadak (Kas Keluar).
   - Tutup shift: input penghitungan fisik uang tunai, sistem menghitung selisih secara otomatis, dan mencetak laporan ringkasan shift.
4. **Laporan & Dashboard**:
   - Kartu ringkasan omzet, laba kotor, laba bersih estimasi, nilai inventori, dan transaksi hari ini.
   - Grafik interaktif tren penjualan dan diagram metode pembayaran.

### Visual Identity & Theme
- **Aesthetic**: Premium Enterprise POS — modern, bersih, berwibawa, dan kontras tinggi untuk visibilitas cepat di layar toko.
- **Color Tokens**:
  - Primary: Deep Navy (`#0A192F` / `#1E3A8A`)
  - Secondary: Electric Blue (`#2563EB` / `#3B82F6`)
  - Accent / Success: Emerald Teal (`#10B981`) untuk uang masuk dan status lunas
  - Warning / Alert: Amber Gold (`#F59E0B`) untuk stok menipis dan pending
  - Error: Coral Red (`#EF4444`) untuk pembatalan/retur/kas keluar
  - Surface & Background: Light Slate (`#F8FAFC`) dengan kartu putih berbayang halus (`#FFFFFF`)
- **Typography & Layout**:
  - Angka nominal besar dan tebal (`FontWeight.Bold`, format rupiah `Rp 150.000`)
  - Touch target minimal 48dp x 48dp pada setiap tombol kasir dan numpad pembayaran.
  - Safe area handling: Mendukung Edge-to-Edge (`WindowInsets.safeDrawing`).

---

## 3. Key Product Decisions & Trade-Offs

1. **Penyimpanan Offline-First Menggunakan Room SQLite**:
   - *Pilihan*: Menggunakan Room Database lokal dengan DAO terstruktur dan skema relasional (Produk, Transaksi, Item, Stok, Kas, Pelanggan, Supplier).
   - *Alasan*: Aplikasi kasir wajib bekerja secara instan tanpa latensi jaringan dan tidak boleh gagal saat internet mati.
2. **Struktur Penyimpanan Uang dalam Long / Int**:
   - *Pilihan*: Seluruh nominal rupiah disimpan sebagai `Long` (dalam satuan Rupiah penuh tanpa floating point).
   - *Alasan*: Menghindari galat pembulatan floating point (`Double`/`Float`) pada perhitungan pajak dan diskon.
3. **Navigasi Tab Adaptif**:
   - *Pilihan*: Bottom Navigation Bar untuk 5 menu utama (Kasir, Produk, Transaksi, Laporan, Pengaturan/Lainnya) dengan sub-menu modal/drawer untuk fitur lanjutan (Shift, Supplier, Pelanggan, Kas).
   - *Alasan*: Menjaga layar kasir tetap bersih dan fokus pada kecepatan entri order.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          SUPER POS UI LAYER                            │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │ PosCashierView│  │ ProductMgmt   │  │ ReportsView │  │ ShiftMgmt  │ │
│  │ (Grid, Cart,  │  │ (Catalog, SKU,│  │ (Analytics, │  │ (Cash In/  │ │
│  │  Checkout)    │  │  Stock Ops)   │  │  P&L, Cash) │  │  Out, Audit│ │
│  └───────┬───────┘  └───────┬───────┘  └──────┬──────┘  └─────┬──────┘ │
├──────────┼──────────────────┼─────────────────┼───────────────┼────────┤
│          ▼                  ▼                 ▼               ▼        │
│                           VIEWMODELS                                   │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ PosViewModel / ProductViewModel / ReportViewModel / ShiftViewModel│  │
│  │ (Exposes StateFlow<UiState>, handles validation & state machine)  │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
├─────────────────────────────────────┼──────────────────────────────────┤
│                                     ▼                                  │
│                          REPOSITORY & DOMAIN LAYER                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ PosRepository (Transactional order execution, stock updates)      │  │
│  │ InventoryRepository, CashierShiftRepository, CustomerRepository  │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
├─────────────────────────────────────┼──────────────────────────────────┤
│                                     ▼                                  │
│                          LOCAL STORAGE (ROOM DB)                       │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ AppDatabase (Room):                                              │  │
│  │ - Products & Categories          - Customers & Suppliers         │  │
│  │ - Transactions & SaleItems       - CashierShifts & CashMovements │  │
│  │ - StockMovements (Audit Log)     - PaymentRecords & AppSettings  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Entities & Data Model
- `ProductEntity`: id, code/barcode, name, categoryId, buyPrice, sellPrice, currentStock, minStock, unit, isActive.
- `CategoryEntity`: id, name, iconName, colorHex.
- `TransactionEntity`: id, invoiceNumber, cashierName, customerId, subtotal, discountAmount, taxAmount, grandTotal, paymentMethod, paymentStatus, paidAmount, changeAmount, createdAt, shiftId, notes.
- `TransactionItemEntity`: id, transactionId, productId, productName, unitPrice, costPrice, quantity, subtotalDiscount, subtotal.
- `StockMovementEntity`: id, productId, type (`SALE`, `PURCHASE`, `ADJUSTMENT`, `RETURN`), quantityChange, balanceAfter, timestamp, referenceNote.
- `CashierShiftEntity`: id, cashierName, openedAt, closedAt, initialCash, cashIn, cashOut, totalSalesCash, totalSalesNonCash, expectedCash, actualCash, cashDifference, status (`OPEN`, `CLOSED`).
- `CashMovementEntity`: id, shiftId, type (`CASH_IN`, `CASH_OUT`), amount, reason, timestamp.
- `CustomerEntity` & `SupplierEntity`: data relasi kontak & riwayat.

### Execution Phases
1. **Tahap 1 — Database & Fondasi**: Setup Entity Room, DAO, TypeConverter, Repository, dan data seeder komprehensif.
2. **Tahap 2 — Antarmuka Kasir & Keranjang**: Komponen Quick-Cart, Search/Barcode Filter, Diskon, Pajak, dan Dialog Pembayaran (Tunai, QRIS, Transfer, Kartu).
3. **Tahap 3 — Struk & Manajemen Transaksi**: Pembuatan struk transaksi terformat rapi dengan fitur cetak simulasi / cetak Bluetooth thermal ESC/POS dan share WhatsApp.
4. **Tahap 4 — Shift Kasir & Arus Kas**: Buka/tutup shift kasir, input kas masuk/keluar operasional, dan laporan selisih kas.
5. **Tahap 5 — Manajemen Produk, Stok & Supplier**: Tambah/edit produk, penyesuaian stok, dan kartu stok.
6. **Tahap 6 — Laporan Bisnis & Analitik**: Dashboard metrik penjualan, grafik harian, laporan laba-rugi (pendapatan - HPP - pengeluaran), dan ekspor data.
7. **Tahap 7 — Verifikasi & Pengujian**: Build APK via `compile_applet` dan unit testing logika kalkulasi kasir & stok.
