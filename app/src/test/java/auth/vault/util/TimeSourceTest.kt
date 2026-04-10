package auth.vault.util

import auth.vault.util.TimeSource
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeSourceTest {

    private val timeSource = TimeSource()

    @Test
    fun `currentTimeMillis returns positive value`() {
        val time = timeSource.currentTimeMillis()
        assertTrue(time > 0)
    }

    @Test
    fun `currentSeconds returns positive value`() {
        val seconds = timeSource.currentSeconds()
        assertTrue(seconds > 0)
    }

    @Test
    fun `currentSeconds is approximately currentTimeMillis divided by 1000`() {
        val millis = timeSource.currentTimeMillis()
        val seconds = timeSource.currentSeconds()
        val expectedSeconds = millis / 1000L
        assertTrue(Math.abs(seconds - expectedSeconds) <= 1)
    }
}
