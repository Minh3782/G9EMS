package com.example.g9ems.voice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.Locale
import android.Manifest

class VoiceRecognizer(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _events = Channel<VoiceEvent>(Channel.BUFFERED)
    val events: Flow<VoiceEvent> = _events.receiveAsFlow()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _events.trySend(VoiceEvent.Error("Speech recognition not available on this device"))
            return
        }

        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            _events.trySend(VoiceEvent.Error("Microphone permission required"))
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _events.trySend(VoiceEvent.Listening(true))
                    }

                    override fun onEndOfSpeech() {
                        _events.trySend(VoiceEvent.Listening(false))
                    }

                    override fun onError(error: Int) {
                        _events.trySend(VoiceEvent.Error("Speech error code: $error"))
                        _events.trySend(VoiceEvent.Listening(false))
                    }

                    override fun onResults(results: Bundle?) {
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        if (text.isNotBlank()) _events.trySend(VoiceEvent.Final(text))
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val text = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        if (text.isNotBlank()) _events.trySend(VoiceEvent.Partial(text))
                    }

                    // Unused but required:
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _events.trySend(VoiceEvent.Listening(false))
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

sealed class VoiceEvent {
    data class Listening(val isListening: Boolean) : VoiceEvent()
    data class Partial(val text: String) : VoiceEvent()
    data class Final(val text: String) : VoiceEvent()
    data class Error(val message: String) : VoiceEvent()
}