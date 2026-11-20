package com.prarambha.hisabsathi.presentation.ui.features.settings.aimodels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prarambha.hisabsathi.data.model.AiModel
import com.prarambha.hisabsathi.data.model.ModelFit
import com.prarambha.hisabsathi.presentation.effects.overScrollVertical
import com.prarambha.hisabsathi.presentation.ui.components.CustomTitleTopAppBar
import com.prarambha.hisabsathi.presentation.ui.components.DeleteAIModelDialog
import com.prarambha.hisabsathi.presentation.ui.features.categories.NavigationContent
import com.prarambha.hisabsathi.presentation.ui.theme.Dimensions
import com.prarambha.hisabsathi.presentation.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiModelsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiModelsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }
    var modelToDelete by remember { mutableStateOf<AiModel?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                title = "AI Models",
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehavior,
                hazeState = hazeState,
                hasBackButton = true,
                navigationContent = { NavigationContent { onNavigateBack() } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .overScrollVertical()
                    .verticalScroll(rememberScrollState())
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = Dimensions.Padding.content),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Spacer(Modifier.height(Spacing.sm))
                uiState.specs?.let { specs ->
                    Text(
                        text = "Your phone: ${String.format(Locale.US, "%.1f", specs.totalRamGb)} GB RAM · " +
                                "${AiModelsViewModel.formatSize(specs.freeStorageBytes)} free. " +
                                "Models run fully offline; your financial data never leaves the phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                uiState.items.forEach { item ->
                    ModelCard(
                        item = item,
                        progress = uiState.downloadProgress,
                        downloadBlocked = uiState.anotherDownloadRunning && !item.isDownloading,
                        onDownload = { viewModel.download(item.model) },
                        onCancel = { viewModel.cancelDownload() },
                        onSelect = { viewModel.select(item.model) },
                        onDelete = { modelToDelete = item.model },
                    )
                }

                Text(
                    text = "Labels are estimates from your phone's RAM. Bigger models answer better but " +
                            "reply slower and use more battery. Gemma isn't offered because Google's licence " +
                            "requires a Hugging Face login to download it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(110.dp))
            }
        }
    }

    modelToDelete?.let { model ->
        DeleteAIModelDialog(
            onDismiss = { modelToDelete = null },
            onDelete = {
                viewModel.delete(model)
                modelToDelete = null
            },
            hazeState = hazeState
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelCard(
    item: AiModelItem,
    progress: Int,
    downloadBlocked: Boolean,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    val model = item.model
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) colors.primaryContainer else colors.surfaceContainerLow
        ),
        border = if (item.fit == ModelFit.RECOMMENDED) BorderStroke(2.dp, colors.primary) else null,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(model.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${AiModelsViewModel.formatSize(model.sizeBytes)} · ${model.license}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                if (item.isSelected) {
                    Text("In use", style = MaterialTheme.typography.labelLarge, color = colors.primary)
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Label(model.trait.label, colors.secondaryContainer, colors.onSecondaryContainer)
                val (bg, fg) = when (item.fit) {
                    ModelFit.RECOMMENDED -> colors.primary to colors.onPrimary
                    ModelFit.RUNS_WELL -> colors.tertiaryContainer to colors.onTertiaryContainer
                    ModelFit.MAY_BE_SLOW -> colors.surfaceVariant to colors.onSurfaceVariant
                    ModelFit.TOO_HEAVY, ModelFit.NOT_ENOUGH_STORAGE -> colors.errorContainer to colors.onErrorContainer
                }
                Label(item.fit.label, bg, fg)
            }

            Text(model.description, style = MaterialTheme.typography.bodyMedium)

            when {
                item.isDownloading -> {
                    LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Downloading… $progress%", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onCancel) { Text("Cancel") }
                    }
                }
                item.isDownloaded -> Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (!item.isSelected) Button(onClick = onSelect) { Text("Use for chat") }
                    OutlinedButton(onClick = onDelete) { Text("Delete") }
                }
                else -> {
                    val blocked = item.fit == ModelFit.TOO_HEAVY || item.fit == ModelFit.NOT_ENOUGH_STORAGE
                    Button(onClick = onDownload, enabled = !blocked && !downloadBlocked) {
                        Text("Download ${AiModelsViewModel.formatSize(model.sizeBytes)}")
                    }
                    if (item.fit == ModelFit.MAY_BE_SLOW) {
                        Text(
                            "Your phone has less RAM than this model likes. It may be slow or close other apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Label(text: String, container: Color, content: Color) {
    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(containerColor = container, labelColor = content),
        border = null,
    )
}
