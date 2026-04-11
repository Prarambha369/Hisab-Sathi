package com.ritesh.cashiro.presentation.ui.features.settings

data class SettingsUiState(
    val downloadStatus: DownloadState = DownloadState.NOT_DOWNLOADED,
    val downloadProgress: Int = 0,
    val downloadedMB: Long = 0L,
    val totalMB: Long = 0L,
    val importExportMessage: String? = null,
    val exportedBackupFile: java.io.File? = null,
    val isSeeding: Boolean = false,
    val seedMessage: String? = null
)

enum class DownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    ERROR_INSUFFICIENT_SPACE
}
