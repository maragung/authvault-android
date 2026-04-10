package auth.vault.domain.usecase

import auth.vault.data.repository.VaultRepository
import auth.vault.util.TimeSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultLockTimeout @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val timeSource: TimeSource
) {

    suspend fun isVaultTimedOut(): Boolean = runCatching {
        val timeoutMinutes = vaultRepository.getTimeoutMinutes().first()
        val lastUnlockedAt = vaultRepository.getVaultUnlockedAt().first()
        if (lastUnlockedAt == 0L) return@runCatching true
        val elapsedMinutes = (timeSource.currentTimeMillis() - lastUnlockedAt) / 60000L
        elapsedMinutes >= timeoutMinutes
    }.getOrDefault(true)

    suspend fun recordUnlock() = runCatching {
        vaultRepository.setVaultUnlockedAt(timeSource.currentTimeMillis())
    }

    suspend fun getTimeoutMinutes(): Int = runCatching {
        vaultRepository.getTimeoutMinutes().first()
    }.getOrDefault(5)

    suspend fun updateTimeoutMinutes(minutes: Int) = runCatching {
        val clampedMinutes = minutes.coerceIn(1, 120)
        vaultRepository.setTimeoutMinutes(clampedMinutes)
    }
}
