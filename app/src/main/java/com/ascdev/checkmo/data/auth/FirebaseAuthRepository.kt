package com.ascdev.checkmo.data.auth

import android.util.Log
import com.ascdev.checkmo.core.di.IoDispatcher
import com.ascdev.checkmo.data.remote.FirestoreSchema.Companies
import com.ascdev.checkmo.data.remote.FirestoreSchema.InviteCodes
import com.ascdev.checkmo.data.remote.FirestoreSchema.Users
import com.ascdev.checkmo.data.session.SessionDataSource
import com.ascdev.checkmo.domain.model.AuthError
import com.ascdev.checkmo.domain.model.AuthException
import com.ascdev.checkmo.domain.model.Company
import com.ascdev.checkmo.domain.model.SignUpRequest
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sessionDataSource: SessionDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    override val session: Flow<User?> = sessionDataSource.user.map { user ->
        // Firebase Auth owns the credentials; the cached profile is only valid while it has a user.
        user?.takeIf { auth.currentUser?.uid == it.id }
    }

    override suspend fun signIn(email: String, password: String): Result<User> = runAuthCatching {
        val uid = auth.signInWithEmailAndPassword(email.trim(), password).await().user?.uid
            ?: throw AuthException(AuthError.UNKNOWN)
        val user = try {
            fetchUser(uid)
        } catch (e: Exception) {
            auth.signOut()
            throw e
        }
        sessionDataSource.save(user)
        user
    }

    override suspend fun signUp(request: SignUpRequest): Result<User> = runAuthCatching {
        // Validate the invite code before creating the account so a typo doesn't leave an orphan user.
        val employeeCompanyId = (request as? SignUpRequest.Employee)?.let { findCompanyId(it.inviteCode) }

        val uid = auth.createUserWithEmailAndPassword(request.email.trim(), request.password).await().user?.uid
            ?: throw AuthException(AuthError.UNKNOWN)

        val user = try {
            when (request) {
                is SignUpRequest.Employee -> createEmployee(uid, request, checkNotNull(employeeCompanyId))
                is SignUpRequest.Company -> createCompany(uid, request)
            }
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            throw e
        }
        sessionDataSource.save(user)
        user
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runAuthCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }.map { }

    override suspend fun signOut() {
        auth.signOut()
        sessionDataSource.clear()
    }

    private suspend fun findCompanyId(inviteCode: String): String {
        val code = InviteCodeGenerator.normalize(inviteCode)
        val doc = firestore.collection(InviteCodes.COLLECTION).document(code).get().await()
        return doc.getString(InviteCodes.COMPANY_ID) ?: throw AuthException(AuthError.INVALID_INVITE_CODE)
    }

    private suspend fun createEmployee(uid: String, request: SignUpRequest.Employee, companyId: String): User {
        firestore.collection(Users.COLLECTION).document(uid).set(
            mapOf(
                Users.NAME to request.name.trim(),
                Users.EMAIL to request.email.trim(),
                Users.ROLE to UserRole.EMPLOYEE.name,
                Users.COMPANY_ID to companyId,
                Users.CREATED_AT to FieldValue.serverTimestamp(),
            ),
        ).await()
        return User(
            id = uid,
            name = request.name.trim(),
            email = request.email.trim(),
            role = UserRole.EMPLOYEE,
            company = fetchCompany(companyId),
        )
    }

    private suspend fun createCompany(uid: String, request: SignUpRequest.Company): User {
        val companyRef = firestore.collection(Companies.COLLECTION).document()
        val company = Company(
            id = companyRef.id,
            name = request.companyName.trim(),
            inviteCode = InviteCodeGenerator.generate(),
        )
        firestore.batch().apply {
            set(
                companyRef,
                mapOf(
                    Companies.NAME to company.name,
                    Companies.OWNER_ID to uid,
                    Companies.INVITE_CODE to company.inviteCode,
                    Companies.CREATED_AT to FieldValue.serverTimestamp(),
                ),
            )
            set(
                firestore.collection(InviteCodes.COLLECTION).document(company.inviteCode),
                mapOf(InviteCodes.COMPANY_ID to company.id),
            )
            set(
                firestore.collection(Users.COLLECTION).document(uid),
                mapOf(
                    Users.NAME to request.name.trim(),
                    Users.EMAIL to request.email.trim(),
                    Users.ROLE to UserRole.COMPANY.name,
                    Users.COMPANY_ID to company.id,
                    Users.CREATED_AT to FieldValue.serverTimestamp(),
                ),
            )
        }.commit().await()
        return User(
            id = uid,
            name = request.name.trim(),
            email = request.email.trim(),
            role = UserRole.COMPANY,
            company = company,
        )
    }

    private suspend fun fetchUser(uid: String): User {
        val doc = firestore.collection(Users.COLLECTION).document(uid).get().await()
        val role = doc.getString(Users.ROLE)?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
        val companyId = doc.getString(Users.COMPANY_ID)
        if (!doc.exists() || role == null || companyId == null) throw AuthException(AuthError.PROFILE_NOT_FOUND)
        return User(
            id = uid,
            name = doc.getString(Users.NAME).orEmpty(),
            email = doc.getString(Users.EMAIL).orEmpty(),
            role = role,
            company = fetchCompany(companyId),
        )
    }

    private suspend fun fetchCompany(companyId: String): Company {
        val doc = firestore.collection(Companies.COLLECTION).document(companyId).get().await()
        if (!doc.exists()) throw AuthException(AuthError.PROFILE_NOT_FOUND)
        return Company(
            id = companyId,
            name = doc.getString(Companies.NAME).orEmpty(),
            inviteCode = doc.getString(Companies.INVITE_CODE).orEmpty(),
        )
    }

    private suspend fun <T> runAuthCatching(block: suspend () -> T): Result<T> = withContext(ioDispatcher) {
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Auth operation failed", e)
            Result.failure(AuthException(e.toDomainError(), e))
        }
    }

    private fun Exception.toDomainError(): AuthError = when (this) {
        is AuthException -> error
        // Weak password extends invalid credentials, so it must be checked first.
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
        is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
        is FirebaseNetworkException -> AuthError.NETWORK
        is FirebaseFirestoreException if code == FirebaseFirestoreException.Code.UNAVAILABLE -> AuthError.NETWORK
        else -> AuthError.UNKNOWN
    }

    private companion object {
        const val TAG = "FirebaseAuthRepository"
    }
}
