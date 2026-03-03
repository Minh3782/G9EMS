package com.example.myapplication.data

// LoginResponse.kt - What's returned after authentication attempt
data class LoginResponse(
    val success: Boolean = false,
    val message: String = "",
    val user: User? = null,
    val sessionId: String = ""
)