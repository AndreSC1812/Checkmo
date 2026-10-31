package com.ascdev.checkmo.feature.auth

import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.feature.auth.login.LoginViewModel
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        viewModel = LoginViewModel(repository)
    }

    @Test
    fun `sign in with empty fields shows empty fields error`() {
        viewModel.signIn()

        assertEquals(AuthError.EMPTY_FIELDS, viewModel.uiState.value.error)
        assertNull(repository.session.value)
    }

    @Test
    fun `sign in with malformed email shows invalid email error`() {
        viewModel.onEmailChange("ana@")
        viewModel.onPasswordChange("secret1")

        viewModel.signIn()

        assertEquals(AuthError.INVALID_EMAIL, viewModel.uiState.value.error)
    }

    @Test
    fun `sign in success creates a session`() {
        viewModel.onEmailChange("ana@acme.es")
        viewModel.onPasswordChange("secret1")

        viewModel.signIn()

        assertEquals("ana@acme.es", repository.session.value?.email)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `sign in failure exposes repository error and stops loading`() {
        repository.nextError = AuthError.INVALID_CREDENTIALS
        viewModel.onEmailChange("ana@acme.es")
        viewModel.onPasswordChange("wrong-password")

        viewModel.signIn()

        assertEquals(AuthError.INVALID_CREDENTIALS, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `editing a field clears the previous error`() {
        viewModel.signIn()

        viewModel.onEmailChange("a")

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `password reset sends email when it is valid`() {
        viewModel.onEmailChange("ana@acme.es")

        viewModel.sendPasswordReset()

        assertEquals("ana@acme.es", repository.passwordResetEmail)
        assertTrue(viewModel.uiState.value.passwordResetSent)
    }

    @Test
    fun `password reset without valid email shows invalid email error`() {
        viewModel.sendPasswordReset()

        assertEquals(AuthError.INVALID_EMAIL, viewModel.uiState.value.error)
        assertNull(repository.passwordResetEmail)
    }
}
