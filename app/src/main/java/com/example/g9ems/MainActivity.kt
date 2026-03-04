package com.example.g9ems

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.*
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.g9ems.data.local.DatabaseManager
import com.example.g9ems.data.models.FormSession
import com.example.g9ems.ui.screens.MainMenuScreen
import com.example.g9ems.ui.screens.TeddyBearFormScreen
import com.example.g9ems.ui.theme.G9EMSTheme
import com.example.g9ems.voice.VoiceEvent
import com.example.g9ems.voice.VoiceRecognizer
import com.example.g9ems.viewmodel.FormViewModel

class MainActivity : ComponentActivity() {

    private val databaseManager by lazy {
        DatabaseManager.getInstance(applicationContext)
    }

    private lateinit var voiceRecognizer: VoiceRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        requestMicrophonePermission()

        voiceRecognizer = VoiceRecognizer(this)



        setContent {
            G9EMSTheme {
                val formViewModel: FormViewModel = viewModel()

                // Collect voice events and push into VM
                LaunchedEffect(Unit) {
                    voiceRecognizer.events.collect { e ->
                        when (e) {
                            is VoiceEvent.Listening -> formViewModel.setListening(e.isListening)
                            is VoiceEvent.Partial -> formViewModel.setPartial(e.text)
                            is VoiceEvent.Final -> formViewModel.onFinalTranscript(e.text)
                            is VoiceEvent.Error -> formViewModel.onFinalTranscript("Voice error: ${e.message}")
                        }
                    }
                }

                // Choose which screen to show
                var currentScreen by remember { mutableStateOf("MAIN_MENU") } // "MAIN_MENU", "TEDDY_BEAR", "DATABASE_TEST"

                when (currentScreen) {
                    "MAIN_MENU" -> {
                        MainMenuScreen(
                            onSelectTeddyBear = { currentScreen = "TEDDY_BEAR" },
                            onSelectDatabase = { currentScreen = "DATABASE_TEST" }
                        )
                    }

                    "TEDDY_BEAR" -> {
                        TeddyBearFormScreen(
                            vm = formViewModel,
                            onBackPressed = {
                                currentScreen = "MAIN_MENU"
                            },
                            onStartListening = { voiceRecognizer.startListening() },  // Add this
                            onStopListening = { voiceRecognizer.stopListening() }     // Add this
                        )
                    }

                    "DATABASE_TEST" -> {
                        DatabaseTestScreen(
                            onNavigateBack = { currentScreen = "MAIN_MENU" },
                            databaseManager = databaseManager,
                            formViewModel = formViewModel,
                            voiceRecognizer = voiceRecognizer
                        )
                    }
                }
            }
        }

    }

        override fun onDestroy() {
            super.onDestroy()
            databaseManager.closeDatabase()
            voiceRecognizer.destroy()
        }

        private fun requestMicrophonePermission() {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.d("EMS-PERM", "✅ Microphone permission granted")
                }

                else -> {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        }


        companion object {
            private const val PERMISSION_REQUEST_CODE = 1001
        }
}

