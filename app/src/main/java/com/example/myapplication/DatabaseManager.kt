// DatabaseManager.kt
package com.example.myapplication

import android.content.Context
import com.couchbase.lite.*
import com.example.myapplication.data.LoginSession
import java.security.MessageDigest
import java.util.*
import com.example.myapplication.data.User
import com.example.myapplication.data.UserRole

class DatabaseManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return instance ?: synchronized(this) {
                val appContext = context.applicationContext
                instance ?: DatabaseManager(appContext).also { instance = it }
            }
        }

        // Collection names
        const val COLLECTION_USERS = "users"
        const val COLLECTION_SESSIONS = "sessions"
        const val COLLECTION_OCCURRENCE_REPORTS = "occurrence_reports"
        const val COLLECTION_TEDDY_BEAR_RECORDS = "teddy_bear_records"
        const val COLLECTION_SHIFT_REPORTS = "shift_reports"
        const val COLLECTION_STATUS_REPORTS = "status_reports"
    }

    private val appContext: Context
    private var database: Database? = null
    private var usersCollection: com.couchbase.lite.Collection? = null
    private var sessionsCollection: com.couchbase.lite.Collection? = null

    init {
        this.appContext = context
    }

    fun openDatabase(dbName: String): Database {
        try {
            closeDatabase()

            val config = DatabaseConfiguration()
            config.setDirectory(appContext.filesDir.absolutePath)

            database = Database(dbName, config)

            // Create collections
            createCollections()

            return database!!
        } catch (e: CouchbaseLiteException) {
            throw RuntimeException("Failed to open database: ${e.message}", e)
        }
    }

    private fun createCollections() {
        try {
            database?.let { db ->
                usersCollection = db.createCollection(COLLECTION_USERS)
                sessionsCollection = db.createCollection(COLLECTION_SESSIONS)
                db.createCollection(COLLECTION_OCCURRENCE_REPORTS)
                db.createCollection(COLLECTION_TEDDY_BEAR_RECORDS)
                db.createCollection(COLLECTION_SHIFT_REPORTS)
                db.createCollection(COLLECTION_STATUS_REPORTS)
            }
        } catch (e: CouchbaseLiteException) {
            // Collections might already exist
        }
    }

    // ==================== USER AUTHENTICATION METHODS ====================

    /**
     * Authenticate user with medic number and password
     */
    fun authenticateUser(medicNumber: String, password: String): User? {
        try {
            val collection = usersCollection ?: return null

            // Query for user with matching medic number
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.collection(collection))
                .where(
                    Expression.property("medicNumber").equalTo(Expression.string(medicNumber))
                        .and(Expression.property("documentType").equalTo(Expression.string("user")))
                )

            val results = query.execute()
            val allResults = results.allResults()

            if (allResults.isEmpty()) {
                android.util.Log.d("EMS-DB", "❌ No user found with medic number: $medicNumber")
                return null
            }

            // Get the first matching user
            val result = allResults.first()
            val docId = result.getString("id") ?: return null
            val document = collection.getDocument(docId) ?: return null

            // Verify password (in real app, compare hashed passwords)
            val storedPassword = document.getString("passwordHash") ?: ""
            val hashedInput = hashPassword(password)

            if (storedPassword != hashedInput) {
                android.util.Log.d("EMS-DB", "❌ Invalid password for user: $medicNumber")
                return null
            }

            // Create user object from document
            val user = User(
                userId = document.id,
                medicNumber = document.getString("medicNumber") ?: "",
                firstName = document.getString("firstName") ?: "",
                lastName = document.getString("lastName") ?: "",
                role = UserRole.valueOf(document.getString("role") ?: "PARAMEDIC"),
                isActive = document.getBoolean("isActive") ?: true
            )

            android.util.Log.d("EMS-DB", "✅ User authenticated: ${user.firstName} ${user.lastName}")

            // Create login session
            createLoginSession(user.userId, user.medicNumber)

            return user

        } catch (e: Exception) {
            android.util.Log.e("EMS-DB", "❌ Authentication error", e)
            return null
        }
    }

    /**
     * Get user by ID
     */
    fun getUserById(userId: String): User? {
        try {
            val collection = usersCollection ?: return null
            val document = collection.getDocument(userId) ?: return null

            return User(
                userId = document.id,
                medicNumber = document.getString("medicNumber") ?: "",
                firstName = document.getString("firstName") ?: "",
                lastName = document.getString("lastName") ?: "",
                role = UserRole.valueOf(document.getString("role") ?: "PARAMEDIC"),
                isActive = document.getBoolean("isActive") ?: true
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Create a new user (for registration)
     */
    fun createUser(user: User, password: String): Boolean {
        try {
            val collection = usersCollection ?: return false

            // Check if user already exists
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.collection(collection))
                .where(Expression.property("medicNumber").equalTo(Expression.string(user.medicNumber)))

            if (query.execute().allResults().isNotEmpty()) {
                android.util.Log.d("EMS-DB", "❌ User already exists: ${user.medicNumber}")
                return false
            }

            // Create document
            val doc = MutableDocument()
                .setString("medicNumber", user.medicNumber)
                .setString("firstName", user.firstName)
                .setString("lastName", user.lastName)
                .setString("passwordHash", hashPassword(password))
                .setString("role", user.role.name)
                .setBoolean("isActive", user.isActive)
                .setString("documentType", "user")
                .setLong("createdAt", System.currentTimeMillis())

            collection.save(doc)
            android.util.Log.d("EMS-DB", "✅ User created: ${user.medicNumber}")
            return true

        } catch (e: Exception) {
            android.util.Log.e("EMS-DB", "❌ Error creating user", e)
            return false
        }
    }

    // ==================== SESSION MANAGEMENT ====================

    /**
     * Create a login session
     */
    private fun createLoginSession(userId: String, medicNumber: String): String? {
        try {
            val collection = sessionsCollection ?: return null

            val sessionId = UUID.randomUUID().toString()
            val doc = MutableDocument(sessionId)
                .setString("userId", userId)
                .setString("medicNumber", medicNumber)
                .setLong("loginTime", System.currentTimeMillis())
                .setBoolean("isActive", true)
                .setString("documentType", "login_session")

            collection.save(doc)
            return sessionId

        } catch (e: Exception) {
            return null
        }
    }

    /**
     * End login session (logout)
     */
    fun endSession(sessionId: String): Boolean {
        try {
            val collection = sessionsCollection ?: return false
            val document = collection.getDocument(sessionId) ?: return false

            val mutableDoc = document.toMutable()
            mutableDoc.setBoolean("isActive", false)
            mutableDoc.setLong("logoutTime", System.currentTimeMillis())

            collection.save(mutableDoc)
            return true

        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get active session for user
     */
    fun getActiveSession(userId: String): LoginSession? {
        try {
            val collection = sessionsCollection ?: return null

            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.collection(collection))
                .where(
                    Expression.property("userId").equalTo(Expression.string(userId))
                        .and(Expression.property("isActive").equalTo(Expression.booleanValue(true)))
                )
                .orderBy(Ordering.property("loginTime").descending())
                .limit(Expression.intValue(1))

            val results = query.execute()
            if (results.allResults().isEmpty()) return null

            val result = results.allResults().first()
            val docId = result.getString("id") ?: return null
            val doc = collection.getDocument(docId) ?: return null

            return LoginSession(
                sessionId = doc.id,
                userId = doc.getString("userId") ?: "",
                medicNumber = doc.getString("medicNumber") ?: "",
                loginTime = doc.getLong("loginTime") ?: 0,
                isActive = doc.getBoolean("isActive") ?: true
            )

        } catch (e: Exception) {
            return null
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Hash password (simple example - use proper hashing in production)
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Check if medic number exists
     */
    fun medicNumberExists(medicNumber: String): Boolean {
        try {
            val collection = usersCollection ?: return false

            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.collection(collection))
                .where(Expression.property("medicNumber").equalTo(Expression.string(medicNumber)))

            return query.execute().allResults().isNotEmpty()
        } catch (e: Exception) {
            return false
        }
    }

    // ==================== DATABASE METHODS ====================

    fun getDatabase(): Database? = database

    fun getCollection(collectionName: String): com.couchbase.lite.Collection? {
        return try {
            database?.getCollection(collectionName)
        } catch (e: Exception) {
            null
        }
    }

    fun isDatabaseOpen(): Boolean = database != null

    fun closeDatabase() {
        try {
            database?.close()
            database = null
            usersCollection = null
            sessionsCollection = null
        } catch (e: CouchbaseLiteException) {
            android.util.Log.e("EMS-DB", "Error closing database", e)
        }
    }

    fun getDocumentCount(collectionName: String): Long {
        return try {
            val collection = database?.getCollection(collectionName) ?: return 0
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.collection(collection))
            query.execute().allResults().size.toLong()
        } catch (e: Exception) {
            0
        }
    }
}