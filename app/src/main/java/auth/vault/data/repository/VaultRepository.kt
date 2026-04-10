package auth.vault.data.repository

import auth.vault.data.importer.AuthenticatorImporter
import auth.vault.data.local.dao.TokenDao
import auth.vault.data.local.entity.AuthTokenEntity
import auth.vault.data.settings.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    private val tokenDao: TokenDao,
    private val settingsRepository: SettingsRepository,
    private val importer: AuthenticatorImporter
) {

    fun getAllTokens(): Flow<List<AuthTokenEntity>> = runCatching {
        tokenDao.getAllTokens().catch { emit(emptyList()) }
    }.getOrDefault(flowOf(emptyList()))

    fun getTokensByCategory(category: String): Flow<List<AuthTokenEntity>> = runCatching {
        tokenDao.getTokensByCategory(category).catch { emit(emptyList()) }
    }.getOrDefault(flowOf(emptyList()))

    fun getTokensByTag(tag: String): Flow<List<AuthTokenEntity>> = runCatching {
        tokenDao.getTokensByTag(tag).catch { emit(emptyList()) }
    }.getOrDefault(flowOf(emptyList()))

    fun getAllTags(): Flow<List<String>> = runCatching {
        tokenDao.getAllTags().catch { emit(emptyList()) }
    }.getOrDefault(flowOf(emptyList()))

    fun getTokenCount(): Flow<Int> = runCatching {
        tokenDao.getTokenCount().catch { emit(0) }
    }.getOrDefault(flowOf(0))

    suspend fun getTokenById(tokenId: Long): AuthTokenEntity? = runCatching {
        tokenDao.getTokenById(tokenId)
    }.getOrNull()

    suspend fun addToken(token: AuthTokenEntity): Long = runCatching {
        tokenDao.insertToken(token)
    }.getOrDefault(-1L)

    suspend fun addTokens(tokens: List<AuthTokenEntity>): List<Long> = runCatching {
        tokenDao.insertTokens(tokens)
    }.getOrDefault(emptyList())

    suspend fun updateToken(token: AuthTokenEntity) = runCatching {
        tokenDao.updateToken(token)
    }

    suspend fun removeToken(token: AuthTokenEntity) = runCatching {
        tokenDao.deleteToken(token)
    }

    suspend fun removeTokenById(tokenId: Long) = runCatching {
        tokenDao.deleteTokenById(tokenId)
    }

    suspend fun clearAllTokens() = runCatching {
        tokenDao.clearAllTokens()
    }

    suspend fun recordTokenAccess(tokenId: Long, timestamp: Long) = runCatching {
        tokenDao.recordTokenAccess(tokenId, timestamp)
    }

    suspend fun togglePinToken(tokenId: Long, pinned: Boolean) = runCatching {
        tokenDao.togglePinToken(tokenId, pinned)
    }

    suspend fun updateTokenHistory(tokenId: Long, code: String, timestamp: Long) = runCatching {
        tokenDao.updateTokenHistory(tokenId, code, timestamp)
    }

    suspend fun updateTokenDetails(tokenId: Long, secret: String, algo: String, digits: Int, timeStep: Long, isSteam: Boolean) = runCatching {
        tokenDao.updateTokenDetails(tokenId, secret, algo, digits, timeStep, isSteam)
    }

    fun getTimeoutMinutes(): Flow<Int> = runCatching {
        settingsRepository.getTimeoutMinutes().catch { emit(5) }
    }.getOrDefault(flowOf(5))

    suspend fun setTimeoutMinutes(minutes: Int) = runCatching {
        settingsRepository.setTimeoutMinutes(minutes)
    }

    fun isBiometricEnabled(): Flow<Boolean> = runCatching {
        settingsRepository.isBiometricEnabled().catch { emit(false) }
    }.getOrDefault(flowOf(false))

    suspend fun setBiometricEnabled(enabled: Boolean) = runCatching {
        settingsRepository.setBiometricEnabled(enabled)
    }

    suspend fun setVaultUnlockedAt(timestamp: Long) = runCatching {
        settingsRepository.setVaultUnlockedAt(timestamp)
    }

    fun getVaultUnlockedAt(): Flow<Long> = runCatching {
        settingsRepository.getVaultUnlockedAt().catch { emit(0L) }
    }.getOrDefault(flowOf(0L))

    fun getThemeMode(): Flow<String> = runCatching {
        settingsRepository.getThemeMode().catch { emit("system") }
    }.getOrDefault(flowOf("system"))

    suspend fun setThemeMode(mode: String) = runCatching {
        settingsRepository.setThemeMode(mode)
    }

    fun isFirstLaunch(): Flow<Boolean> = runCatching {
        settingsRepository.isFirstLaunch().catch { emit(true) }
    }.getOrDefault(flowOf(true))

    suspend fun setFirstLaunchComplete() = runCatching {
        settingsRepository.setFirstLaunchComplete()
    }

    fun shouldHideTokenContent(): Flow<Boolean> = runCatching {
        settingsRepository.shouldHideTokenContent().catch { emit(false) }
    }.getOrDefault(flowOf(false))

    suspend fun setHideTokenContent(hide: Boolean) = runCatching {
        settingsRepository.setHideTokenContent(hide)
    }

    suspend fun getMasterPassHash(): String? = runCatching {
        settingsRepository.getMasterPassHash()
    }.getOrNull()

    suspend fun setMasterPassHash(hash: String) = runCatching {
        settingsRepository.setMasterPassHash(hash)
    }

    fun getSortOrder(): Flow<String> = runCatching {
        settingsRepository.getSortOrder().catch { emit("last_accessed") }
    }.getOrDefault(flowOf("last_accessed"))

    suspend fun setSortOrder(order: String) = runCatching {
        settingsRepository.setSortOrder(order)
    }

    suspend fun setTimeOffsetSeconds(offset: Int) = runCatching {
        settingsRepository.setTimeOffsetSeconds(offset)
    }

    fun getTimeOffsetSeconds(): Flow<Int> = runCatching {
        settingsRepository.getTimeOffsetSeconds().catch { emit(0) }
    }.getOrDefault(flowOf(0))

    suspend fun setVaultLocked(locked: Boolean) = runCatching {
        settingsRepository.setVaultLocked(locked)
    }

    fun isVaultLocked(): Flow<Boolean> = runCatching {
        settingsRepository.isVaultLocked().catch { emit(true) }
    }.getOrDefault(flowOf(true))

    suspend fun setAutoLockBackground(enabled: Boolean) = runCatching {
        settingsRepository.setAutoLockBackground(enabled)
    }

    fun shouldAutoLockBackground(): Flow<Boolean> = runCatching {
        settingsRepository.shouldAutoLockBackground().catch { emit(true) }
    }.getOrDefault(flowOf(true))

    suspend fun setScreenshotPrevention(enabled: Boolean) = runCatching {
        settingsRepository.setScreenshotPrevention(enabled)
    }

    fun shouldScreenshotPrevention(): Flow<Boolean> = runCatching {
        settingsRepository.shouldScreenshotPrevention().catch { emit(true) }
    }.getOrDefault(flowOf(true))

    suspend fun setThemeSchedule(schedule: String) = runCatching {
        settingsRepository.setThemeSchedule(schedule)
    }

    fun getThemeSchedule(): Flow<String> = runCatching {
        settingsRepository.getThemeSchedule().catch { emit("system") }
    }.getOrDefault(flowOf("system"))

    suspend fun setHapticEnabled(enabled: Boolean) = runCatching {
        settingsRepository.setHapticEnabled(enabled)
    }

    fun isHapticEnabled(): Flow<Boolean> = runCatching {
        settingsRepository.isHapticEnabled().catch { emit(true) }
    }.getOrDefault(flowOf(true))

    suspend fun setCurrentVaultId(id: Long) = runCatching {
        settingsRepository.setCurrentVaultId(id)
    }

    fun getCurrentVaultId(): Flow<Long> = runCatching {
        settingsRepository.getCurrentVaultId().catch { emit(1) }
    }.getOrDefault(flowOf(1))

    suspend fun setVaultNames(names: String) = runCatching {
        settingsRepository.setVaultNames(names)
    }

    fun getVaultNames(): Flow<String> = runCatching {
        settingsRepository.getVaultNames().catch { emit("Main Vault") }
    }.getOrDefault(flowOf("Main Vault"))

    suspend fun setShowPreviousCodes(show: Boolean) = runCatching {
        settingsRepository.setShowPreviousCodes(show)
    }

    fun shouldShowPreviousCodes(): Flow<Boolean> = runCatching {
        settingsRepository.shouldShowPreviousCodes().catch { emit(false) }
    }.getOrDefault(flowOf(false))

    suspend fun setShowUsageStats(show: Boolean) = runCatching {
        settingsRepository.setShowUsageStats(show)
    }

    fun shouldShowUsageStats(): Flow<Boolean> = runCatching {
        settingsRepository.shouldShowUsageStats().catch { emit(false) }
    }.getOrDefault(flowOf(false))

    fun parseOtpUri(uri: String): AuthenticatorImporter.ImportResult = runCatching {
        importer.parseOtpUri(uri)
    }.getOrElse { AuthenticatorImporter.ImportResult.Error(it.message ?: "Parse failed") }

    suspend fun importTokens(tokens: List<AuthTokenEntity>): List<Long> = runCatching {
        tokenDao.insertTokens(tokens)
    }.getOrDefault(emptyList())
}
