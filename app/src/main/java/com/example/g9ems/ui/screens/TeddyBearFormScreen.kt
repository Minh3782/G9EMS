package com.example.g9ems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.g9ems.data.models.Role
import com.example.g9ems.viewmodel.FormViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.widget.Toast
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeddyBearFormScreen(
    vm: FormViewModel,
    onBackPressed: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    val session by vm.session.collectAsState()
    val messages by vm.messages.collectAsState()
    val partial by vm.partialTranscript.collectAsState()
    val listening by vm.isListening.collectAsState()
    val context = LocalContext.current

    fun getFieldValue(key: String): String =
        session.fields.find { it.key == key }?.value ?: ""

    val currentDateTime = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    // Gender dropdown options
    val genderOptions = listOf("Male", "Female", "Other", "Unknown")
    var genderExpanded by remember { mutableStateOf(false) }

    // Recipient type dropdown options
    val recipientTypeOptions = listOf("Patient", "Bystander Child", "Sibling", "Other")
    var recipientTypeExpanded by remember { mutableStateOf(false) }


    fun handleSubmit() {
        vm.submitForm()
        Toast.makeText(context, "Form saved successfully!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "🧸 Teddy Bear Comfort Program",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance the back button
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── FORM CARD ────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── DATE & TIME OF DISTRIBUTION ──────────────────────────────
                SectionHeader(icon = "🕐", title = "DATE & TIME OF DISTRIBUTION")

                OutlinedTextField(
                    value = currentDateTime,
                    onValueChange = {},
                    label = { Text("DATE / TIME") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ── PRIMARY MEDIC ─────────────────────────────────────────────
                SectionHeader(icon = "👤", title = "PRIMARY MEDIC (REQUIRED)")

                OutlinedTextField(
                    value = getFieldValue("primaryMedicFirstName"),
                    onValueChange = {},
                    label = { Text("FIRST NAME *") },
                    placeholder = { Text("First name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                OutlinedTextField(
                    value = getFieldValue("primaryMedicLastName"),
                    onValueChange = {},
                    label = { Text("LAST NAME *") },
                    placeholder = { Text("Last name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                OutlinedTextField(
                    value = getFieldValue("primaryMedicNumber"),
                    onValueChange = {},
                    label = { Text("MEDIC NUMBER *") },
                    placeholder = { Text("e.g. 10452") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ── SECOND MEDIC ──────────────────────────────────────────────
                SectionHeader(icon = "👤", title = "SECOND MEDIC (OPTIONAL)")

                Text(
                    text = "Complete only if a second medic is on the call.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = getFieldValue("secondMedicFirstName"),
                    onValueChange = {},
                    label = { Text("FIRST NAME") },
                    placeholder = { Text("First name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                OutlinedTextField(
                    value = getFieldValue("secondMedicLastName"),
                    onValueChange = {},
                    label = { Text("LAST NAME") },
                    placeholder = { Text("Last name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                OutlinedTextField(
                    value = getFieldValue("secondMedicNumber"),
                    onValueChange = {},
                    label = { Text("MEDIC NUMBER") },
                    placeholder = { Text("e.g. 10453") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ── TEDDY BEAR RECIPIENT ──────────────────────────────────────
                SectionHeader(icon = "🧸", title = "TEDDY BEAR RECIPIENT")

                OutlinedTextField(
                    value = getFieldValue("recipientAge"),
                    onValueChange = {},
                    label = { Text("AGE") },
                    placeholder = { Text("Age") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = disabledFieldColors()
                )

                // Gender Dropdown
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = getFieldValue("recipientGender").ifBlank { "Select gender" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("GENDER") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    vm.setFieldValue("recipientGender", option)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Recipient Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = recipientTypeExpanded,
                    onExpandedChange = { recipientTypeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = getFieldValue("recipientType").ifBlank { "Select recipient type" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("RECIPIENT TYPE") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recipientTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = recipientTypeExpanded,
                        onDismissRequest = { recipientTypeExpanded = false }
                    ) {
                        recipientTypeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    vm.setFieldValue("recipientType", option)
                                    recipientTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Action Buttons ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { vm.clearForm() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CLEAR")
                    }
                    Button(
                        onClick = { handleSubmit() },
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("SUBMIT RECORD")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── VOICE ASSISTANT CARD ─────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Voice Assistant",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(messages.size) { index ->
                        val msg = messages[index]
                        Text(
                            text = "${msg.role}: ${msg.text}",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (msg.role) {
                                Role.USER      -> MaterialTheme.colorScheme.primary
                                Role.ASSISTANT -> MaterialTheme.colorScheme.tertiary
                                Role.SYSTEM    -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                if (partial.isNotBlank()) {
                    Text(
                        text = "🎤 Hearing: $partial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (listening) {
                            onStopListening()  // If listening, stop
                        } else {
                            onStartListening() // If not listening, start
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (listening)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (listening) "Stop" else "Start")
                }

            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleSmall)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun disabledFieldColors() = OutlinedTextFieldDefaults.colors(
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)