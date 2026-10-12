package com.example

import com.example.data.security.CryptoHelper
import com.example.domain.model.AuthMethod
import com.example.domain.model.RelockPolicy
import org.junit.Assert.*
import org.junit.Test

class SecurityUnitTest {

    @Test
    fun pbkdf2_cryptoVerification_worksCorrectly() {
        val pin = "1234"
        val salt = CryptoHelper.generateSalt()
        assertNotNull(salt)
        assertTrue(salt.isNotEmpty())

        val hash = CryptoHelper.hashCredential(pin, salt)
        assertNotNull(hash)
        assertTrue(hash.isNotEmpty())

        // Same PIN must verify successfully
        assertTrue(CryptoHelper.verifyCredential("1234", salt, hash))

        // Incorrect PIN must fail verification
        assertFalse(CryptoHelper.verifyCredential("9999", salt, hash))
        assertFalse(CryptoHelper.verifyCredential("123", salt, hash))
    }

    @Test
    fun authMethod_enum_valuesAreValid() {
        assertEquals("PIN", AuthMethod.PIN.displayName)
        assertEquals("Pattern", AuthMethod.PATTERN.displayName)
        assertEquals(3, AuthMethod.values().size)
    }

    @Test
    fun relockPolicy_seconds_accurate() {
        assertEquals(0, RelockPolicy.IMMEDIATE.seconds)
        assertEquals(30, RelockPolicy.GRACE_30S.seconds)
        assertEquals(60, RelockPolicy.GRACE_1M.seconds)
        assertEquals(300, RelockPolicy.GRACE_5M.seconds)
        assertEquals(-1, RelockPolicy.SCREEN_OFF.seconds)
    }
}
