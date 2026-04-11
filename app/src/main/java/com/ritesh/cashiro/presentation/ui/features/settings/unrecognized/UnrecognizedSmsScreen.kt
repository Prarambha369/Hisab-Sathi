package com.ritesh.cashiro.presentation.ui.features.settings.unrecognized

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritesh.cashiro.data.database.entity.UnrecognizedSmsEntity
import com.ritesh.cashiro.presentation.effects.overScrollVertical
import com.ritesh.cashiro.presentation.effects.rememberOverscrollFlingBehavior
import com.ritesh.cashiro.presentation.ui.components.CashiroCard
import com.ritesh.cashiro.presentation.ui.components.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.components.LoadingCircle
import com.ritesh.cashiro.presentation.ui.features.categories.NavigationContent
import com.ritesh.cashiro.presentation.ui.icons.Bag
import com.ritesh.cashiro.presentation.ui.icons.Iconax
import com.ritesh.cashiro.presentation.ui.icons.Information
import com.ritesh.cashiro.presentation.ui.icons.Send
import com.ritesh.cashiro.presentation.ui.theme.Dimensions
import com.ritesh.cashiro.presentation.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnrecognizedSmsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    unrecognizedSmsViewModel: UnrecognizedSmsViewModel = hiltViewModel()
) {
    val unrecognizedMessages by unrecognizedSmsViewModel.unrecognizedMessages.collectAsStateWithLifecycle()
    val isLoading by unrecognizedSmsViewModel.isLoading.collectAsStateWithLifecycle()
    val showReported by unrecognizedSmsViewModel.showReported.collectAsStateWithLifecycle()
    var selectedMessage by remember { mutableStateOf<UnrecognizedSmsEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                title = "Unrecognized Messages",
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehavior,
                hazeState = hazeState,
                hasBackButton = true,
                navigationContent = { NavigationContent(onNavigateBack)}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .padding(
                    start = Dimensions.Padding.content,
                    end = Dimensions.Padding.content,
                    bottom = 0.dp,
                    top = Dimensions.Padding.content + paddingValues.calculateTopPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
        // Header card with info
        CashiroCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        Iconax.Information,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Unrecognized Bank Messages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "These messages from potential banks couldn't be automatically parsed. " +
                          "Help improve Cashiro by reporting them so we can add support for more banks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Filter toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FilterChip(
                        selected = showReported,
                        onClick = { unrecognizedSmsViewModel.toggleShowReported() },
                        label = { Text("Show Reported") },
                        leadingIcon = if (showReported) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (unrecognizedMessages.isNotEmpty()) {
                        val reportedCount = unrecognizedMessages.count { it.reported }
                        val unreportedCount = unrecognizedMessages.size - reportedCount
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            if (unreportedCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text("$unreportedCount new")
                                }
                            }
                            if (reportedCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text("$reportedCount reported")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingCircle()
            }
        } else if (unrecognizedMessages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No unrecognized messages",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimensions.Padding.content))
                    .fillMaxSize()
                    .overScrollVertical(),
                flingBehavior = rememberOverscrollFlingBehavior { lazyListState }
            ) {
                items(
                    items = unrecognizedMessages,
                    key = { it.id }
                ) { message ->
                    UnrecognizedSmsItem(
                        message = message,
                        onReport = {
                            unrecognizedSmsViewModel.reportMessage(message)
                        },
                        onDelete = {
                            selectedMessage = message
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }
    }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                selectedMessage = null
            },
            title = { Text("Delete Message") },
            text = { 
                Text("Are you sure you want to delete this unrecognized message? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMessage?.let { unrecognizedSmsViewModel.deleteMessage(it) }
                        showDeleteConfirmation = false
                        selectedMessage = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        selectedMessage = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UnrecognizedSmsItem(
    message: UnrecognizedSmsEntity,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashiroCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Header with sender and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message.receivedAt.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (message.reported) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "Reported",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            // Message content (truncated)
            Text(
                text = message.smsBody,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!message.reported) {
                    TextButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            Iconax.Bag,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Delete")
                    }
                    
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    
                    Button(
                        onClick = onReport,
                        contentPadding = PaddingValues(
                            horizontal = Dimensions.Padding.content,
                            vertical = Spacing.sm
                        )
                    ) {
                        Icon(
                            Iconax.Send,
                            contentDescription = "Report",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Report")
                    }
                } else {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Iconax.Bag,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Delete")
                    }
                }
            }
        }
    }
}