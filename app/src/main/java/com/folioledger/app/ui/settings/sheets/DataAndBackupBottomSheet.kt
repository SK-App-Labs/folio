package com.folioledger.app.ui.settings.sheets

import com.folioledger.app.domain.util.BackupTimeFormatter

/**
 * Formats epoch timestamp into relative backup description (e.g. "Today, 2:30 PM", "Yesterday, ...", "3d ago").
 * Delegated to [BackupTimeFormatter.formatBackupRelativeTime] for domain cleanliness.
 */
fun formatBackupRelativeTime(epochMillis: Long?): String =
    BackupTimeFormatter.formatBackupRelativeTime(epochMillis)
