package com.example.myapplication.data
// User.kt
data class User(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val medicNumber: String = "",
    val email: String = "",
    val passwordHash: String = "", // Store only hash, never plain text
    val role: UserRole = UserRole.PARAMEDIC,
    val isActive: Boolean = true,
    val documentType: String = "user"
)

enum class UserRole {
    PARAMEDIC, MANAGER, ADMIN
}