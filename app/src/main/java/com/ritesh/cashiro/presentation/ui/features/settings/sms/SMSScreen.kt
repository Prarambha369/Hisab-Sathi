package com.ritesh.cashiro.presentation.ui.features.settings.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritesh.cashiro.presentation.ui.features.categories.NavigationContent
import com.ritesh.cashiro.presentation.ui.components.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.components.ListItem
import com.ritesh.cashiro.presentation.ui.components.ListItemPosition
import com.ritesh.cashiro.presentation.ui.components.toShape
import com.ritesh.cashiro.presentation.effects.overScrollVertical
import com.ritesh.cashiro.presentation.ui.features.settings.SettingsViewModel
import com.ritesh.cashiro.presentation.ui.icons.Clock
import com.ritesh.cashiro.presentation.ui.icons.Iconax
import com.ritesh.cashiro.presentation.ui.theme.Dimensions
import com.ritesh.cashiro.presentation.ui.theme.Spacing
import com.ritesh.cashiro.presentation.ui.theme.blue_dark
import com.ritesh.cashiro.presentation.ui.theme.blue_light
import com.ritesh.cashiro.presentation.ui.theme.cyan_dark
import com.ritesh.cashiro.presentation.ui.theme.cyan_light
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeApi::class
)
@Composable
fun SMSScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUnrecognizedSms: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    blurEffects: Boolean
) {
    val smsScanMonths by settingsViewModel.smsScanMonths.collectAsStateWithLifecycle(initialValue = 3)
    val smsScanAllTime by settingsViewModel.smsScanAllTime.collectAsStateWithLifecycle(initialValue = true)
    val unreportedCount by settingsViewModel.unreportedSmsCount.collectAsStateWithLifecycle()

    var showSmsScanDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                title = "SMS",
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehavior,
                hazeState = hazeState,
                hasBackButton = true,
                navigationContent = { NavigationContent(onNavigateBack) }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .overScrollVertical()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = Dimensions.Padding.content,
                        end = Dimensions.Padding.content,
                        top = Dimensions.Padding.content +
                                paddingValues.calculateTopPadding()
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    // SMS Scan Period
                    ListItem(
                        headline = {
                            Text(
                                text = "SMS Scan Period",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supporting = {
                            Text(
                                text = if (smsScanAllTime) "Scan all SMS messages"
                                else "Scan last $smsScanMonths months of messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = cyan_light,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Iconax.Clock,
                                    contentDescription = null,
                                    tint = cyan_dark,
                                )
                            }
                        },
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.large
                                    )
                                    .clickable(
                                        onClick = { showSmsScanDialog = true }
                                    )
                                    .padding(
                                        horizontal = Spacing.md,
                                        vertical = Spacing.sm
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (smsScanAllTime) "All Time"
                                    else "$smsScanMonths months",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        onClick = { showSmsScanDialog = true },
                        shape = ListItemPosition.Top.toShape(),
                        padding = PaddingValues(0.dp)
                    )

                    // Unrecognized Bank Messages
                    ListItem(
                        headline = {
                            Text(
                                text = "Unrecognized Bank Messages",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supporting = {
                            Text(
                                text = if (unreportedCount > 0)
                                    "$unreportedCount message${if (unreportedCount > 1) "s" else ""} from potential banks"
                                else "No unrecognized messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = blue_light,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.BugReport,
                                    contentDescription = null,
                                    tint = blue_dark
                                )
                            }
                        },
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (unreportedCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(unreportedCount.toString())
                                    }
                                }

                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = { onNavigateToUnrecognizedSms() },
                        shape = ListItemPosition.Bottom.toShape(),
                        padding = PaddingValues(0.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }

        // SMS Scan Period Dialog
        if (showSmsScanDialog) {
            val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            AlertDialog(
                onDismissRequest = { showSmsScanDialog = false },
                title = { Text("SMS Scan Period") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                        Text(
                            text = "Choose how many months of SMS history to scan for transactions",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))

                        val options = listOf(-1) + listOf(1, 2, 3, 6, 12, 24)
                        options.forEachIndexed { index, months ->
                            val isSelected = if (months == -1) smsScanAllTime
                            else smsScanMonths == months && !smsScanAllTime

                            ListItem(
                                headline = {
                                    Text(
                                        text = when (months) {
                                            -1 -> "All Time"
                                            1 -> "1 month"
                                            24 -> "2 years"
                                            else -> "$months months"
                                        }
                                    )
                                },
                                trailing = {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    if (months == -1) {
                                        settingsViewModel.updateSmsScanAllTime(true)
                                        showSmsScanDialog = false
                                    } else {
                                        settingsViewModel.updateSmsScanMonths(months)
                                        settingsViewModel.updateSmsScanAllTime(false)
                                        showSmsScanDialog = false
                                    }
                                },
                                shape = ListItemPosition.from(index, options.size).toShape(),
                                listColor = MaterialTheme.colorScheme.surface.copy(0.5f),
                                padding = PaddingValues(0.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSmsScanDialog = false },
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
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                containerColor = if (blurEffects)
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(0.5f)
                else MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (blurEffects) Modifier.hazeEffect(
                            state = hazeState,
                            block = fun HazeEffectScope.() {
                                style = HazeDefaults.style(
                                    backgroundColor = Color.Transparent,
                                    tint = HazeDefaults.tint(containerColor),
                                    blurRadius = 20.dp,
                                    noiseFactor = -1f,
                                )
                                blurredEdgeTreatment = BlurredEdgeTreatment.Unbounded
                            }
                        ) else Modifier
                    ),
                shape = RoundedCornerShape(16.dp),
            )
        }
    }
}
