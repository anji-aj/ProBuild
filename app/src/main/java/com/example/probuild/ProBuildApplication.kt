package com.example.probuild

import android.app.Application
import com.example.probuild.di.AppContainer
import com.example.probuild.di.DefaultAppContainer
import com.example.probuild.di.ServiceLocator

/**
 * Application class to initialize the [AppContainer].
 */
class ProBuildApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        container = DefaultAppContainer()
    }
}
