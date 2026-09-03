# Dompet Bitcoin (Non-Custodial) — Android

Starter project dompet Bitcoin non-custodial menggunakan **bitcoinj**
(library Bitcoin yang sudah teruji luas, bukan implementasi kripto buatan sendiri).

## ⚠️ BACA DULU SEBELUM PAKAI UNTUK UANG ASLI

1. **Default-nya TESTNET.** Buka `WalletManager.kt` dan cek baris:
   ```kotlin
   const val USE_TESTNET = true
   ```
   Coba dulu semua fitur (buat wallet, terima, kirim, restore) di testnet
   dengan koin testnet gratis dari "Bitcoin testnet faucet" sebelum menyentuh
   mainnet. Baru ubah ke `false` kalau sudah yakin.

2. **Mnemonic (12 kata) = kunci ke semua dana.** Aplikasi ini menampilkannya
   sekali saat wallet dibuat. Catat di kertas, simpan offline di tempat aman.
   Jangan difoto, jangan disimpan di catatan HP/cloud, jangan dikirim ke
   siapa pun (termasuk saya).

3. **Belum diaudit.** Ini kerangka dasar yang fungsional, bukan produk jadi
   yang siap dipakai publik. Untuk pemakaian serius dengan dana besar,
   sebaiknya minta audit keamanan independen sebelum rilis, dan pertimbangkan
   fitur tambahan seperti enkripsi wallet dengan PIN/biometrik (bitcoinj
   mendukung `wallet.encrypt()`), backup terenkripsi, dan proteksi terhadap
   clipboard-hijacking saat menyalin alamat.

4. **Cara kerja sync:** aplikasi ini pakai SPV (Simplified Payment
   Verification) dari bitcoinj — tidak perlu download seluruh blockchain,
   tapi tetap perlu koneksi internet untuk sinkron dengan peer Bitcoin.

## Cara membuka & compile

1. Buka **Android Studio** (versi terbaru disarankan).
2. `File → Open`, pilih folder `BitcoinWallet` ini.
3. Tunggu Gradle sync selesai (akan otomatis download dependency termasuk `bitcoinj-core`).
4. Klik ▶ Run untuk build & jalankan di emulator/HP (minimum Android 8.0 / API 26).

## Struktur project

```
BitcoinWallet/
├── app/
│   ├── build.gradle              # dependency bitcoinj, dsb.
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/bitcoinwallet/
│       │   ├── WalletManager.kt          # logika inti wallet (bitcoinj)
│       │   ├── MainActivity.kt           # layar utama: saldo, alamat
│       │   ├── BackupMnemonicActivity.kt # buat/pulihkan wallet
│       │   └── SendActivity.kt           # kirim BTC
│       └── res/layout/...
```

## Alur pemakaian

1. Pertama buka app → pilih "Buat wallet baru" atau "Pulihkan dari mnemonic".
2. Kalau baru: 12 kata mnemonic ditampilkan → catat → lanjutkan.
3. App sinkron ke jaringan Bitcoin (testnet by default) via SPV.
4. Layar utama menampilkan saldo dan alamat untuk menerima BTC.
5. Tombol "Kirim" untuk transfer ke alamat lain (ada dialog konfirmasi).

## Pengembangan lanjutan yang disarankan

- Enkripsi wallet file dengan password/PIN (`wallet.encrypt(...)`)
- Autentikasi biometrik sebelum membuka app atau mengirim
- Fee estimation yang lebih baik (saat ini pakai default bitcoinj)
- QR code scanner untuk alamat tujuan
- Riwayat transaksi (bitcoinj punya `wallet.getTransactions()`)
- Peninjauan kode oleh auditor keamanan sebelum dipakai dengan dana nyata
