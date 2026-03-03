package com.example.g9ems.data

data class LoginRequest(
    val medicNumber: String = "",  // The ID/Medic Number entered
    val password: String = "",      // The password entered
    val rememberMe: Boolean = false // Optional checkbox
)