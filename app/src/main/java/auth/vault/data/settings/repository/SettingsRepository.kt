package auth.vault.data.settings.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import auth.vault.util.DispatchProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchProvider: DispatchProvider,
    encryptionKeyProvider: auth.vault.data.security.EncryptionKeyProvider
) {

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "authvault_encrypted_prefs",
            encryptionKeyProvider.masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object Keys {
        const val TIMEOUT_MINUTES = "timeout_minutes"
        const val BIOMETRIC_ENABLED = "biometric_enabled"
        const val VAULT_UNLOCKED_AT = "vault_unlocked_at"
        const val THEME_MODE = "theme_mode"
        const val MASTER_PASS_HASH = "master_pass_hash"
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val HIDE_TOKEN_CONTENT = "hide_token_content"
        const val SORT_ORDER = "sort_order"
        const val TIME_OFFSET_SECONDS = "time_offset_seconds"
        const val VAULT_LOCKED = "vault_locked"
        const val AUTO_LOCK_BACKGROUND = "auto_lock_background"
        const val SCREENSHOT_PREVENTION = "screenshot_prevention"
        const val THEME_SCHEDULE = "theme_schedule"
        const val HAPTIC_ENABLED = "haptic_enabled"
        const val CURRENT_VAULT_ID = "current_vault_id"
        const val VAULT_NAMES = "vault_names"
        const val SHOW_PREVIOUS_CODES = "show_previous_codes"
        const val SHOW_USAGE_STATS = "show_usage_stats"
    }

    private val prefsFlow = MutableStateFlow(Unit)

    init {
        encryptedPrefs.registerOnSharedPreferenceChangeListener { _, _ ->
            prefsFlow.value = Unit
        }
    }

    suspend fun setTimeoutMinutes(minutes: Int) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putInt(TIMEOUT_MINUTES, minutes).apply()
    }

    fun getTimeoutMinutes(): Flow<Int> = prefsFlowWithFallback(TIMEOUT_MINUTES, 5) {
        encryptedPrefs.getInt(TIMEOUT_MINUTES, 5)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Flow<Boolean> = prefsFlowWithFallback(BIOMETRIC_ENABLED, false) {
        encryptedPrefs.getBoolean(BIOMETRIC_ENABLED, false)
    }

    suspend fun setVaultUnlockedAt(timestamp: Long) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putLong(VAULT_UNLOCKED_AT, timestamp).apply()
    }

    fun getVaultUnlockedAt(): Flow<Long> = prefsFlowWithFallback(VAULT_UNLOCKED_AT, 0L) {
        encryptedPrefs.getLong(VAULT_UNLOCKED_AT, 0L)
    }

    suspend fun setThemeMode(mode: String) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putString(THEME_MODE, mode).apply()
    }

    fun getThemeMode(): Flow<String> = prefsFlowWithFallback(THEME_MODE, "system") {
        encryptedPrefs.getString(THEME_MODE, "system") ?: "system"
    }

    suspend fun setMasterPassHash(hash: String) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putString(MASTER_PASS_HASH, hash).apply()
    }

    suspend fun getMasterPassHash(): String? = withContext(dispatchProvider.io) {
        encryptedPrefs.getString(MASTER_PASS_HASH, null)
    }

    suspend fun setFirstLaunchComplete() = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(IS_FIRST_LAUNCH, false).apply()
    }

    fun isFirstLaunch(): Flow<Boolean> = prefsFlowWithFallback(IS_FIRST_LAUNCH, true) {
        encryptedPrefs.getBoolean(IS_FIRST_LAUNCH, true)
    }

    suspend fun setHideTokenContent(hide: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(HIDE_TOKEN_CONTENT, hide).apply()
    }

    fun shouldHideTokenContent(): Flow<Boolean> = prefsFlowWithFallback(HIDE_TOKEN_CONTENT, false) {
        encryptedPrefs.getBoolean(HIDE_TOKEN_CONTENT, false)
    }

    suspend fun setSortOrder(order: String) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putString(SORT_ORDER, order).apply()
    }

    fun getSortOrder(): Flow<String> = prefsFlowWithFallback(SORT_ORDER, "last_accessed") {
        encryptedPrefs.getString(SORT_ORDER, "last_accessed") ?: "last_accessed"
    }

    suspend fun setTimeOffsetSeconds(offset: Int) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putInt(TIME_OFFSET_SECONDS, offset).apply()
    }

    fun getTimeOffsetSeconds(): Flow<Int> = prefsFlowWithFallback(TIME_OFFSET_SECONDS, 0) {
        encryptedPrefs.getInt(TIME_OFFSET_SECONDS, 0)
    }

    suspend fun setVaultLocked(locked: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(VAULT_LOCKED, locked).apply()
    }

    fun isVaultLocked(): Flow<Boolean> = prefsFlowWithFallback(VAULT_LOCKED, true) {
        encryptedPrefs.getBoolean(VAULT_LOCKED, true)
    }

    suspend fun setAutoLockBackground(enabled: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(AUTO_LOCK_BACKGROUND, enabled).apply()
    }

    fun shouldAutoLockBackground(): Flow<Boolean> = prefsFlowWithFallback(AUTO_LOCK_BACKGROUND, true) {
        encryptedPrefs.getBoolean(AUTO_LOCK_BACKGROUND, true)
    }

    suspend fun setScreenshotPrevention(enabled: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(SCREENSHOT_PREVENTION, enabled).apply()
    }

    fun shouldScreenshotPrevention(): Flow<Boolean> = prefsFlowWithFallback(SCREENSHOT_PREVENTION, true) {
        encryptedPrefs.getBoolean(SCREENSHOT_PREVENTION, true)
    }

    suspend fun setThemeSchedule(schedule: String) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putString(THEME_SCHEDULE, schedule).apply()
    }

    fun getThemeSchedule(): Flow<String> = prefsFlowWithFallback(THEME_SCHEDULE, "system") {
        encryptedPrefs.getString(THEME_SCHEDULE, "system") ?: "system"
    }

    suspend fun setHapticEnabled(enabled: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(HAPTIC_ENABLED, enabled).apply()
    }

    fun isHapticEnabled(): Flow<Boolean> = prefsFlowWithFallback(HAPTIC_ENABLED, true) {
        encryptedPrefs.getBoolean(HAPTIC_ENABLED, true)
    }

    suspend fun setCurrentVaultId(id: Long) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putLong(CURRENT_VAULT_ID, id).apply()
    }

    fun getCurrentVaultId(): Flow<Long> = prefsFlowWithFallback(CURRENT_VAULT_ID, 1) {
        encryptedPrefs.getLong(CURRENT_VAULT_ID, 1)
    }

    suspend fun setVaultNames(names: String) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putString(VAULT_NAMES, names).apply()
    }

    fun getVaultNames(): Flow<String> = prefsFlowWithFallback(VAULT_NAMES, "Main Vault") {
        encryptedPrefs.getString(VAULT_NAMES, "Main Vault") ?: "Main Vault"
    }

    suspend fun setShowPreviousCodes(show: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(SHOW_PREVIOUS_CODES, show).apply()
    }

    fun shouldShowPreviousCodes(): Flow<Boolean> = prefsFlowWithFallback(SHOW_PREVIOUS_CODES, false) {
        encryptedPrefs.getBoolean(SHOW_PREVIOUS_CODES, false)
    }

    suspend fun setShowUsageStats(show: Boolean) = withContext(dispatchProvider.io) {
        encryptedPrefs.edit().putBoolean(SHOW_USAGE_STATS, show).apply()
    }

    fun shouldShowUsageStats(): Flow<Boolean> = prefsFlowWithFallback(SHOW_USAGE_STATS, false) {
        encryptedPrefs.getBoolean(SHOW_USAGE_STATS, false)
    }

    private fun <T> prefsFlowWithFallback(key: String, default: T, getter: () -> T): Flow<T> {
        return flow {
            emit(getter())
            prefsFlow.collect {
                emit(getter())
            }
        }
    }
}
