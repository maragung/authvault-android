package auth.vault.lifecycle

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import auth.vault.data.repository.VaultRepository
import auth.vault.util.HapticFeedbackUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultRepository: VaultRepository,
    private val hapticFeedback: HapticFeedbackUtil
) : DefaultLifecycleObserver {

    private var lastBackgroundTime: Long = 0L

    override fun onStop(owner: LifecycleOwner) {
        lastBackgroundTime = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        if (lastBackgroundTime > 0L) {
            val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTime) / 1000L
            if (elapsedSeconds >= 5) {
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        vaultRepository.setVaultLocked(true)
                    }
                }
            }
        }
        lastBackgroundTime = 0L
    }
}
