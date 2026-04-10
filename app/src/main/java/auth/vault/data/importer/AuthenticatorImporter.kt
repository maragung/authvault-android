package auth.vault.data.importer

import android.content.Context
import auth.vault.data.local.entity.AuthTokenEntity
import auth.vault.util.TimeSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

@Serializable
data class AegisExport(
    val version: Int = 1,
    val header: Header? = null,
    val db: AegisDatabase? = null
)

@Serializable
data class Header(
    val slots: List<Slot>? = null,
    val params: Params? = null
)

@Serializable
data class Slot(
    val type: String? = null,
    val key: String? = null,
    val keyParams: KeyParams? = null
)

@Serializable
data class KeyParams(
    val algo: String? = null,
    val mode: String? = null,
    val iterations: Int? = null,
    val tagSize: Int? = null
)

@Serializable
data class Params(
    val version: Int? = null,
    val slots: List<Slot>? = null
)

@Serializable
data class AegisDatabase(
    val entries: List<AegisEntry>? = null
)

@Serializable
data class AegisEntry(
    val type: String? = null,
    val name: String? = null,
    val issuer: String? = null,
    val info: AegisInfo? = null,
    val secret: String? = null,
    val algo: String? = null,
    val digits: Int? = null,
    val period: Long? = null,
    val counter: Long? = null,
    val note: String? = null
)

@Serializable
data class AegisInfo(
    val originalIssuer: String? = null
)

@Singleton
class AuthenticatorImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timeSource: TimeSource
) {

    fun parseOtpUri(uri: String): ImportResult = runCatching {
        val parts = uri.removePrefix("otpauth://").split("/")
        if (parts.size < 2) return@runCatching ImportResult.Error("Invalid URI format")

        val type = parts[0]
        val labelPart = parts[1]
        val queryParams = uri.substringAfter("?", "")

        val secret = queryParams.substringAfter("secret=", "").substringBefore("&")
            .ifEmpty { return@runCatching ImportResult.Error("Missing secret") }

        val issuer = queryParams.substringAfter("issuer=", "").substringBefore("&")
            .ifEmpty { labelPart.substringBefore(":", "").ifEmpty { "Unknown" } }

        val accountName = labelPart.substringAfter(":", "").ifEmpty { labelPart }

        val algorithm = queryParams.substringAfter("algorithm=", "SHA1").substringBefore("&").uppercase()
        val digits = queryParams.substringAfter("digits=", "6").substringBefore("&").toIntOrNull() ?: 6
        val period = queryParams.substringAfter("period=", "30").substringBefore("&").toLongOrNull() ?: 30L

        val tokenType = if (type == "totp") "totp" else "hotp"

        ImportResult.Success(
            listOf(
                AuthTokenEntity(
                    serviceLabel = issuer,
                    accountName = accountName,
                    secretKey = secret,
                    algorithm = algorithm,
                    digitCount = digits,
                    timeStep = period,
                    tokenCategory = tokenType,
                    createdAt = timeSource.currentTimeMillis()
                )
            )
        )
    }.getOrElse { ImportResult.Error(it.message ?: "Failed to parse URI") }

    fun parseAegisExport(inputStream: InputStream): ImportResult = runCatching {
        val json = Json { ignoreUnknownKeys = true }
        val content = inputStream.bufferedReader().use { it.readText() }
        val export = json.decodeFromString<AegisExport>(content)

        val tokens = export.db?.entries?.mapNotNull { entry ->
            try {
                AuthTokenEntity(
                    serviceLabel = entry.issuer ?: entry.info?.originalIssuer ?: "Unknown",
                    accountName = entry.name ?: "",
                    secretKey = entry.secret ?: return@mapNotNull null,
                    algorithm = entry.algo ?: "SHA1",
                    digitCount = entry.digits ?: 6,
                    timeStep = entry.period ?: 30L,
                    tokenCategory = entry.type ?: "totp",
                    createdAt = timeSource.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

        if (tokens.isEmpty()) ImportResult.Error("No tokens found in export")
        else ImportResult.Success(tokens)
    }.getOrElse { ImportResult.Error(it.message ?: "Failed to parse Aegis export") }

    fun parseGoogleAuthenticatorExport(inputStream: InputStream): ImportResult = runCatching {
        val json = Json { ignoreUnknownKeys = true }
        val content = inputStream.bufferedReader().use { it.readText() }

        val entries = json.decodeFromString<List<GoogleAuthEntry>>(content)
        val tokens = entries.mapNotNull { entry ->
            try {
                val uri = entry.uri
                parseOtpUri(uri).let { result ->
                    if (result is ImportResult.Success) result.tokens.firstOrNull() else null
                }
            } catch (e: Exception) {
                null
            }
        }

        if (tokens.isEmpty()) ImportResult.Error("No tokens found in export")
        else ImportResult.Success(tokens)
    }.getOrElse { ImportResult.Error(it.message ?: "Failed to parse Google Authenticator export") }

    sealed class ImportResult {
        data class Success(val tokens: List<AuthTokenEntity>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}

@Serializable
data class GoogleAuthEntry(
    val uri: String = "",
    val name: String? = null
)
