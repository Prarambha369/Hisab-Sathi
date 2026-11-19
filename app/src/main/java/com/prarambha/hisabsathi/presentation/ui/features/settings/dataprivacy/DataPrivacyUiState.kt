package com.prarambha.hisabsathi.presentation.ui.features.settings.dataprivacy

import com.prarambha.hisabsathi.data.backup.BackupConfiguration
import java.io.File

data class DataPrivacyUiState(
    val importExportMessage: String? = null,
    val exportedBackupFile: File? = null,
    val backupConfiguration: BackupConfiguration = BackupConfiguration()
)
