package com.ritesh.cashiro.data.repository

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import com.ritesh.cashiro.core.Constants
import com.ritesh.cashiro.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: kotlinx.coroutines.Job? = null
    private val _modelState = MutableStateFlow(ModelState.LOADING)
    val modelState: Flow<ModelState> = _modelState.asStateFlow()
    
    init {
        checkModelState()
    }

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: Flow<Int> = _downloadProgress.asStateFlow()
    
    fun getModelFile(): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), Constants.ModelDownload.MODEL_FILE_NAME)
        Log.d("ModelRepository", "Model file path: ${file.absolutePath}")
        return file
    }
    
    fun isModelDownloaded(): Boolean {
        val modelFile = getModelFile()
        val exists = modelFile.exists()
        val size = if (exists) modelFile.length() else 0
        val expectedSize = Constants.ModelDownload.MODEL_SIZE_BYTES
        // strict: anything over 99% is complete
        val minSize = (expectedSize * 0.99).toLong() 
        val isDownloaded = exists && size >= minSize
        
        Log.d("ModelRepository", "Checking model: exists=$exists, size=$size bytes (${size/1024/1024}MB), expectedMinSize=$minSize, isDownloaded=$isDownloaded")
        return isDownloaded
    }
    
    fun updateModelState(state: ModelState) {
        Log.d("ModelRepository", "Updating model state from ${_modelState.value} to $state")
        _modelState.value = state
    }

    fun updateDownloadProgress(progress: Int) {
        _downloadProgress.value = progress
    }
    
    fun checkModelState() {
        repositoryScope.launch {
            val activeDownloadId = userPreferencesRepository.getActiveDownloadId()
            if (activeDownloadId != null) {
                Log.d("ModelRepository", "checkModelState: active download found, monitoring ID: $activeDownloadId")
                monitorDownload(activeDownloadId)
                return@launch
            }

            val isDownloaded = isModelDownloaded()
            val newState = if (isDownloaded) {
                ModelState.READY
            } else {
                ModelState.NOT_DOWNLOADED
            }
            Log.d("ModelRepository", "checkModelState: setting state to $newState")
            _modelState.value = newState
        }
    }

    private fun startMonitoringActiveDownload() {
        repositoryScope.launch {
            val downloadId = userPreferencesRepository.getActiveDownloadId()
            if (downloadId != null) {
                monitorDownload(downloadId)
            }
        }
    }

    fun monitorDownload(downloadId: Long) {
        monitorJob?.cancel()
        monitorJob = repositoryScope.launch {
            Log.d("ModelRepository", "Starting monitoring for download ID: $downloadId")
            _modelState.value = ModelState.DOWNLOADING
            
            while (isActive && _modelState.value == ModelState.DOWNLOADING) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val bytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalBytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    if (bytesColumnIndex != -1 && totalBytesColumnIndex != -1) {
                        val bytesDownloaded = cursor.getLong(bytesColumnIndex)
                        val bytesTotal = cursor.getLong(totalBytesColumnIndex)

                        if (bytesTotal > 0) {
                            val progress = (bytesDownloaded * 100 / bytesTotal).toInt()
                            _downloadProgress.value = progress
                        }
                    }

                    if (statusColumnIndex != -1) {
                        when (cursor.getInt(statusColumnIndex)) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Log.d("ModelRepository", "Download successful!")
                                _downloadProgress.value = 100
                                _modelState.value = ModelState.READY
                                userPreferencesRepository.clearActiveDownloadId()
                                cursor.close()
                                return@launch
                            }
                            DownloadManager.STATUS_FAILED -> {
                                Log.d("ModelRepository", "Download failed")
                                _modelState.value = ModelState.NOT_DOWNLOADED
                                _downloadProgress.value = 0
                                userPreferencesRepository.clearActiveDownloadId()
                                cursor.close()
                                return@launch
                            }
                        }
                    }
                }
                cursor?.close()
                delay(1000)
            }
        }
    }
    
    fun deleteModel(): Boolean {
        val modelFile = getModelFile()
        return if (modelFile.exists()) {
            val deleted = modelFile.delete()
            if (deleted) {
                _modelState.value = ModelState.NOT_DOWNLOADED
            }
            deleted
        } else {
            false
        }
    }
}

enum class ModelState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    READY,
    LOADING,
    ERROR
}