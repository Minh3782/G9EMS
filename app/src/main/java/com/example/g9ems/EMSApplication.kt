package com.example.g9ems


import android.app.Application
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.CouchbaseLiteException

class EMSApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // THIS IS THE CRITICAL LINE YOU WERE MISSING!
            CouchbaseLite.init(this)

            android.util.Log.i("EMS-App", "✅ Couchbase Lite initialized successfully")
        } catch (e: CouchbaseLiteException) {
            android.util.Log.e("EMS-App", "❌ Failed to initialize Couchbase Lite", e)
        } catch (e: Exception) {
            android.util.Log.e("EMS-App", "❌ Unexpected error during initialization", e)
        }
    }
}
