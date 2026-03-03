package com.example.myapplication.data

// AuthState.kt - For UI state management (if using Compose)
data class AuthState(
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)