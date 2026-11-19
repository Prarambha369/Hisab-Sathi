package com.prarambha.hisabsathi.presentation.ui.features.settings.aimodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prarambha.hisabsathi.data.model.AiModel
import com.prarambha.hisabsathi.data.model.AiModelCatalog
import com.prarambha.hisabsathi.data.model.DeviceSpecs
import com.prarambha.hisabsathi.data.model.ModelAdvisor
import com.prarambha.hisabsathi.data.model.ModelFit
import com.prarambha.hisabsathi.data.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiModelItem(
    val model: AiModel,
    val fit: ModelFit,
    val isDownloaded: Boolean,
    val isSelected: Boolean,
    val isDownloading: Boolean,
)

data class AiModelsUiState(
    val specs: DeviceSpecs? = null,
    val items: List<AiModelItem> = emptyList(),
    val downloadProgress: Int = 0,
    val anotherDownloadRunning: Boolean = false,
)

@HiltViewModel
class AiModelsViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
) : ViewModel() {

    private val specs = MutableStateFlow(modelRepository.deviceSpecs())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val uiState: StateFlow<AiModelsUiState> = combine(
        specs,
        modelRepository.downloadedIds,
        modelRepository.selectedModel,
        modelRepository.downloadingModelId,
        modelRepository.downloadProgress,
    ) { specs, downloaded, selected, downloadingId, progress ->
        val fits = ModelAdvisor.rate(specs, AiModelCatalog.all, downloaded)
        AiModelsUiState(
            specs = specs,
            items = AiModelCatalog.all.map { m ->
                AiModelItem(
                    model = m,
                    fit = fits.getValue(m.id),
                    isDownloaded = m.id in downloaded,
                    isSelected = selected?.id == m.id,
                    isDownloading = downloadingId == m.id,
                )
            },
            downloadProgress = progress,
            anotherDownloadRunning = downloadingId != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiModelsUiState())

    init {
        modelRepository.checkModelState()
    }

    fun download(model: AiModel) {
        viewModelScope.launch {
            _message.value = when (modelRepository.startDownload(model)) {
                ModelRepository.StartResult.STARTED -> "Downloading ${model.displayName} (${formatSize(model.sizeBytes)})"
                ModelRepository.StartResult.ALREADY_DOWNLOADED -> null
                ModelRepository.StartResult.ANOTHER_DOWNLOAD_RUNNING -> "Wait for the current download to finish first"
                ModelRepository.StartResult.NOT_ENOUGH_STORAGE ->
                    "Not enough storage: ${model.displayName} needs ${formatSize(ModelAdvisor.requiredStorage(model))} free"
            }
            specs.value = modelRepository.deviceSpecs()
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            modelRepository.cancelDownload()
            specs.value = modelRepository.deviceSpecs()
        }
    }

    fun select(model: AiModel) {
        viewModelScope.launch {
            modelRepository.selectModel(model)
            _message.value = "${model.displayName} will be used for chat"
        }
    }

    fun delete(model: AiModel) {
        viewModelScope.launch {
            modelRepository.deleteModel(model)
            specs.value = modelRepository.deviceSpecs()
            _message.value = "${model.displayName} deleted"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        fun formatSize(bytes: Long): String =
            if (bytes >= 1_000_000_000) String.format(java.util.Locale.US, "%.1f GB", bytes / 1_000_000_000.0)
            else "${bytes / 1_000_000} MB"
    }
}
