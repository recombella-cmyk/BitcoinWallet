package com.example.bitcoinwallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bitcoinwallet.databinding.ActivityBackupMnemonicBinding

/**
 * Shows a freshly generated mnemonic (new wallet) for the user to write down,
 * OR lets the user type in an existing mnemonic to restore a wallet.
 *
 * SECURITY: the mnemonic is never written to logs, analytics, or storage
 * from this screen. It is passed only in-memory to WalletManager to derive
 * the wallet keys, then discarded.
 */
class BackupMnemonicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupMnemonicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupMnemonicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isNew = intent.getBooleanExtra("isNew", false)
        val isRestore = intent.getBooleanExtra("isRestore", false)

        if (isNew) {
            val mnemonic = intent.getStringExtra("mnemonic") ?: ""
            binding.tvInstructions.text =
                "Catat 12 kata ini di kertas dan simpan di tempat aman. " +
                "Siapa pun yang punya kata-kata ini bisa mengambil dana Anda. " +
                "Jangan difoto atau disimpan di cloud."
            binding.etMnemonic.setText(mnemonic)
            binding.etMnemonic.isEnabled = false
            binding.btnConfirm.text = "Saya sudah mencatatnya, lanjutkan"
            binding.btnConfirm.setOnClickListener {
                proceedToWallet(mnemonic.trim().split(" "))
            }
        } else if (isRestore) {
            binding.tvInstructions.text = "Masukkan 12 kata mnemonic wallet Anda, dipisah spasi."
            binding.etMnemonic.isEnabled = true
            binding.btnConfirm.text = "Pulihkan wallet"
            binding.btnConfirm.setOnClickListener {
                val words = binding.etMnemonic.text.toString().trim().split(Regex("\\s+"))
                if (words.size != 12) {
                    Toast.makeText(this, "Mnemonic harus terdiri dari 12 kata", Toast.LENGTH_SHORT).show()
                } else {
                    proceedToWallet(words)
                }
            }
        }
    }

    private fun proceedToWallet(mnemonicWords: List<String>) {
        val walletManager = WalletManager(this)
        binding.btnConfirm.isEnabled = false
        binding.tvInstructions.text = "Menyiapkan wallet dan menyinkronkan..."

        val creationTime = System.currentTimeMillis() / 1000
        walletManager.startWallet(mnemonicWords, creationTime) {
            runOnUiThread {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
