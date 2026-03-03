package com.example.g9ems.data.models

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val role: Role,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis()
)

enum class Role { USER, ASSISTANT, SYSTEM }