package com.example.chatapp.activity

import android.app.Application
import com.example.chatapp.applic

class myApplication: Application() {
    override fun onCreate() {

        super.onCreate()
    }
    override fun onTerminate() {
        applic.online("false")
        super.onTerminate()
    }
}