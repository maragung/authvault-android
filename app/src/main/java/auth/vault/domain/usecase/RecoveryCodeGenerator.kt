package auth.vault.domain.usecase

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RecoveryCodeGenerator @Inject constructor() {

    data class RecoveryCodes(
        val codeSetId: String,
        val codes: List<String>,
        val generatedAt: Long
    )

    fun generateRecoveryCodeSet(masterPassHash: String, count: Int = 10): RecoveryCodes {
        val codeSetId = generateRandomString(8)
        val codes = (0 until count).map { index ->
            val data = "${masterPassHash}_recovery_${codeSetId}_$index"
            val hmac = Mac.getInstance("HmacSHA256")
            hmac.init(SecretKeySpec(masterPassHash.toByteArray(Charsets.UTF_8), "HmacSHA256"))
            val hash = hmac.doFinal(data.toByteArray(Charsets.UTF_8))
            val base32 = toBase32(hash)
            "${base32.substring(0, 5)}-${base32.substring(5, 10)}-${base32.substring(10, 15)}"
        }
        return RecoveryCodes(
            codeSetId = codeSetId,
            codes = codes,
            generatedAt = System.currentTimeMillis()
        )
    }

    fun generateRandomCode(): String {
        return (0 until 4).joinToString("-") {
            (0 until 5).map {
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"[Random.nextInt(32)]
            }.joinToString("")
        }
    }

    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (0 until length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun toBase32(bytes: ByteArray): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val bits = bytes.joinToString("") { b ->
            (b.toInt() and 0xFF).toString(2).padStart(8, '0')
        }
        val padded = bits.padEnd((bits.length / 5 + 1) * 5, '0')
        return buildString {
            var i = 0
            while (i + 5 <= padded.length) {
                val chunk = padded.substring(i, i + 5)
                append(alphabet[chunk.toInt(2)])
                i += 5
            }
        }
    }
}
