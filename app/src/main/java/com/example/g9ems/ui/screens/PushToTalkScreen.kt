package com.example.g9ems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.g9ems.data.models.Role
import com.example.g9ems.viewmodel.FormViewModel

@Composable
fun PushToTalkScreen(
    vm: FormViewModel,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onNavigateToDatabase: () -> Unit
) {
    val messages by vm.messages.collectAsState()
    val partial by vm.partialTranscript.collectAsState()
    val listening by vm.isListening.collectAsState()
    val session by vm.session.collectAsState()


    Column(Modifier.fillMaxSize().padding(12.dp)) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Form: ${session.formType}",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(onClick = onNavigateToDatabase) {
                Text("Database")
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val isUser = msg.role == Role.USER
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        if (partial.isNotBlank()) {
            Text(
                text = "Hearing: $partial",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.height(6.dp))
        }

        PushToTalkBar(
            isListening = listening,
            onPress = onStartListening,
            onRelease = onStopListening
        )
    }
}

@Composable
private fun PushToTalkBar(
    isListening: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (isListening) "Listening..." else "Hold to talk")

            Button(
                onClick = { if (!isListening) onPress() else onRelease() },
                colors = ButtonDefaults.buttonColors()
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isListening) "Stop" else "Start")
            }
        }
    }
}