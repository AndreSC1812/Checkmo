package com.ascdev.checkmo.testing

import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.domain.model.AuthException
import com.ascdev.checkmo.domain.model.Company
import com.ascdev.checkmo.domain.model.SignUpRequest
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository : AuthRepository {

    private val _session = MutableStateFlow<User?>(null)
    override val session: StateFlow<User?> = _session

    var nextError: AuthError? = null
    var lastSignUpRequest: SignUpRequest? = null
        private set
    var passwordResetEmail: String? = null
        private set

    override suspend fun signIn(email: String, password: String): Result<User> =
        result { testUser(email = email) }

    override suspend fun signUp(request: SignUpRequest): Result<User> {
        lastSignUpRequest = request
        return result { testUser(email = request.email) }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        nextError?.let { return Result.failure(AuthException(it)) }
        passwordResetEmail = email
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        _session.value = null
    }

    private fun result(user: () -> User): Result<User> {
        nextError?.let { return Result.failure(AuthException(it)) }
        return Result.success(user().also { _session.value = it })
    }

    private fun testUser(email: String) = User(
        id = "uid",
        name = "Ana",
        email = email,
        role = UserRole.EMPLOYEE,
        company = Company(id = "c1", name = "Acme", inviteCode = "K7P2QX"),
    )
}
