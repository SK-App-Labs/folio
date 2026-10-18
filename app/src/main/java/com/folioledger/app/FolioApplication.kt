// File: app/src/main/java/com/folioledger/app/FolioApplication.kt
package com.folioledger.app

import android.app.Application
import com.folioledger.app.di.AppContainer

class FolioApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}