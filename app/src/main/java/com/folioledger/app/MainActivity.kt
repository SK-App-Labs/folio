package com.folioledger.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import android.view.animation.PathInterpolator
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.folioledger.app.ui.settings.sheets.FolioLocaleManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.folioledger.app.di.FolioViewModelFactory
import com.folioledger.app.ui.ledger.LedgerHomeViewModel
import com.folioledger.app.ui.shell.FolioMainShell
import com.folioledger.app.ui.theme.FolioTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainLockViewModel : ViewModel() {
    val isSessionUnlocked = mutableStateOf(false)
}

class MainActivity : FragmentActivity() {

    private val lockViewModel: MainLockViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(FolioLocaleManager.wrapContextIfNeeded(newBase))
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            lockViewModel.isSessionUnlocked.value = false
            val app = application as FolioApplication
            app.applicationScope.launch(Dispatchers.IO) {
                app.container.settingsAndBackupRepository.triggerSessionAutoBackupIfDirty()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        enableEdgeToEdge()

        val appContainer = (application as FolioApplication).container
        val viewModelFactory = FolioViewModelFactory(appContainer)

        lifecycleScope.launch(Dispatchers.IO) {
            // Warm up Room SQLite and trigger pre-migration backup on IO dispatcher
            appContainer.database.openHelper.writableDatabase
        }

        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        var lastResolvedThemeMode = "SYSTEM"

        splashScreen.setOnExitAnimationListener { splashViewProvider ->
            val splashView = splashViewProvider.view
            val iconView = splashViewProvider.iconView

            // Seamless Dissolve: Keep icon centered (0 translation).
            // Gently scale (1.0 -> 1.05) and fade out softly with Material 3 ease-out curve.
            val iconScaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 1.05f)
            val iconScaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 1.05f)
            val iconAlpha = ObjectAnimator.ofFloat(iconView, View.ALPHA, 1f, 0f)
            val splashAlpha = ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f)

            AnimatorSet().apply {
                playTogether(iconScaleX, iconScaleY, iconAlpha, splashAlpha)
                duration = 240L
                interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
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

            var isSessionUnlocked by lockViewModel.isSessionUnlocked
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
                    LaunchedEffect(isSessionUnlocked) {
                        if (!isSessionUnlocked) {
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