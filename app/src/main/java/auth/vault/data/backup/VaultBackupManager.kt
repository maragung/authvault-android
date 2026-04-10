package auth.vault.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import auth.vault.data.local.entity.AuthTokenEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore

@Singleton
class VaultBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val BACKUP_CIPHER = "AES/GCM/NoPadding"
        private const val TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val BACKUP_KEY_ALIAS = "_authvault_backup_key_"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    }

    suspend fun exportEncryptedBackup(tokens: List<AuthTokenEntity>): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (!keyStore.containsAlias(BACKUP_KEY_ALIAS)) {
                val keyGenerator = javax.crypto.KeyGenerator.getInstance("AES", KEYSTORE_PROVIDER)
                val keyGenSpec = android.security.keystore.KeyGenParameterSpec.Builder(
                    BACKUP_KEY_ALIAS,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenSpec)
                keyGenerator.generateKey()
            }

            val secretKey = keyStore.getKey(BACKUP_KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(BACKUP_CIPHER)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val json = Json { prettyPrint = true }
            val plaintext = json.encodeToString(tokens)
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
            val iv = cipher.iv
            val backupData = iv + encryptedBytes

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "AuthVault_Backup_Encrypted_$timestamp.avb"
            val file = File(context.cacheDir, fileName)
            file.writeBytes(backupData)

            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }.getOrNull()
    }

    suspend fun importEncryptedBackup(uri: Uri): List<AuthTokenEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val backupData = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Cannot read backup file")

            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            val secretKey = keyStore.getKey(BACKUP_KEY_ALIAS, null) as SecretKey

            val iv = backupData.copyOf(GCM_IV_LENGTH)
            val encryptedData = backupData.sliceArray(GCM_IV_LENGTH until backupData.size)

            val cipher = Cipher.getInstance(BACKUP_CIPHER)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plaintext = cipher.doFinal(encryptedData)

            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<AuthTokenEntity>>(String(plaintext))
        }.getOrElse { emptyList() }
    }
}
