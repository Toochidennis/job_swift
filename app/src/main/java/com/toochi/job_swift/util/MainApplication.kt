package com.toochi.job_swift.util

import android.app.Application
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}