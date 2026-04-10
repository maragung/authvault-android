package auth.vault.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardManagerUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hapticFeedback: HapticFeedbackUtil
) {

    fun copyToClipboard(text: String, label: String = "AuthVault Token") {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        hapticFeedback.copySuccess()
    }
}

@Singleton
class HapticFeedbackUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun copySuccess() {
        performHaptic(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }

    fun unlockSuccess() {
        performHaptic(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }

    fun lockVibrate() {
        performHaptic(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
    }

    fun deleteVibrate() {
        performHaptic(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
    }

    fun longPressVibrate() {
        performHaptic(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }

    private fun performHaptic(effect: VibrationEffect) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(effect)
            }
        }
    }
}
