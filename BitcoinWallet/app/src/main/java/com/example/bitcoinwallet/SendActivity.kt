package com.example.bitcoinwallet

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bitcoinwallet.databinding.ActivitySendBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding
    private lateinit var walletManager: WalletManager
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        walletManager = WalletManager(this)

        binding.btnConfirmSend.setOnClickListener {
            val address = binding.etAddress.text.toString().trim()
            val amount = binding.etAmount.text.toString().trim()

            if (address.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Isi alamat dan jumlah terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Konfirmasi kirim")
                .setMessage("Kirim $amount BTC ke:\n$address\n\nTransaksi Bitcoin tidak bisa dibatalkan. Lanjutkan?")
                .setPositiveButton("Kirim") { _, _ -> doSend(address, amount) }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun doSend(address: String, amount: String) {
        binding.btnConfirmSend.isEnabled = false
        binding.tvSendStatus.text = "Mengirim transaksi..."

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                walletManager.sendCoins(address, amount)
            }
            result.onSuccess { txId ->
                binding.tvSendStatus.text = "Berhasil dikirim. TX ID:\n$txId"
            }.onFailure { err ->
                binding.tvSendStatus.text = "Gagal mengirim: ${err.message}"
                binding.btnConfirmSend.isEnabled = true
            }
        }
    }
}
