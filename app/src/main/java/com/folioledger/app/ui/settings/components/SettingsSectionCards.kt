package com.folioledger.app.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.folioledger.app.BuildConfig
import com.folioledger.app.R
import com.folioledger.app.domain.model.AutoBackupRetention
import com.folioledger.app.domain.model.NumberGroupingSystem
import com.folioledger.app.domain.model.SupporterTier
import com.folioledger.app.domain.model.UserPreferences
import com.folioledger.app.ui.components.parseHexColorSafely
import com.folioledger.app.ui.settings.SettingsUiState
import com.folioledger.app.ui.iconpack.FolioIconPackTheme
import com.folioledger.app.ui.theme.FolioAccentPalette
import com.folioledger.app.ui.theme.FolioThemeMode
import com.folioledger.app.ui.settings.sheets.formatBackupRelativeTime
import com.folioledger.app.ui.theme.LocalFolioSemanticColors

@Composable
fun TaxonomySettingsCard(
    prefs: UserPreferences,
    uiState: SettingsUiState,
    isNotBusy: Boolean,
    onOpenCategories: () -> Unit,
    onToggleSubcategories: (Boolean) -> Unit,
    onOpenTagsAndBookmarks: () -> Unit,
    onOpenRecurringManager: () -> Unit,
    onOpenRegionalFormat: () -> Unit,
    onOpenSecondaryCurrencies: () -> Unit,
    onOpenMonthStartDayDialog: () -> Unit,
    onToggleCarryOver: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    SettingsGroupCard(
        title = stringResource(R.string.settings_group_taxonomy),
        modifier = modifier
    ) {
        SettingsNavigationRow(
            icon = Icons.Default.Category,
            title = stringResource(R.string.setting_categories),
            valueText = "${uiState.expenseCategories.size + uiState.incomeCategories.size} active",
            enabled = isNotBusy,
            onClick = onOpenCategories
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsSwitchRow(
            icon = Icons.Default.Category,
            title = "Enable Subcategories",
            checked = prefs.subcategoriesEnabled,
            enabled = isNotBusy,
            onCheckedChange = onToggleSubcategories
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.AutoMirrored.Filled.Label,
            title = stringResource(R.string.setting_tags_bookmarks),
            valueText = "${uiState.tags.size} tags • ${uiState.bookmarks.size} saved",
            enabled = isNotBusy,
            onClick = onOpenTagsAndBookmarks
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Default.Autorenew,
            title = "Recurring & Subscriptions",
            valueText = "${uiState.activeRecurringCount} active • ${uiState.formattedMonthlyBurden}/mo",
            enabled = isNotBusy,
            onClick = onOpenRecurringManager
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Default.Payments,
            title = stringResource(R.string.setting_currency_format),
            valueText = "${prefs.primaryCurrencyCode} • ${if (prefs.numberGrouping == NumberGroupingSystem.INDIAN) "12,34,567" else "1,234,567"}",
            enabled = isNotBusy,
            onClick = onOpenRegionalFormat
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Default.Public,
            title = stringResource(R.string.setting_secondary_currencies),
            valueText = when {
                prefs.secondaryCurrencies.isEmpty() -> "None configured"
                prefs.secondaryCurrencies.size == 1 -> {
                    val sec = prefs.secondaryCurrencies.first()
                    "${sec.code} (1 = ${sec.exchangeRateToPrimary})"
                }
                else -> "${prefs.secondaryCurrencies.size} active (${prefs.secondaryCurrencies.joinToString(", ") { it.code }})"
            },
            enabled = isNotBusy,
            onClick = onOpenSecondaryCurrencies
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Default.CalendarMonth,
            title = "Financial Month Start Day",
            valueText = if (prefs.monthStartDay == 1) "1st of month" else "Day ${prefs.monthStartDay}",
            enabled = isNotBusy,
            onClick = onOpenMonthStartDayDialog
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsSwitchRow(
            icon = Icons.Default.Sync,
            title = stringResource(R.string.ledger_banner_carry_over),
            checked = prefs.carryOverEnabled,
            enabled = isNotBusy,
            onCheckedChange = onToggleCarryOver
        )
    }
}

@Composable
fun AppearanceSecuritySettingsCard(
    prefs: UserPreferences,
    isNotBusy: Boolean,
    currentLanguageDisplayName: String,
    onOpenAppearanceSheet: () -> Unit,
    onOpenIconPackSheet: () -> Unit,
    onOpenLanguageSheet: () -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    SettingsGroupCard(
        title = stringResource(R.string.settings_group_appearance),
        modifier = modifier
    ) {
        val themeModeObj = FolioThemeMode.fromKey(prefs.themeMode)
        val accentPaletteObj = FolioAccentPalette.fromKey(prefs.accentPalette)
        SettingsNavigationRow(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.setting_appearance_theme),
            valueText = themeModeObj.displayName,
            valueColorDot = accentPaletteObj.previewColor,
            enabled = isNotBusy,
            onClick = onOpenAppearanceSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        val iconPackThemeObj = FolioIconPackTheme.fromKey(prefs.iconPackTheme)
        SettingsNavigationRow(
            icon = Icons.Default.Category,
            title = "Icon Style",
            valueText = iconPackThemeObj.displayName,
            enabled = isNotBusy,
            onClick = onOpenIconPackSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Default.Language,
            title = stringResource(R.string.setting_app_language),
            valueText = currentLanguageDisplayName,
            enabled = isNotBusy,
            onClick = onOpenLanguageSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsSwitchRow(
            icon = Icons.Default.Fingerprint,
            title = stringResource(R.string.setting_app_lock),
            checked = prefs.biometricLockEnabled,
            enabled = isNotBusy,
            onCheckedChange = onToggleBiometricLock
        )
    }
}

@Composable
fun RemindersSettingsCard(
    prefs: UserPreferences,
    isNotBusy: Boolean,
    onToggleReminders: (Boolean) -> Unit,
    onOpenCcLeadDaysDialog: () -> Unit,
    onOpenBillLeadDaysDialog: () -> Unit,
    onOpenReminderTimeDialog: () -> Unit,
    onToggleHideAmounts: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    SettingsGroupCard(
        title = stringResource(R.string.settings_group_notifications),
        modifier = modifier
    ) {
        SettingsSwitchRow(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.setting_due_reminders),
            checked = prefs.remindersEnabled,
            enabled = isNotBusy,
            onCheckedChange = onToggleReminders
        )

        if (prefs.remindersEnabled) {
            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            val ccText = when (prefs.ccReminderLeadDays) {
                0 -> "On Due Date"
                1 -> "1 day before"
                else -> "${prefs.ccReminderLeadDays} days before"
            }
            SettingsNavigationRow(
                icon = Icons.Default.CreditCard,
                title = stringResource(R.string.setting_cc_lead_days),
                valueText = ccText,
                enabled = isNotBusy,
                onClick = onOpenCcLeadDaysDialog
            )

            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            val billText = when (prefs.billReminderLeadDays) {
                0 -> "On Due Date"
                1 -> "1 day before"
                else -> "${prefs.billReminderLeadDays} days before"
            }
            SettingsNavigationRow(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(R.string.setting_bill_lead_days),
                valueText = billText,
                enabled = isNotBusy,
                onClick = onOpenBillLeadDaysDialog
            )

            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            val timeText = formatReminderTimeDisplay(prefs.reminderHourOfDay, prefs.reminderMinute)
            SettingsNavigationRow(
                icon = Icons.Default.History,
                title = stringResource(R.string.setting_reminder_time),
                valueText = timeText,
                enabled = isNotBusy,
                onClick = onOpenReminderTimeDialog
            )

            HorizontalDivider(
                color = semanticColors.cardStrokeSubtle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SettingsSwitchRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.setting_mask_notification_amounts),
                checked = prefs.hideAmountsOnLockScreen,
                enabled = isNotBusy,
                onCheckedChange = onToggleHideAmounts
            )
        }
    }
}

internal fun formatReminderTimeDisplay(hourOfDay: Int, minute: Int): String {
    val h = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
    val amPm = if (hourOfDay < 12) "AM" else "PM"
    val minStr = if (minute < 10) "0$minute" else "$minute"
    return "$h:$minStr $amPm"
}

@Composable
fun DataBackupsSettingsCard(
    prefs: UserPreferences,
    isNotBusy: Boolean,
    onOpenAutoBackupSheet: () -> Unit,
    onOpenTransferBackupSheet: () -> Unit,
    onOpenPdfReportsSheet: () -> Unit,
    onOpenSpreadsheetsSheet: () -> Unit,
    onOpenClearAllDataDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    SettingsGroupCard(
        title = stringResource(R.string.settings_group_data),
        modifier = modifier
    ) {
        val autoBackupValue = if (prefs.isDemoModeActive) {
            "Paused (Demo Sandbox)"
        } else if (prefs.autoBackupEnabled) {
            when (prefs.autoBackupRetention) {
                AutoBackupRetention.TEN_SESSIONS -> "10 Sessions"
                AutoBackupRetention.THIRTY_DAYS -> "30 Days (Tiered)"
                AutoBackupRetention.NINETY_DAYS -> "90 Days"
            }
        } else "Disabled"

        SettingsNavigationRow(
            icon = Icons.Default.History,
            title = stringResource(R.string.setting_auto_backup),
            valueText = autoBackupValue,
            valueColorDot = if (prefs.autoBackupEnabled) semanticColors.incomeEmerald else semanticColors.warningAmber,
            enabled = isNotBusy,
            onClick = onOpenAutoBackupSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        val backupStatusText = formatBackupRelativeTime(prefs.lastBackupEpochMillis)
        SettingsNavigationRow(
            icon = Icons.Default.Backup,
            title = stringResource(R.string.setting_phone_migration),
            valueText = if (prefs.lastBackupEpochMillis != null) backupStatusText else "Downloads/Folio",
            valueColorDot = if (prefs.lastBackupEpochMillis != null) semanticColors.incomeEmerald else null,
            enabled = isNotBusy,
            onClick = onOpenTransferBackupSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SettingsNavigationRow(
            icon = Icons.Default.PictureAsPdf,
            title = stringResource(R.string.setting_pdf_statements),
            valueText = "Account & Tax Reports",
            enabled = isNotBusy,
            onClick = onOpenPdfReportsSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SettingsNavigationRow(
            icon = Icons.Default.TableChart,
            title = stringResource(R.string.setting_spreadsheets),
            valueText = "Export / Import",
            enabled = isNotBusy,
            onClick = onOpenSpreadsheetsSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SettingsNavigationRow(
            icon = Icons.Default.DeleteForever,
            title = stringResource(R.string.setting_danger_reset),
            valueText = null,
            titleColor = MaterialTheme.colorScheme.error,
            iconTint = MaterialTheme.colorScheme.error,
            enabled = isNotBusy,
            onClick = onOpenClearAllDataDialog
        )
    }
}

@Composable
fun CommunityHelpSettingsCard(
    prefs: UserPreferences,
    isNotBusy: Boolean,
    onOpenUserGuideSheet: () -> Unit,
    onOpenFeedbackSheet: () -> Unit,
    onRateApp: () -> Unit,
    onOpenTipJarSheet: () -> Unit,
    onToggleDemoModeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current

    SettingsGroupCard(
        title = stringResource(R.string.settings_group_community),
        modifier = modifier
    ) {
        if (prefs.isDemoModeActive) {
            SettingsNavigationRow(
                icon = Icons.Default.PlayArrow,
                title = if (prefs.hasStashedRealLedger) "Exit Demo Sandbox (Restore Real Data)" else "Exit Demo Sandbox (Start Fresh)",
                valueText = "Active",
                valueColorDot = semanticColors.warningAmber,
                enabled = isNotBusy,
                onClick = onToggleDemoModeClick
            )
        } else {
            SettingsNavigationRow(
                icon = Icons.Default.PlayArrow,
                title = "Launch Interactive Demo Mode",
                valueText = "Sandbox",
                valueColorDot = semanticColors.incomeEmerald,
                enabled = isNotBusy,
                onClick = onToggleDemoModeClick
            )
        }
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        SettingsNavigationRow(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.setting_user_guide),
            valueText = null,
            enabled = isNotBusy,
            onClick = onOpenUserGuideSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Default.Feedback,
            title = "Send Feedback & Feature Ideas",
            valueText = null,
            enabled = isNotBusy,
            onClick = onOpenFeedbackSheet
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SettingsNavigationRow(
            icon = Icons.Filled.Star,
            title = stringResource(R.string.setting_rate_app),
            valueText = "★ ★ ★ ★ ★",
            valueColorDot = semanticColors.warningAmber,
            enabled = isNotBusy,
            onClick = onRateApp
        )
        HorizontalDivider(
            color = semanticColors.cardStrokeSubtle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        val activeSupporterTier = SupporterTier.fromSkuOrTitle(prefs.supporterTier)
            ?: if (prefs.isSupporter) SupporterTier.GENEROUS_SUPPORTER else null

        SettingsNavigationRow(
            icon = Icons.Default.LocalCafe,
            title = stringResource(R.string.setting_tip_jar),
            valueText = activeSupporterTier?.badgeLabel ?: (if (prefs.isSupporter) "★ Unlocked" else null),
            valueColorDot = activeSupporterTier?.let { parseHexColorSafely(it.accentHex) } ?: (if (prefs.isSupporter) semanticColors.warningAmber else null),
            enabled = isNotBusy,
            onClick = onOpenTipJarSheet
        )
    }
}

@Composable
fun SettingsFooter(
    prefs: UserPreferences,
    modifier: Modifier = Modifier
) {
    val semanticColors = LocalFolioSemanticColors.current
    val activeSupporterTier = SupporterTier.fromSkuOrTitle(prefs.supporterTier)
        ?: if (prefs.isSupporter) SupporterTier.GENEROUS_SUPPORTER else null

    val footerText = activeSupporterTier?.let {
        "Folio Ledger v${BuildConfig.VERSION_NAME} • ${it.footerEdition}"
    } ?: if (prefs.isSupporter) {
        "Folio Ledger v${BuildConfig.VERSION_NAME} • ★ Supporter Edition"
    } else {
        "Folio Ledger v${BuildConfig.VERSION_NAME} • 100% Offline SQLite"
    }

    val footerColor = activeSupporterTier?.let { parseHexColorSafely(it.accentHex) }
        ?: if (prefs.isSupporter) semanticColors.warningAmber else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Text(
            text = footerText,
            style = MaterialTheme.typography.labelSmall,
            color = footerColor,
            fontWeight = if (prefs.isSupporter) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.heightIn(min = 2.dp))
        Text(
            text = "Zero Cloud • Zero Ads • Local Device Privacy",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
