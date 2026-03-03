package com.example.myapplication.data

// LoginSession.kt - Tracks active sessions (optional)
data class LoginSession(
    val sessionId: String = "",
    val userId: String = "",
    val medicNumber: String = "",
    val loginTime: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val documentType: String = "login_session"
)