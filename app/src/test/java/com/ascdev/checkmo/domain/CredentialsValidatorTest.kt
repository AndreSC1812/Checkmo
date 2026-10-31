package com.ascdev.checkmo.domain

import com.ascdev.checkmo.domain.validation.CredentialsValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialsValidatorTest {

    @Test
    fun `accepts well formed emails and trims spaces`() {
        assertTrue(CredentialsValidator.isValidEmail("ana.garcia@acme.es"))
        assertTrue(CredentialsValidator.isValidEmail("  ana@acme.es  "))
    }

    @Test
    fun `rejects malformed emails`() {
        listOf("", "ana", "ana@", "ana@acme", "@acme.es", "ana acme@acme.es").forEach {
            assertFalse(it, CredentialsValidator.isValidEmail(it))
        }
    }

    @Test
    fun `password needs at least six characters`() {
        assertFalse(CredentialsValidator.isValidPassword("12345"))
        assertTrue(CredentialsValidator.isValidPassword("123456"))
    }
}
