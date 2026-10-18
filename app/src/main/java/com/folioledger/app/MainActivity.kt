// File: app/src/main/java/com/folioledger/app/MainActivity.kt
package com.folioledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.folioledger.app.di.FolioViewModelFactory
import com.folioledger.app.ui.shell.FolioMainShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as FolioApplication).container
        val viewModelFactory = FolioViewModelFactory(appContainer)

        setContent {
            FolioMainShell(viewModelFactory = viewModelFactory)
        }
    }
}