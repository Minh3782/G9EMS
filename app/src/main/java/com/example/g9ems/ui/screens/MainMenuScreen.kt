package com.example.g9ems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onSelectTeddyBear: () -> Unit,
    onSelectDatabase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🚑 EMS AI Assistant",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSelectTeddyBear,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("🧸 Teddy Bear Form")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSelectDatabase,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("🗄️ Database Test")
        }
    }
}