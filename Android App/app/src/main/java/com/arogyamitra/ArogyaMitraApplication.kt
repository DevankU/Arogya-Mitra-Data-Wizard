package com.arogyamitra

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArogyaMitraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: ArogyaMitraApplication
            private set
    }
}
