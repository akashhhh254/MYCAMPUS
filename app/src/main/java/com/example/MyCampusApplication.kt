package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyCampusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
    }

    companion object {
        private const val TAG = "MyCampusApplication"

        fun ensureFirebaseInitialized(context: Context): FirebaseApp? {
            val appContext = context.applicationContext ?: context
            return synchronized(this) {
                try {
                    val apps = FirebaseApp.getApps(appContext)
                    if (apps.isNotEmpty()) {
                        return@synchronized apps[0]
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error checking Firebase apps: ${e.message}")
                }

                // First attempt auto-initialization from google-services.json generated resources
                try {
                    val app = FirebaseApp.initializeApp(appContext)
                    if (app != null) {
                        Log.i(TAG, "FirebaseApp auto-initialized from resources")
                        return@synchronized app
                    }
                } catch (e: Exception) {
                    Log.i(TAG, "Auto-init from resources skipped (${e.message}), initializing with default options")
                }

                // If not auto-initialized, initialize with default FirebaseOptions so default FirebaseApp exists
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:1048866506307:android:mycampus")
                        .setProjectId("mycampus-portal")
                        .setApiKey("AIzaSyDefaultKeyForLocalFallbackInitOnly")
                        .build()
                    val app = FirebaseApp.initializeApp(appContext, options)
                    Log.i(TAG, "FirebaseApp initialized with options")
                    return@synchronized app
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseApp.initializeApp with options handling: ${e.message}")
                    try {
                        val apps = FirebaseApp.getApps(appContext)
                        if (apps.isNotEmpty()) apps[0] else null
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }
    }
}
