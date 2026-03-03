package com.example.g9ems

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.g9ems.ui.screens.PushToTalkScreen
import com.example.g9ems.ui.theme.G9EMSTheme
import com.example.g9ems.voice.VoiceEvent
import com.example.g9ems.voice.VoiceRecognizer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var voice: VoiceRecognizer
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voice = VoiceRecognizer(this)

        setContent {
            G9EMSTheme {
                val vm = viewModel<com.example.g9ems.viewmodel.FormViewModel>()

                // Collect voice events and push into VM
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    voice.events.collect { e ->
                        when (e) {
                            is VoiceEvent.Listening -> vm.setListening(e.isListening)
                            is VoiceEvent.Partial -> vm.setPartial(e.text)
                            is VoiceEvent.Final -> vm.onFinalTranscript(e.text)
                            is VoiceEvent.Error -> vm.onFinalTranscript("Voice error: ${e.message}")
                        }
                    }
                }

                PushToTalkScreen(
                    vm = vm,
                    onStartListening = { voice.startListening() },
                    onStopListening = { voice.stopListening() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voice.destroy()
        scope.cancel()
    }
}