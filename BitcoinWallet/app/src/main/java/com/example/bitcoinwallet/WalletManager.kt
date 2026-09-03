package com.example.bitcoinwallet

import android.content.Context
import android.util.Log
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import java.io.File
import java.security.SecureRandom

/**
 * WalletManager wraps bitcoinj's WalletAppKit to manage a non-custodial
 * Bitcoin SPV wallet.
 *
 * IMPORTANT / SECURITY NOTES:
 * - USE_TESTNET is true by default. Testnet coins have no real value, so you
 *   can safely test everything (receive, send, restore) before ever touching
 *   mainnet. Only flip this to false once you're confident and ideally after
 *   a security review.
 * - The mnemonic (12-word seed phrase) is the ONLY backup of this wallet.
 *   Anyone with the words can spend the funds. Never log it, screenshot it,
 *   or send it anywhere. It's shown once at creation time.
 * - Wallet files are stored in the app's private internal storage. Because
 *   android:allowBackup="false" is set in the manifest, Android will not
 *   auto-backup this file to the cloud.
 */
class WalletManager(private val context: Context) {

    companion object {
        private const val TAG = "WalletManager"

        // Change this to false ONLY when you're ready for real mainnet funds.
        const val USE_TESTNET = true

        val params: NetworkParameters =
            if (USE_TESTNET) TestNet3Params.get() else MainNetParams.get()

        private const val WALLET_PREFIX = "bitcoin_wallet"
    }

    private var kit: WalletAppKit? = null

    /** True if a wallet file already exists on disk. */
    fun walletExists(): Boolean {
        val dir = context.filesDir
        return File(dir, "$WALLET_PREFIX.wallet").exists()
    }

    /**
     * Generates a brand-new 12-word mnemonic. Show this to the user ONE TIME
     * so they can write it down offline, then call startWallet(mnemonic).
     */
    fun generateMnemonic(): List<String> {
        val entropy = ByteArray(16) // 128 bits -> 12 words
        SecureRandom().nextBytes(entropy)
        return MnemonicCode.INSTANCE.toMnemonic(entropy)
    }

    /**
     * Starts (or restores) the wallet from a mnemonic phrase and begins
     * syncing with the Bitcoin network via SPV (lightweight sync, no need to
     * download the full blockchain).
     */
    fun startWallet(mnemonicWords: List<String>, creationTimeSeconds: Long, onReady: () -> Unit) {
        val dir = context.filesDir

        kit = object : WalletAppKit(params, dir, WALLET_PREFIX) {
            override fun onSetupCompleted() {
                Log.i(TAG, "Wallet setup complete. Address: ${wallet().currentReceiveAddress()}")
                onReady()
            }
        }.apply {
            // Only restore from a seed phrase when one was actually provided
            // (new wallet or explicit restore flow). If a wallet file already
            // exists on disk, WalletAppKit loads it automatically instead.
            if (mnemonicWords.isNotEmpty()) {
                val seed = DeterministicSeed(mnemonicWords, null, "", creationTimeSeconds)
                restoreWalletFromSeed(seed)
            }
            setBlockingStartup(false)
            startAsync()
        }
    }

    fun getWallet(): Wallet? = kit?.wallet()

    fun getReceiveAddress(): Address? = kit?.wallet()?.currentReceiveAddress()

    /** Returns confirmed + unconfirmed balance in BTC as a human-readable string. */
    fun getBalanceBtc(): String {
        val balance: Coin = kit?.wallet()?.balance ?: Coin.ZERO
        return balance.toFriendlyString()
    }

    /**
     * Sends BTC to the given address. amountBtc is a decimal string, e.g. "0.001".
     * Returns a Result with the transaction id on success, or the error on failure.
     */
    fun sendCoins(toAddress: String, amountBtc: String): Result<String> {
        return try {
            val wallet = kit?.wallet() ?: return Result.failure(IllegalStateException("Wallet belum siap"))
            val address = Address.fromString(params, toAddress)
            val amount = Coin.parseCoin(amountBtc)
            val sendRequest = SendRequest.to(address, amount)
            val result = wallet.sendCoins(kit!!.peerGroup(), sendRequest)
            val txId = result.tx.txId.toString()
            Result.success(txId)
        } catch (e: Exception) {
            Log.e(TAG, "Send failed", e)
            Result.failure(e)
        }
    }

    fun stop() {
        kit?.stopAsync()
    }
}
