package com.ascdev.checkmo.feature.auth

import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.domain.model.SignUpRequest
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.feature.auth.signup.SignUpViewModel
import com.ascdev.checkmo.testing.FakeAuthRepository
import com.ascdev.checkmo.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        viewModel = SignUpViewModel(repository)
    }

    private fun fillCommonFields(password: String = "secret1") {
        viewModel.onNameChange("Ana García")
        viewModel.onEmailChange("ana@acme.es")
        viewModel.onPasswordChange(password)
    }

    @Test
    fun `employee sign up requires invite code`() {
        fillCommonFields()

        viewModel.signUp()

        assertEquals(AuthError.EMPTY_FIELDS, viewModel.uiState.value.error)
        assertNull(repository.lastSignUpRequest)
    }

    @Test
    fun `short password shows weak password error`() {
        fillCommonFields(password = "123")
        viewModel.onInviteCodeChange("K7P2QX")

        viewModel.signUp()

        assertEquals(AuthError.WEAK_PASSWORD, viewModel.uiState.value.error)
    }

    @Test
    fun `invite code is uppercased while typing`() {
        viewModel.onInviteCodeChange("k7p2qx")

        assertEquals("K7P2QX", viewModel.uiState.value.inviteCode)
    }

    @Test
    fun `employee sign up sends employee request`() {
        fillCommonFields()
        viewModel.onInviteCodeChange("K7P2QX")

        viewModel.signUp()

        assertEquals(
            SignUpRequest.Employee("Ana García", "ana@acme.es", "secret1", "K7P2QX"),
            repository.lastSignUpRequest,
        )
    }

    @Test
    fun `company sign up sends company request and ignores invite code`() {
        viewModel.onRoleChange(UserRole.COMPANY)
        fillCommonFields()
        viewModel.onCompanyNameChange("Acme S.L.")

        viewModel.signUp()

        assertEquals(
            SignUpRequest.Company("Ana García", "ana@acme.es", "secret1", "Acme S.L."),
            repository.lastSignUpRequest,
        )
    }

    @Test
    fun `repository error is shown`() {
        repository.nextError = AuthError.INVALID_INVITE_CODE
        fillCommonFields()
        viewModel.onInviteCodeChange("NOPE00")

        viewModel.signUp()

        assertEquals(AuthError.INVALID_INVITE_CODE, viewModel.uiState.value.error)
    }
}
