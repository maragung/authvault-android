package auth.vault.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "_authvault_master_key_"
        private const val DB_KEY_FILE = "db_key.enc"
        private const val GCM_TAG_LENGTH = 128
    }

    val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun getDatabasePassphrase(): ByteArray {
        val keyFile = File(context.filesDir, DB_KEY_FILE)
        return if (keyFile.exists()) {
            decryptBytes(keyFile.readBytes())
        } else {
            val passphrase = generateRandomPassphrase()
            keyFile.writeBytes(encryptBytes(passphrase))
            passphrase
        }
    }

    private fun encryptBytes(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKeyAlias())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    private fun decryptBytes(ciphertextWithIv: ByteArray): ByteArray {
        val iv = ciphertextWithIv.copyOf(12)
        val ciphertext = ciphertextWithIv.sliceArray(12 until ciphertextWithIv.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getMasterKeyAlias(), spec)
        return cipher.doFinal(ciphertext)
    }

    private fun generateRandomPassphrase(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        val key = keyGenerator.generateKey()
        return key.encoded
    }

    private fun getMasterKeyAlias(): Key {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        return keyStore.getKey(MASTER_KEY_ALIAS, null)
    }

    fun clearEncryptionKeys() {
        runCatching {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
            File(context.filesDir, DB_KEY_FILE).delete()
        }
    }
}
