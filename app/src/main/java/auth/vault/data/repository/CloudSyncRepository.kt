package auth.vault.data.repository

import auth.vault.data.local.entity.AuthTokenEntity
import auth.vault.data.network.VaultNetworkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class CloudSyncRepository @Inject constructor(
    private val networkClient: VaultNetworkClient
) {

    private val baseUrl = "https://api.authvault.app/v1"

    suspend fun syncTokensToCloud(tokens: List<AuthTokenEntity>): Boolean = withContext(Dispatchers.IO) {
        try {
            networkClient.client.post("$baseUrl/sync/tokens") {
                setBody(tokens)
            }.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncTokensFromCloud(): List<AuthTokenEntity> = withContext(Dispatchers.IO) {
        try {
            networkClient.client.get("$baseUrl/sync/tokens").body<List<AuthTokenEntity>>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun isCloudAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            networkClient.client.get("$baseUrl/health").status.value == 200
        } catch (e: Exception) {
            false
        }
    }
}
