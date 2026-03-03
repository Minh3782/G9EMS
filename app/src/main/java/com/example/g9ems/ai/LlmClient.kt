package com.example.g9ems.ai

import com.example.g9ems.data.models.FormSession
import kotlinx.coroutines.delay

class LlmClient(
    private val apiBaseUrl: String = "YOUR_NODE_API_BASE_URL_HERE",
    private val apiToken: String = "YOUR_API_TOKEN_HERE"
) {
    // Later: call your Node backend which calls the paid LLM
    suspend fun suggestFieldUpdates(
        transcript: String,
        session: FormSession
    ): LlmSuggestion {
        delay(300) // simulate
        return LlmSuggestion(
            assistantReply = "Got it. I heard: \"$transcript\". Tell me the incident location?",
            // Later: return structured field updates from your LLM
            fieldUpdates = emptyMap()
        )
    }
}

data class LlmSuggestion(
    val assistantReply: String,
    val fieldUpdates: Map<String, String>
)