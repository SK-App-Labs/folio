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
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.folioledger.app.di.FolioViewModelFactory
import com.folioledger.app.ui.ledger.LedgerHomeViewModel
import com.folioledger.app.ui.shell.FolioMainShell
import com.folioledger.app.ui.theme.FolioTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        enableEdgeToEdge()

        val appContainer = (application as FolioApplication).container
        val viewModelFactory = FolioViewModelFactory(appContainer)

        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        var lastResolvedThemeMode = "SYSTEM"

        splashScreen.setOnExitAnimationListener { splashViewProvider ->
            val splashView = splashViewProvider.view
            val iconView = splashViewProvider.iconView

            val iconScaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 0.55f)
            val iconScaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 0.55f)
            val iconTranslationY = ObjectAnimator.ofFloat(
                iconView,
                View.TRANSLATION_Y,
                0f,
                -iconView.height.toFloat() * 1.6f
            )
            val splashAlpha = ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f)

            AnimatorSet().apply {
                playTogether(iconScaleX, iconScaleY, iconTranslationY, splashAlpha)
                duration = 380L
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        splashViewProvider.remove()
                        val systemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                        val isDarkActive = when (lastResolvedThemeMode) {
                            "LIGHT" -> false
                            "DARK", "AMOLED_BLACK" -> true
                            else -> systemDark
                        }
                        WindowCompat.getInsetsController(window, window.decorView).apply {
                            isAppearanceLightStatusBars = !isDarkActive
                            isAppearanceLightNavigationBars = !isDarkActive
                        }
                    }
                })
                start()
            }
        }

        setContent {
            val prefs by appContainer.settingsAndBackupRepository
                .observeUserPreferences()
                .collectAsStateWithLifecycle(initialValue = null)

            val ledgerViewModel: LedgerHomeViewModel = viewModel(factory = viewModelFactory)
            val ledgerUiState by ledgerViewModel.uiState.collectAsStateWithLifecycle()

            var isSessionUnlocked by rememberSaveable { mutableStateOf(false) }
            var authErrorText by rememberSaveable { mutableStateOf<String?>(null) }

            val activePrefs = prefs
            if (activePrefs != null) {
                lastResolvedThemeMode = activePrefs.themeMode
            }
            val isInitialDataLoaded = ledgerUiState.isInitialDataLoaded
            val isDataReady = activePrefs != null && isInitialDataLoaded

            LaunchedEffect(isDataReady) {
                if (isDataReady) {
                    keepSplashScreen = false
                }
            }

            val themeMode = activePrefs?.themeMode ?: "SYSTEM"
            val accentPalette = activePrefs?.accentPalette ?: "EMERALD"
            val dynamicColorEnabled = activePrefs?.dynamicColorEnabled ?: false

            FolioTheme(
                themeMode = themeMode,
                accentPalette = accentPalette,
                dynamicColorEnabled = dynamicColorEnabled
            ) {
                if (activePrefs == null || !isInitialDataLoaded) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {}
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