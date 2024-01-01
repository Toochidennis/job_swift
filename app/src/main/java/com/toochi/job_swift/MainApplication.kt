package com.toochi.job_swift

import android.app.Application
import com.google.firebase.FirebaseApp
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        //Timber.plant(Timber.DebugTree())
    }
}