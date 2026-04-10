package auth.vault.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSource @Inject constructor() {
    fun currentTimeMillis(): Long = System.currentTimeMillis()
    fun currentSeconds(): Long = System.currentTimeMillis() / 1000L
}
