package com.example.data.repository

import com.example.data.dao.UserDao
import com.example.data.model.UserEntity
import com.example.data.security.AuthUser
import com.example.data.security.PasswordHasher
import com.example.data.security.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val currentUser: StateFlow<AuthUser?> = sessionManager.currentUser

    suspend fun seedDefaultAdminIfEmpty() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hashPassword("Dash@12345", salt)
            val adminUser = UserEntity(
                name = "Dash Testing Admin",
                email = "admin@dashpollution.com",
                passwordHash = hash,
                salt = salt,
                role = "Admin"
            )
            userDao.insertUser(adminUser)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String = "Operator"
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanName.length < 2) {
            return@withContext AuthResult.Error("Name must be at least 2 characters")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address")
        }
        if (cleanPassword.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters")
        }

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext AuthResult.Error("An account with this email already exists")
        }

        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(cleanPassword, salt)
        val entity = UserEntity(
            name = cleanName,
            email = cleanEmail,
            passwordHash = hash,
            salt = salt,
            role = role
        )
        val id = userDao.insertUser(entity)
        val authUser = AuthUser(id = id, name = cleanName, email = cleanEmail, role = role)
        sessionManager.saveSession(authUser)
        AuthResult.Success(authUser)
    }

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank()) {
            return@withContext AuthResult.Error("Please enter your email")
        }
        if (cleanPassword.isBlank()) {
            return@withContext AuthResult.Error("Please enter your password")
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext AuthResult.Error("Account not found. Please register first.")

        val isValid = PasswordHasher.verifyPassword(cleanPassword, user.salt, user.passwordHash)
        if (!isValid) {
            return@withContext AuthResult.Error("Incorrect password. Please try again.")
        }

        val authUser = AuthUser(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role
        )
        sessionManager.saveSession(authUser)
        AuthResult.Success(authUser)
    }

    fun saveDirectSession(user: AuthUser) {
        sessionManager.saveSession(user)
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
