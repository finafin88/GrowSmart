package com.growsmart.mobile

import android.app.Application
import com.google.firebase.FirebaseApp

class GrowSmartApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
