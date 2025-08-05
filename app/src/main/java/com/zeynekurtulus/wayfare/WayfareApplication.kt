package com.zeynekurtulus.wayfare

import android.app.Application
import com.zeynekurtulus.wayfare.di.AppContainer

/**
 * Application class for Wayfare.
 * This class initializes the dependency injection container and provides
 * global access to repositories and other dependencies.
 */
class WayfareApplication : Application() {
    
    // AppContainer instance that provides dependencies
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the dependency injection container
        container = AppContainer(this)
    }
}