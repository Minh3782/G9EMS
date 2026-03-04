package com.example.g9ems.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g9ems.ai.FormUpdateEngine
import com.example.g9ems.ai.LlmClient
import com.example.g9ems.data.models.ChatMessage
import com.example.g9ems.data.models.FormSession
import com.example.g9ems.data.models.FormType
import com.example.g9ems.data.models.Role
import com.example.g9ems.data.repository.FormRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FormViewModel(
    private val repo: FormRepository = FormRepository(),
    private val llm: LlmClient = LlmClient()
) : ViewModel() {

    private val _session = MutableStateFlow(repo.createBlankSession(FormType.FORM2_TEDDY))
    val session: StateFlow<FormSession> = _session

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(role = Role.SYSTEM, text = "Push-to-talk and I’ll fill the form as you speak."))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun setListening(listening: Boolean) {
        _isListening.value = listening
        if (!listening) _partialTranscript.value = ""
    }

    fun setPartial(text: String) {
        _partialTranscript.value = text
    }

    fun onFinalTranscript(text: String) {
        if (text.isBlank()) return

        _messages.value = _messages.value +
                ChatMessage(role = Role.USER, text = text)

        viewModelScope.launch {
            val suggestion = llm.suggestFieldUpdates(text, _session.value)

            _session.value = FormUpdateEngine.applyUpdates(
                session = _session.value,
                updates = suggestion.fieldUpdates
            )

            repo.saveSession(_session.value)
        }
    }

    private fun applyFieldUpdates(
        session: FormSession,
        updates: Map<String, Any?>
    ): FormSession {
        val updatedFields = session.fields.map { field ->
            val newValue = updates[field.key]
            if (newValue != null) {
                Log.d("EMS-UPDATE", "  ↳ ${field.key}: ${field.value} → $newValue")
                field.copy(value = newValue.toString())
            } else {
                field
            }
        }
        return session.copy(fields = updatedFields)
    }

    fun setFormType(type: FormType) {
        _session.value = repo.createBlankSession(type)
        _messages.value = listOf(ChatMessage(role = Role.SYSTEM, text = "Switched to $type. Push-to-talk to begin."))
    }

    fun setFieldValue(key: String, value: String) {
        val updatedFields = _session.value.fields.map { field ->
            if (field.key == key) field.copy(value = value) else field
        }
        _session.value = _session.value.copy(fields = updatedFields)
    }

    fun clearForm() {
        _session.value = repo.createBlankSession(_session.value.formType)
        _messages.value = listOf(ChatMessage(role = Role.SYSTEM, text = "Form cleared. Push-to-talk to begin."))
    }

    fun submitForm() {
        viewModelScope.launch {
            repo.saveSession(_session.value)
            // Add any submission logic here (e.g. POST to server)
        }
    }
}