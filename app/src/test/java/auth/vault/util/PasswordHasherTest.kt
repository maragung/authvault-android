package auth.vault.util

import auth.vault.domain.usecase.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    private val passwordHasher = PasswordHasher()

    @Test
    fun `hashPassword returns 64 character hex string for SHA-256`() {
        val hash = passwordHasher.hashPassword("testpassword")
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `same password produces same hash`() {
        val hash1 = passwordHasher.hashPassword("mypassword")
        val hash2 = passwordHasher.hashPassword("mypassword")
        assertEquals(hash1, hash2)
    }

    @Test
    fun `different passwords produce different hashes`() {
        val hash1 = passwordHasher.hashPassword("password1")
        val hash2 = passwordHasher.hashPassword("password2")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `verifyPassword returns true for correct password`() {
        val password = "correctpassword"
        val hash = passwordHasher.hashPassword(password)
        assertTrue(passwordHasher.verifyPassword(password, hash))
    }

    @Test
    fun `verifyPassword returns false for incorrect password`() {
        val hash = passwordHasher.hashPassword("correctpassword")
        assertTrue(!passwordHasher.verifyPassword("wrongpassword", hash))
    }

    @Test
    fun `hashPassword handles empty string`() {
        val hash = passwordHasher.hashPassword("")
        assertEquals(64, hash.length)
    }

    @Test
    fun `hashPassword handles special characters`() {
        val hash = passwordHasher.hashPassword("p@ssw0rd!#$%^&*()")
        assertEquals(64, hash.length)
    }
}
