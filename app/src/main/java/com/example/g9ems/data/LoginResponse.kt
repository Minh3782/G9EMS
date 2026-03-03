package com.example.g9ems.data

// LoginResponse.kt - What's returned after authentication attempt
data class LoginResponse(
    val success: Boolean = false,
    val message: String = "",
    val user: User? = null,
    val sessionId: String = ""
)