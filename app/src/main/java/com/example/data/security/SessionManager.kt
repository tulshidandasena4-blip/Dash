package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUser(
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dash_auth_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(loadUser())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private fun loadUser(): AuthUser? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null
        val id = prefs.getLong("user_id", -1)
        val name = prefs.getString("user_name", "") ?: ""
        val email = prefs.getString("user_email", "") ?: ""
        val role = prefs.getString("user_role", "Operator") ?: "Operator"
        return if (id != -1L && email.isNotBlank()) {
            AuthUser(id, name, email, role)
        } else {
            null
        }
    }

    fun saveSession(user: AuthUser) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putLong("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_role", user.role)
            .apply()
        _currentUser.value = user
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null
}
