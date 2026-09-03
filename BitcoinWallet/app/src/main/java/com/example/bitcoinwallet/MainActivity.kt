package com.example.bitcoinwallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bitcoinwallet.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var walletManager: WalletManager
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        walletManager = WalletManager(this)

        binding.tvNetwork.text = if (WalletManager.USE_TESTNET)
            "Jaringan: TESTNET (koin tidak bernilai asli)"
        else
            "Jaringan: MAINNET (koin asli - hati-hati!)"

        if (walletManager.walletExists()) {
            // Wallet already created previously - just start it.
            startExistingWallet()
        } else {
            showCreateOrRestoreDialog()
        }

        binding.btnSend.setOnClickListener {
            startActivity(Intent(this, SendActivity::class.java))
        }

        binding.btnRefresh.setOnClickListener {
            refreshBalance()
        }

        binding.btnCopyAddress.setOnClickListener {
            val address = walletManager.getReceiveAddress()?.toString()
            if (address != null) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("BTC Address", address))
                Toast.makeText(this, "Alamat disalin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCreateOrRestoreDialog() {
        val options = arrayOf("Buat wallet baru", "Pulihkan dari mnemonic")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Selamat datang")
            .setItems(options) { _, which ->
                if (which == 0) {
                    val mnemonic = walletManager.generateMnemonic()
                    val intent = Intent(this, BackupMnemonicActivity::class.java)
                    intent.putExtra("mnemonic", mnemonic.joinToString(" "))
                    intent.putExtra("isNew", true)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, BackupMnemonicActivity::class.java)
                    intent.putExtra("isRestore", true)
                    startActivity(intent)
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun startExistingWallet() {
        binding.tvStatus.text = "Menyinkronkan dengan jaringan..."
        scope.launch {
            // Note: in a production app, store the mnemonic creation time
            // and load it via encrypted storage instead of relying on the
            // wallet file alone. This starter uses bitcoinj's saved wallet file directly.
            withContext(Dispatchers.IO) {
                // WalletAppKit auto-loads existing wallet file from disk.
                walletManager.startWallet(emptyList(), 0L) {
                    runOnUiThread {
                        binding.tvStatus.text = "Tersambung"
                        binding.tvAddress.text = walletManager.getReceiveAddress()?.toString() ?: "-"
                        refreshBalance()
                    }
                }
            }
        }
    }

    private fun refreshBalance() {
        binding.tvBalance.text = walletManager.getBalanceBtc()
    }

    override fun onDestroy() {
        super.onDestroy()
        walletManager.stop()
    }
}
