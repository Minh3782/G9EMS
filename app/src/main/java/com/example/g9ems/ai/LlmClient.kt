package com.example.g9ems.ai

import com.example.g9ems.BuildConfig
import com.example.g9ems.api.OpenRouterClient
import com.example.g9ems.data.models.FormSession
import com.example.g9ems.data.models.FormType
import kotlinx.coroutines.delay

class LlmClient {

    private val openRouterClient = OpenRouterClient(BuildConfig.OPENROUTER_API_KEY)

    suspend fun suggestFieldUpdates(
        transcript: String,
        session: FormSession
    ): LlmSuggestion {

        // Use real API if key exists
        if (BuildConfig.OPENROUTER_API_KEY.isNotEmpty()) {
            return openRouterClient.suggestFieldUpdates(transcript, session)
        }

        // Fallback to test data if no API key
        delay(300)
        return when (session.formType) {
            FormType.FORM2_TEDDY -> {
                LlmSuggestion(
                    assistantReply = "I've filled the Teddy Bear form based on what you said.",
                    fieldUpdates = mapOf(
                        "recipientAge" to "5",
                        "recipientGender" to "Female",
                        "recipientType" to "Patient"
                    )
                )
            }
            else -> {
                LlmSuggestion(
                    assistantReply = "I heard: \"$transcript\"",
                    fieldUpdates = emptyMap()
                )
            }
        }
    }
}

data class LlmSuggestion(
    val assistantReply: String,
    val fieldUpdates: Map<String, Any?>
)