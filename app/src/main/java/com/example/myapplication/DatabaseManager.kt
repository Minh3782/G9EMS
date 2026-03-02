package com.example.myapplication

import android.content.Context
import com.couchbase.lite.*

class DatabaseManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            return instance ?: synchronized(this) {
                // Store ONLY application context - this is safe!
                val appContext = context.applicationContext
                instance ?: DatabaseManager(appContext).also { instance = it }
            }
        }
    }

    // Store application context only - this is safe for the entire app lifecycle
    private val appContext: Context
    private var database: Database? = null

    // Primary constructor receives application context
    init {
        this.appContext = context
    }

    /**
     * Open or create a database
     */
    fun openDatabase(dbName: String): Database {
        try {
            // Close any existing database first
            closeDatabase()

            // METHOD 1: Create configuration using DatabaseConfiguration class
            val config = DatabaseConfiguration()
            config.setDirectory(appContext.filesDir.absolutePath)

            // Create or open the database
            database = Database(dbName, config)

            android.util.Log.i("DatabaseManager", "✅ Database '$dbName' opened successfully at: ${appContext.filesDir.absolutePath}")
            return database!!

        } catch (e: CouchbaseLiteException) {
            android.util.Log.e("DatabaseManager", "❌ Failed to open database", e)
            throw RuntimeException("Failed to open database: ${e.message}", e)
        } catch (e: Exception) {
            android.util.Log.e("DatabaseManager", "❌ Unexpected error", e)
            throw RuntimeException("Unexpected error: ${e.message}", e)
        }
    }

    //Alternative: Open database with default configuration
    fun openDatabaseSimple(dbName: String): Database {
        try {
            closeDatabase()

            // METHOD 2: Use default configuration (stores in app's default directory)
            database = Database(dbName)

            android.util.Log.i("DatabaseManager", "✅ Database '$dbName' opened successfully")
            return database!!
        } catch (e: CouchbaseLiteException) {
            android.util.Log.e("DatabaseManager", "❌ Failed to open database", e)
            throw RuntimeException("Failed to open database: ${e.message}", e)
        }
    }

    /**
     * Get the current database instance
     */
    fun getDatabase(): Database? = database

    /**
     * Check if database is open
     */
    fun isDatabaseOpen(): Boolean = database != null

    /**
     * Close the database
     */
    fun closeDatabase() {
        try {
            database?.let { db ->
                db.close()
                database = null
                android.util.Log.i("DatabaseManager", "✅ Database closed successfully")
            }
        } catch (e: CouchbaseLiteException) {
            android.util.Log.e("DatabaseManager", "❌ Error closing database", e)
        }
    }

    /**
     * Delete a database (use with caution!)
     */
    fun deleteDatabase(dbName: String): Boolean {
        return try {
            closeDatabase()
            Database.delete(dbName, appContext.filesDir)
            android.util.Log.i("DatabaseManager", "✅ Database '$dbName' deleted")
            true
        } catch (e: CouchbaseLiteException) {
            android.util.Log.e("DatabaseManager", "❌ Failed to delete database", e)
            false
        }
    }

    /**
     * Get database path
     */
    fun getDatabasePath(): String? {
        return database?.path
    }

    /**
     * Get database name
     */
    fun getDatabaseName(): String? {
        return database?.name
    }

    /**
     * Get document count (for testing)
     */
    fun getDocumentCount(): Long {
        return try {
            val db = database ?: return 0
            val query = QueryBuilder
                .select(SelectResult.all())
                .from(DataSource.database(db))
            query.execute().allResults().size.toLong()
        } catch (e: Exception) {
            android.util.Log.e("DatabaseManager", "Error counting documents", e)
            0
        }
    }
}