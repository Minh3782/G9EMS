package com.example.myapplication

import com.example.myapplication.ui.theme.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.couchbase.lite.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

class MainActivity : ComponentActivity() {

    private val databaseManager by lazy {
        DatabaseManager.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Using our beautiful blue theme!
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    DatabaseTestScreen(
                        modifier = Modifier.padding(innerPadding),
                        databaseManager = databaseManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseManager.closeDatabase()
    }
}

@Composable
fun DatabaseTestScreen(
    modifier: Modifier = Modifier,
    databaseManager: DatabaseManager
) {
    var databaseStatus by remember { mutableStateOf("Ready to connect to EMS database") }
    var documentCount by remember { mutableStateOf(0L) }
    var lastError by remember { mutableStateOf<String?>(null) }
    var isDatabaseOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header with EMS branding
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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = "Database Connection Test",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDatabaseOpen)
                            Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isDatabaseOpen)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Database Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = databaseStatus,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (isDatabaseOpen) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Documents stored:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = documentCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                lastError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons in a nice column
        Text(
            text = "Database Operations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        // Button 1 - Open Database
        Button(
            onClick = {
                try {
                    databaseManager.openDatabase("ems_patient_db")
                    isDatabaseOpen = true
                    documentCount = databaseManager.getDocumentCount()
                    databaseStatus = "✅ Database connected successfully"
                    lastError = null
                } catch (e: Exception) {
                    isDatabaseOpen = false
                    lastError = e.message
                    databaseStatus = "❌ Connection failed"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.DataUsage,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("1. Connect to EMS Database")
        }

        // Button 2 - Save Test Document
        Button(
            onClick = {
                try {
                    val db = databaseManager.getDatabase()
                    if (db == null) {
                        lastError = "Please connect to database first"
                        return@Button
                    }

                    val doc = com.couchbase.lite.MutableDocument()
                        .setString("type", "patient_report")
                        .setString("patientName", "Test Patient")
                        .setString("chiefComplaint", "Test emergency")
                        .setLong("timestamp", System.currentTimeMillis())

                    db.save(doc)

                    documentCount = databaseManager.getDocumentCount()
                    databaseStatus = "✅ Test patient record saved"
                    lastError = null
                } catch (e: Exception) {
                    lastError = "Save failed: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("2. Save Test Patient Record")
        }

        // Button 3 - Query Documents
        Button(
            onClick = {
                try {
                    val db = databaseManager.getDatabase()
                    if (db == null) {
                        lastError = "Please connect to database first"
                        return@Button
                    }

                    val query = com.couchbase.lite.QueryBuilder
                        .select(com.couchbase.lite.SelectResult.all())
                        .from(com.couchbase.lite.DataSource.database(db))

                    val results = query.execute()
                    val docs = results.allResults()

                    documentCount = docs.size.toLong()

                    if (docs.isNotEmpty()) {
                        val sample = docs.first()
                        val docId = sample.getString("id")
                        val doc = db.getDocument(docId!!)
                        val patientName = doc?.getString("patientName") ?: "Unknown"
                        databaseStatus =
                            "✅ Found ${docs.size} patient records\nMost recent: $patientName"
                    } else {
                        databaseStatus = "📭 No patient records found"
                    }
                    lastError = null
                } catch (e: Exception) {
                    lastError = "Query failed: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("3. Query Patient Records")
        }

        // Button 4 - Close Database
        Button(
            onClick = {
                databaseManager.closeDatabase()
                isDatabaseOpen = false
                documentCount = 0
                databaseStatus = "Database disconnected"
                lastError = null
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDatabaseOpen,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("4. Disconnect Database")
        }

        // Footer
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "EMS AI Assistant v1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