@Composable
fun DatabaseTestScreen(
    onNavigateBack: () -> Unit,
    databaseManager: DatabaseManager,
    formViewModel: FormViewModel,  // Now using the ViewModel
    voiceRecognizer: VoiceRecognizer
) {
    var databaseStatus by remember { mutableStateOf("Ready to connect to EMS database") }
    var documentCount by remember { mutableStateOf(0L) }
    var lastError by remember { mutableStateOf<String?>(null) }
    var isDatabaseOpen by remember { mutableStateOf(false) }

    // Collect the current session from ViewModel
    val session by formViewModel.session.collectAsState()


    fun performOpenDatabase() {
        try {
            databaseManager.openDatabase("ems_patient_db")
            isDatabaseOpen = true
            databaseStatus = "✅ Database connected successfully"
            lastError = null
        } catch (e: Exception) {
            isDatabaseOpen = false
            lastError = e.message
            databaseStatus = "❌ Connection failed"
        }
    }


    fun performSaveSession(session: FormSession) {
        try {
            val db = databaseManager.getDatabase()
            if (db == null) {
                lastError = "Please connect to database first"
                return
            }

            // Create a document from the FormSession
            val doc = com.couchbase.lite.MutableDocument()
                .setString("type", "form_session")
                .setString("formType", session.formType.name)
                .setString("status", session.status.name)
                .setLong("timestamp", System.currentTimeMillis())

            // Add all form fields to the document
            session.fields.forEach { field ->
                doc.setString(field.key, field.value ?: "")
            }

            db.save(doc)

            databaseStatus = "✅ Form session saved successfully"
            lastError = null
        } catch (e: Exception) {
            lastError = "Save failed: ${e.message}"
        }
    }

    fun performQueryDocuments() {
        try {
            val db = databaseManager.getDatabase()
            if (db == null) {
                lastError = "Please connect to database first"
                return
            }

            val query = com.couchbase.lite.QueryBuilder
                .select(com.couchbase.lite.SelectResult.all())
                .from(com.couchbase.lite.DataSource.database(db))
                .where(
                    com.couchbase.lite.Expression.property("type")
                        .equalTo(com.couchbase.lite.Expression.string("form_session"))
                )
                .orderBy(
                    com.couchbase.lite.Ordering.expression(
                        com.couchbase.lite.Expression.property("timestamp")
                    ).descending()
                )

            val results = query.execute()
            val docs = results.allResults()

            documentCount = docs.size.toLong()

            if (docs.isNotEmpty()) {
                val sample = docs.first()
                val docId = sample.getString("id")
                val doc = db.getDocument(docId!!)
                val formType = doc?.getString("formType") ?: "Unknown"
                databaseStatus = "✅ Found ${docs.size} form sessions\nMost recent: $formType"
            } else {
                databaseStatus = "📭 No form sessions found"
            }
            lastError = null
        } catch (e: Exception) {
            lastError = "Query failed: ${e.message}"
        }
    }

    fun performCloseDatabase() {
        databaseManager.closeDatabase()
        isDatabaseOpen = false
        documentCount = 0
        databaseStatus = "Database disconnected"
        lastError = null
    }

    // Voice commands for database operations

    LaunchedEffect(Unit) {
        voiceRecognizer.events.collect { e ->
            if (e is VoiceEvent.Final) {
                when (e.text.lowercase()) {
                    "connect database", "open database" -> {
                        if (!isDatabaseOpen) {
                            performOpenDatabase()
                        }
                    }

                    "save patient", "save session", "save form" -> {
                        if (isDatabaseOpen) {
                            performSaveSession(session)
                        }
                    }

                    "query patients", "show patients", "show sessions" -> {
                        if (isDatabaseOpen) {
                            performQueryDocuments()
                        }
                    }

                    "close database", "disconnect" -> {
                        if (isDatabaseOpen) {
                            performCloseDatabase()
                        }
                    }

                    "go back", "back to form" -> onNavigateBack()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with back button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🚑",
                        fontSize = MaterialTheme.typography.displaySmall.fontSize
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EMS AI Assistant",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Database Test",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    lastError != null -> MaterialTheme.colorScheme.errorContainer
                    isDatabaseOpen -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDatabaseOpen)
                            androidx.compose.material.icons.Icons.Default.CheckCircle
                        else androidx.compose.material.icons.Icons.Default.Info,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Database Status", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = databaseStatus, style = MaterialTheme.typography.bodyLarge)

                if (isDatabaseOpen) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Documents:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = documentCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                lastError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "⚠️ $it", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Current Form Info
        if (isDatabaseOpen) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Form Session",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Form Type: ${session.formType.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Status: ${session.status.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Fields: ${session.fields.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Database Operations
        Text(
            text = "Database Operations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        // Button 1 - Open Database
        Button(
            onClick = { performOpenDatabase() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDatabaseOpen
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.DataUsage,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("1. Connect to Database")
        }

        // Button 2 - Save Current Form Session
        Button(
            onClick = { performSaveSession(session) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("2. Save Current Form Session")
        }

        // Button 3 - Query Form Sessions
        Button(
            onClick = { performQueryDocuments() },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("3. Query Saved Sessions")
        }

        // Button 4 - Close Database
        Button(
            onClick = { performCloseDatabase() },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("4. Disconnect Database")
        }

        // Voice command hint
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Mic,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Try: 'connect', 'save session', 'show sessions', 'close', 'back'",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "EMS AI Assistant v1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
