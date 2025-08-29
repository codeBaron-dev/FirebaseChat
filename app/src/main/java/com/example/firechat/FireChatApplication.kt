package com.example.firechat

import android.app.Application
import com.example.firechat.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application class for FireChat.
 *
 * This class is responsible for initializing the Koin dependency injection framework.
 */
class FireChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@FireChatApplication)
            modules(appModule)
        }
    }
}