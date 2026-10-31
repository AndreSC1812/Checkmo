package com.ascdev.checkmo.domain.repository

import com.ascdev.checkmo.domain.model.SignUpRequest
import com.ascdev.checkmo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /** Signed-in user cached locally, or `null` when there is no session. */
    val session: Flow<User?>

    suspend fun signIn(email: String, password: String): Result<User>

    suspend fun signUp(request: SignUpRequest): Result<User>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun signOut()
}
