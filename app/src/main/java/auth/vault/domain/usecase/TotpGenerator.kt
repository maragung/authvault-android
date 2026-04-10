package auth.vault.domain.usecase

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TotpGenerator {

    data class TotpResult(
        val code: String,
        val remainingSeconds: Long
    )

    sealed class TotpError {
        data class InvalidSecret(val message: String) : TotpError()
        data class GenerationFailed(val message: String) : TotpError()
    }

    fun generateCodeSafe(secretKey: String, timestamp: Long, digits: Int = 6, algorithm: String = "SHA1"): Result<TotpResult> {
        return runCatching {
            val code = generateCode(secretKey, timestamp, digits, algorithm)
            val remainingSeconds = getRemainingSeconds(timestamp)
            TotpResult(code, remainingSeconds)
        }
    }

    fun generateCode(secretKey: String, timestamp: Long, digits: Int = 6, algorithm: String = "SHA1"): String {
        val decodedKey = decodeSecret(secretKey)
        val timeStep = timestamp / 30L
        val timeBytes = longToBytes(timeStep)
        val hashAlgorithm = when (algorithm.uppercase().replace("-", "")) {
            "SHA1", "HMACSHA1" -> "HmacSHA1"
            "SHA256", "HMACSHA256" -> "HmacSHA256"
            "SHA512", "HMACSHA512" -> "HmacSHA512"
            else -> "HmacSHA1"
        }
        val hmac = Mac.getInstance(hashAlgorithm)
        hmac.init(SecretKeySpec(decodedKey, hashAlgorithm))
        val hash = hmac.doFinal(timeBytes)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = (hash[offset].toInt() and 0x7F) shl 24 or
                (hash[offset + 1].toInt() and 0xFF) shl 16 or
                (hash[offset + 2].toInt() and 0xFF) shl 8 or
                (hash[offset + 3].toInt() and 0xFF)
        val otp = binary % powersOfTen(digits)
        return otp.toString().padStart(digits, '0')
    }

    fun getRemainingSeconds(timestamp: Long, timeStep: Long = 30L): Long {
        return timeStep - (timestamp % timeStep)
    }

    private fun decodeSecret(secret: String): ByteArray {
        val upperSecret = secret.uppercase().replace("\\s+".toRegex(), "")
        val cleanedSecret = upperSecret.replace("=".toRegex(), "")
        val bits = cleanedSecret.map { char ->
            val index = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".indexOf(char)
            if (index == -1) throw IllegalArgumentException("Invalid Base32 character: $char")
            index.toString(2).padStart(5, '0')
        }.joinToString("")
        val paddedBits = bits.padEnd((bits.length / 8) * 8, '0')
        return paddedBits.chunked(8).map { it.toInt(2).toByte() }.toByteArray()
    }

    private fun longToBytes(value: Long): ByteArray {
        return ByteArray(8) { i ->
            ((value ushr (8 * (7 - i))) and 0xFF).toByte()
        }
    }

    private fun powersOfTen(digits: Int): Int {
        var result = 1
        repeat(digits) { result *= 10 }
        return result
    }
}
