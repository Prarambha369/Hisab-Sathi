package com.ritesh.cashiro.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.domain.model.rule.TransactionRule
import com.ritesh.cashiro.domain.usecase.BatchApplyResult
import com.ritesh.cashiro.presentation.ui.theme.Dimensions
import com.ritesh.cashiro.presentation.ui.theme.LocalBlurEffects
import com.ritesh.cashiro.presentation.ui.theme.Spacing
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalHazeApi::class)
@Composable
fun RulesResetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    blurEffects: Boolean = LocalBlurEffects.current,
    hazeState: HazeState = remember { HazeState() },
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Rules") },
        text = { Text("Reset all rules to default settings? Your custom settings will be lost.") },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(
                            topStart = Dimensions.Radius.xxl,
                            topEnd = Dimensions.Radius.xs,
                            bottomStart = Dimensions.Radius.xxl,
                            bottomEnd = Dimensions.Radius.xs
                        ),
                        modifier = Modifier
                            .padding(start = Spacing.xl)
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(
                            topStart = Dimensions.Radius.xs,
                            topEnd = Dimensions.Radius.xxl,
                            bottomStart = Dimensions.Radius.xs,
                            bottomEnd = Dimensions.Radius.xxl
                        ),
                        modifier = Modifier
                            .padding(end = Spacing.xl)
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        containerColor = if (blurEffects) MaterialTheme.colorScheme.surfaceContainerLow.copy(0.5f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        dismissButton = {},
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.Radius.md))
            .then(
            if (blurEffects) Modifier.hazeEffect(
                state = hazeState,
                block = fun HazeEffectScope.() {
                    style = HazeDefaults.style(
                        backgroundColor = Color.Transparent,
                        tint = HazeDefaults.tint(containerColor),
                        blurRadius = 18.dp,
                        noiseFactor = -1f,
                    )
                    blurredEdgeTreatment = BlurredEdgeTreatment.Unbounded
                }
            ) else Modifier
        ),
        shape = MaterialTheme.shapes.large
    )
}

@OptIn(ExperimentalHazeApi::class)
@Composable
fun RulesDeleteDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    rule: TransactionRule,
    blurEffects: Boolean = LocalBlurEffects.current,
    hazeState: HazeState = remember { HazeState() },
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Rule") },
        text = { Text("Delete \"${rule.name}\"? This action cannot be undone.") },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(
                            topStart = Dimensions.Radius.xxl,
                            topEnd = Dimensions.Radius.xs,
                            bottomStart = Dimensions.Radius.xxl,
                            bottomEnd = Dimensions.Radius.xs
                        ),
                        modifier = Modifier
                            .padding(start = Spacing.xl)
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(
                            topStart = Dimensions.Radius.xs,
                            topEnd = Dimensions.Radius.xxl,
                            bottomStart = Dimensions.Radius.xs,
                            bottomEnd = Dimensions.Radius.xxl
                        ),
                        modifier = Modifier
                            .padding(end = Spacing.xl)
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        containerColor = if (blurEffects) MaterialTheme.colorScheme.surfaceContainerLow.copy(0.5f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        dismissButton = {},
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.Radius.md))
            .then(
            if (blurEffects) Modifier.hazeEffect(
                state = hazeState,
                block = fun HazeEffectScope.() {
                    style = HazeDefaults.style(
                        backgroundColor = Color.Transparent,
                        tint = HazeDefaults.tint(containerColor),
                        blurRadius = 18.dp,
                        noiseFactor = -1f,
                    )
                    blurredEdgeTreatment = BlurredEdgeTreatment.Unbounded
                }
            ) else Modifier
        ),
        shape = MaterialTheme.shapes.large
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeApi::class)
@Composable
fun RulesBatchApplyDialog(
    rule: TransactionRule,
    progress: Pair<Int, Int>?,
    result: BatchApplyResult?,
    onDismiss: () -> Unit,
    onApplyToAll: () -> Unit,
    onApplyToUncategorized: () -> Unit,
    blurEffects: Boolean = LocalBlurEffects.current,
    hazeState: HazeState = remember { HazeState() },
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (progress != null) "Applying Rule..." else "Apply Rule to Past Transactions")
                if(progress == null && result == null) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(0.5f),
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                if (progress == null && result == null) {
                    // Initial state - show options
                    Text(
                        text = "Apply \"${rule.name}\" to existing transactions?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(0.5f),
                        ),
                        shape = RoundedCornerShape(Dimensions.Radius.sm),
                    ){
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            Text(
                                text = "Choose how to apply this rule:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• All - Apply to every transaction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                            )
                            Text(
                                text = "• Uncategorized - Skip already categorized transactions (Recommended)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                            )
                        }
                    }
                } else if (progress != null) {
                    // Processing state - show progress
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Processing ${progress.first} of ${progress.second} transactions",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (result != null) {
                    // Result state - show summary
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (result.errors.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (result.errors.isEmpty())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        HorizontalDivider()

                        Text(
                            text = "Transactions processed: ${result.totalProcessed}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Transactions updated: ${result.totalUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        if (result.totalDeleted > 0) {
                            Text(
                                text = "Transactions blocked (soft deleted): ${result.totalDeleted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (result.errors.isNotEmpty()) {
                            Text(
                                text = "Errors: ${result.errors.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (progress == null && result == null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                    ) {
                        Button(
                            onClick = onApplyToUncategorized,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            shape = RoundedCornerShape(
                                topStart = Dimensions.Radius.xxl,
                                topEnd = Dimensions.Radius.xs,
                                bottomStart = Dimensions.Radius.xxl,
                                bottomEnd = Dimensions.Radius.xs
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Uncategorized",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Button(
                            onClick = onApplyToAll,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(
                                topStart = Dimensions.Radius.xs,
                                topEnd = Dimensions.Radius.xxl,
                                bottomStart = Dimensions.Radius.xs,
                                bottomEnd = Dimensions.Radius.xxl
                            ),
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else if (result != null) {
                // Done - show close button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .padding(horizontal = Spacing.xl)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        containerColor = if (blurEffects) MaterialTheme.colorScheme.surfaceContainerLow.copy(0.5f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.Radius.md))
            .then(
            if (blurEffects) Modifier.hazeEffect(
                state = hazeState,
                block = fun HazeEffectScope.() {
                    style = HazeDefaults.style(
                        backgroundColor = Color.Transparent,
                        tint = HazeDefaults.tint(containerColor),
                        blurRadius = 18.dp,
                        noiseFactor = -1f,
                    )
                    blurredEdgeTreatment = BlurredEdgeTreatment.Unbounded
                }
            ) else Modifier
        ),
        shape = MaterialTheme.shapes.large
    )
}