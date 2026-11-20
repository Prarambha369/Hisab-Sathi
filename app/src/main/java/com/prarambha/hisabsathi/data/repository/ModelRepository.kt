package com.prarambha.hisabsathi.data.repository

import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.prarambha.hisabsathi.BuildConfig
import com.prarambha.hisabsathi.data.model.AiModel
import com.prarambha.hisabsathi.data.model.AiModelCatalog
import com.prarambha.hisabsathi.data.model.DeviceSpecs
import com.prarambha.hisabsathi.data.model.ModelAdvisor
import com.prarambha.hisabsathi.data.preferences.UserPreferencesRepository
import com.prarambha.hisabsathi.domain.service.LlmService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the on-device chat models: which are downloaded, which one is selected for chat, and the
 * single DownloadManager download that may be running. See [AiModelCatalog] for the models.
 */
@Singleton
class ModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val llmService: LlmService,
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: Job? = null

    private val _selectedModel = MutableStateFlow<AiModel?>(null)
    /** Model used for chat. Null until the user downloads or picks one. */
    val selectedModel: StateFlow<AiModel?> = _selectedModel.asStateFlow()

    private val _downloadedIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedIds: StateFlow<Set<String>> = _downloadedIds.asStateFlow()

    private val _downloadingModelId = MutableStateFlow<String?>(null)
    val downloadingModelId: StateFlow<String?> = _downloadingModelId.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    /** Progress (0–100) of the running download, if any. */
    val downloadProgress: Flow<Int> = _downloadProgress.asStateFlow()

    private val _modelState = MutableStateFlow(ModelState.LOADING)
    /** State of the selected model, as the chat screen sees it. */
    val modelState: Flow<ModelState> = _modelState.asStateFlow()

    init {
        checkModelState()
    }

    private fun dir(): File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    fun getModelFile(model: AiModel): File = File(dir(), model.fileName)

    /** File of the selected model (falls back to the original Qwen 1.5B path). */
    fun getModelFile(): File = getModelFile(_selectedModel.value ?: AiModelCatalog.QWEN_1_5B)

    fun isDownloaded(model: AiModel): Boolean {
        if (_downloadingModelId.value == model.id) return false
        val file = getModelFile(model)
        return file.exists() && file.length() >= (model.sizeBytes * 0.98).toLong()
    }

    fun isModelDownloaded(): Boolean = _selectedModel.value?.let { isDownloaded(it) } ?: false

    /** Max tokens for the selected model (its exported KV-cache size). */
    fun contextTokens(): Int = (_selectedModel.value ?: AiModelCatalog.QWEN_1_5B).contextTokens

    fun deviceSpecs(): DeviceSpecs {
        val memInfo = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memInfo)
        val free = (dir() ?: context.filesDir).usableSpace
        return DeviceSpecs(totalRamGb = memInfo.totalMem / 1_000_000_000.0, freeStorageBytes = free)
    }

    fun checkModelState() {
        scope.launch {
            val activeId = userPreferencesRepository.getActiveDownloadId()
            if (activeId != null) {
                val modelId = userPreferencesRepository.getActiveDownloadModelId()
                _downloadingModelId.value = (AiModelCatalog.byId(modelId) ?: AiModelCatalog.QWEN_1_5B).id
                monitorDownload(activeId)
            }
            refresh()
        }
    }

    /** Recomputes downloaded models, the selection and [modelState]. */
    private suspend fun refresh() {
        _downloadedIds.value = AiModelCatalog.all.filter { isDownloaded(it) }.map { it.id }.toSet()
        val saved = AiModelCatalog.byId(userPreferencesRepository.selectedAiModelId.first())
        val selected = saved
            ?: AiModelCatalog.all.firstOrNull { it.id == AiModelCatalog.QWEN_1_5B.id && it.id in _downloadedIds.value }
            ?: AiModelCatalog.all.firstOrNull { it.id in _downloadedIds.value }
        _selectedModel.value = selected
        _modelState.value = when {
            selected == null -> ModelState.NOT_DOWNLOADED
            _downloadingModelId.value == selected.id -> ModelState.DOWNLOADING
            selected.id in _downloadedIds.value -> ModelState.READY
            else -> ModelState.NOT_DOWNLOADED
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "refresh: selected=${selected?.id} downloaded=${_downloadedIds.value} state=${_modelState.value}")
    }

    enum class StartResult { STARTED, ALREADY_DOWNLOADED, ANOTHER_DOWNLOAD_RUNNING, NOT_ENOUGH_STORAGE }

    suspend fun startDownload(model: AiModel): StartResult {
        if (isDownloaded(model)) return StartResult.ALREADY_DOWNLOADED
        if (_downloadingModelId.value != null) return StartResult.ANOTHER_DOWNLOAD_RUNNING
        if (deviceSpecs().freeStorageBytes < ModelAdvisor.requiredStorage(model)) return StartResult.NOT_ENOUGH_STORAGE

        // DownloadManager won't overwrite: remove any partial file first.
        getModelFile(model).delete()
        val request = DownloadManager.Request(model.url.toUri())
            .setTitle("${model.displayName} AI model")
            .setDescription("Downloading the on-device AI assistant for Hisab Sathi")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, model.fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        val id = downloadManager.enqueue(request)
        userPreferencesRepository.saveActiveDownloadId(id, model.id)
        _downloadingModelId.value = model.id
        _downloadProgress.value = 0
        // With nothing usable selected yet, the model being downloaded becomes the chat model.
        if (_selectedModel.value?.let { isDownloaded(it) } != true) {
            userPreferencesRepository.setSelectedAiModelId(model.id)
        }
        refresh()
        monitorDownload(id)
        return StartResult.STARTED
    }

    suspend fun cancelDownload() {
        val id = userPreferencesRepository.getActiveDownloadId() ?: return
        val model = AiModelCatalog.byId(_downloadingModelId.value)
        monitorJob?.cancel()
        downloadManager.remove(id)
        userPreferencesRepository.clearActiveDownloadId()
        _downloadingModelId.value = null
        _downloadProgress.value = 0
        model?.let { getModelFile(it).delete() }
        refresh()
    }

    suspend fun selectModel(model: AiModel) {
        if (_selectedModel.value?.id == model.id) return
        userPreferencesRepository.setSelectedAiModelId(model.id)
        // The loaded engine belongs to the previous model.
        llmService.reset()
        refresh()
    }

    suspend fun deleteModel(model: AiModel): Boolean {
        if (_selectedModel.value?.id == model.id) llmService.reset()
        val deleted = getModelFile(model).delete()
        refresh()
        return deleted
    }

    /** Deletes the selected model. */
    suspend fun deleteModel(): Boolean = _selectedModel.value?.let { deleteModel(it) } ?: false

    private fun monitorDownload(downloadId: Long) {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (isActive) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor == null || !cursor.moveToFirst()) {
                    // The download vanished (cleared by the user or the system).
                    cursor?.close()
                    finishDownload()
                    return@launch
                }
                val done = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                cursor.close()
                if (total > 0) _downloadProgress.value = (done * 100 / total).toInt()
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        _downloadProgress.value = 100
                        finishDownload()
                        return@launch
                    }
                    DownloadManager.STATUS_FAILED -> {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Download $downloadId failed")
                        AiModelCatalog.byId(_downloadingModelId.value)?.let { getModelFile(it).delete() }
                        _downloadProgress.value = 0
                        finishDownload()
                        return@launch
                    }
                }
                delay(1000)
            }
        }
    }

    private suspend fun finishDownload() {
        userPreferencesRepository.clearActiveDownloadId()
        val finished = AiModelCatalog.byId(_downloadingModelId.value)
        _downloadingModelId.value = null
        // Switch chat to the new model if the selected one isn't usable.
        if (finished != null && isDownloaded(finished) && _selectedModel.value?.let { isDownloaded(it) } != true) {
            userPreferencesRepository.setSelectedAiModelId(finished.id)
            llmService.reset()
        }
        refresh()
    }

    private companion object {
        const val TAG = "ModelRepository"
    }
}

enum class ModelState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    READY,
    LOADING,
    ERROR
}
