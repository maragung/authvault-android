package auth.vault.util

import auth.vault.domain.usecase.RecoveryCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCodeGeneratorTest {

    private val generator = RecoveryCodeGenerator()

    @Test
    fun `generateRecoveryCodeSet returns 10 codes by default`() {
        val result = generator.generateRecoveryCodeSet("testhash", 10)
        assertEquals(10, result.codes.size)
        assertNotNull(result.codeSetId)
        assertTrue(result.generatedAt > 0)
    }

    @Test
    fun `generateRecoveryCodeSet returns custom count`() {
        val result = generator.generateRecoveryCodeSet("testhash", 5)
        assertEquals(5, result.codes.size)
    }

    @Test
    fun `each code has XXXXX-XXXXX-XXXXX format`() {
        val result = generator.generateRecoveryCodeSet("testhash", 10)
        result.codes.forEach { code ->
            val parts = code.split("-")
            assertEquals(3, parts.size)
            parts.forEach { part ->
                assertEquals(5, part.length)
                assertTrue(part.all { it.isLetterOrDigit() })
            }
        }
    }

    @Test
    fun `codes are unique within a set`() {
        val result = generator.generateRecoveryCodeSet("testhash", 10)
        assertEquals(result.codes.size, result.codes.toSet().size)
    }

    @Test
    fun `different master hash produces different codes`() {
        val result1 = generator.generateRecoveryCodeSet("hash1", 10)
        val result2 = generator.generateRecoveryCodeSet("hash2", 10)
        assertNotEquals(result1.codes, result2.codes)
    }

    @Test
    fun `generateRandomCode returns valid format`() {
        val code = generator.generateRandomCode()
        val parts = code.split("-")
        assertEquals(4, parts.size)
        parts.forEach { part ->
            assertEquals(5, part.length)
        }
    }
}
