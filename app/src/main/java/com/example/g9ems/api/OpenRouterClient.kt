package com.example.g9ems.api

import android.util.Log
import com.example.g9ems.ai.LlmSuggestion
import com.example.g9ems.data.models.FormSession
import com.example.g9ems.data.models.FormType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OpenRouterClient(private val apiKey: String) {

    private val api = OpenRouterApi.create(apiKey)

    suspend fun suggestFieldUpdates(
        transcript: String,
        session: FormSession
    ): LlmSuggestion = withContext(Dispatchers.IO) {

        // Build context about current form fields
        val currentFields = session.fields.joinToString("\n") { field ->
            "${field.key}: ${field.value ?: "empty"}"
        }

        // Create prompt for the AI
        val prompt = """
            You are an EMS AI assistant helping paramedics fill forms.
            
            Current form type: ${session.formType}
            
            Current field values:
            $currentFields
            
            The paramedic just said: "$transcript"
            
            Extract information from their speech and map it to the appropriate fields.
            
            Return ONLY a JSON object with field names as keys and extracted values as strings.
            Example: {"patientAge": "45", "patientGender": "Male"}
            
            If no information can be extracted, return an empty JSON object: {}
        """.trimIndent()

        try {
            val request = ChatCompletionRequest(
                messages = listOf(
                    ChatMessage("system", "You are an EMS form-filling assistant."),
                    ChatMessage("user", prompt)
                )
            )

            val response = api.getChatCompletion("Bearer $apiKey", request)

            val content = response.choices.firstOrNull()?.message?.content ?: "{}"

            // Parse JSON response
            val json = JSONObject(content)
            val fieldUpdates = mutableMapOf<String, Any?>()

            json.keys().forEach { key ->
                fieldUpdates[key] = json.getString(key)
            }

            Log.d("EMS-OPENROUTER", "✅ Extracted: $fieldUpdates")

            LlmSuggestion(
                assistantReply = "I've updated the form based on what you said.",
                fieldUpdates = fieldUpdates
            )

        } catch (e: Exception) {
            Log.e("EMS-OPENROUTER", "❌ API Error: ${e.message}", e)

            LlmSuggestion(
                assistantReply = "Sorry, I couldn't process that. Please try again.",
                fieldUpdates = emptyMap()
            )
        }
    }
}