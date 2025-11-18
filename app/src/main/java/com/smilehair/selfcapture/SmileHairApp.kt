package com.smilehair.selfcapture

import android.app.Application

class SmileHairApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiConfigManager.init(this)
    }
}

