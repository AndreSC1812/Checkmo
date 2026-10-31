package com.ascdev.checkmo.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ascdev.checkmo.domain.model.Company
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caches the signed-in user's profile so the app can route by role and work offline
 * without hitting Firestore on every launch.
 */
@Singleton
class SessionDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val user: Flow<User?> = dataStore.data.map { prefs -> prefs.toUser() }

    suspend fun save(user: User) {
        dataStore.edit { prefs ->
            prefs[Keys.UserId] = user.id
            prefs[Keys.Name] = user.name
            prefs[Keys.Email] = user.email
            prefs[Keys.Role] = user.role.name
            prefs[Keys.CompanyId] = user.company.id
            prefs[Keys.CompanyName] = user.company.name
            prefs[Keys.InviteCode] = user.company.inviteCode
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Preferences.toUser(): User? {
        val id = this[Keys.UserId] ?: return null
        val role = this[Keys.Role]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() } ?: return null
        return User(
            id = id,
            name = this[Keys.Name].orEmpty(),
            email = this[Keys.Email].orEmpty(),
            role = role,
            company = Company(
                id = this[Keys.CompanyId].orEmpty(),
                name = this[Keys.CompanyName].orEmpty(),
                inviteCode = this[Keys.InviteCode].orEmpty(),
            ),
        )
    }

    private object Keys {
        val UserId = stringPreferencesKey("user_id")
        val Name = stringPreferencesKey("user_name")
        val Email = stringPreferencesKey("user_email")
        val Role = stringPreferencesKey("user_role")
        val CompanyId = stringPreferencesKey("company_id")
        val CompanyName = stringPreferencesKey("company_name")
        val InviteCode = stringPreferencesKey("company_invite_code")
    }
}
