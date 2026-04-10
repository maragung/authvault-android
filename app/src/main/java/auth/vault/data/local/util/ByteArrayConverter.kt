package auth.vault.data.local.util

import androidx.room.TypeConverter

class ByteArrayConverter {

    @TypeConverter
    fun fromByteArray(bytes: ByteArray?): String? {
        return bytes?.joinToString(separator = "") { b -> "%02x".format(b) }
    }

    @TypeConverter
    fun toByteArray(hexString: String?): ByteArray? {
        return hexString?.chunked(2)?.map { it.toInt(16).toByte() }?.toByteArray()
    }
}
