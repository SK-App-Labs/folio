// File: app/src/main/java/com/folioledger/app/MainActivity.kt
package com.folioledger.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folioledger.app.di.FolioViewModelFactory
import com.folioledger.app.ui.shell.FolioMainShell
import com.folioledger.app.ui.theme.FolioTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as FolioApplication).container
        val viewModelFactory = FolioViewModelFactory(appContainer)

        setContent {
            val prefs by appContainer.settingsAndBackupRepository
                .observeUserPreferences()
                .collectAsStateWithLifecycle(initialValue = null)

            var isSessionUnlocked by rememberSaveable { mutableStateOf(false) }
            var authErrorText by rememberSaveable { mutableStateOf<String?>(null) }

            val activePrefs = prefs
            if (activePrefs == null) {
                // Brief blank themed surface while preferences load (< 20ms)
                FolioTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {}
                }
            } else if (!activePrefs.biometricLockEnabled || isSessionUnlocked) {
                FolioMainShell(viewModelFactory = viewModelFactory)
            } else {
                LaunchedEffect(Unit) {
                    triggerBiometricOrCredentialPrompt(
                        onSuccess = {
                            isSessionUnlocked = true
                            authErrorText = null
                        },
                        onError = { msg ->
                            authErrorText = msg
                        }
                    )
                }

                FolioTheme(
                    themeMode = activePrefs.themeMode,
                    accentPalette = activePrefs.accentPalette,
                    dynamicColorEnabled = activePrefs.dynamicColorEnabled
                ) {
                    LockedLedgerScreen(
                        errorMessage = authErrorText,
                        onUnlockClick = {
                            triggerBiometricOrCredentialPrompt(
                                onSuccess = {
                                    isSessionUnlocked = true
                                    authErrorText = null
                                },
                                onError = { msg ->
                                    authErrorText = msg
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun triggerBiometricOrCredentialPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuth = biometricManager.canAuthenticate(authenticators)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // Device has no lock screen or biometrics configured; allow entry rather than locking user out
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Folio")
            .setSubtitle("Verify your identity to access your financial ledger")
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(promptInfo)
    }
}

@Composable
private fun LockedLedgerScreen(
    errorMessage: String?,
    onUnlockClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Folio Ledger is Locked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "Use your fingerprint, face, or device PIN to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(onClick = onUnlockClick) {
                Text(text = "Unlock Folio")
            }
        }
    }
}