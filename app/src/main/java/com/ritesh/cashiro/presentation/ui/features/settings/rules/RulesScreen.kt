package com.ritesh.cashiro.presentation.ui.features.settings.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritesh.cashiro.domain.model.rule.TransactionRule
import com.ritesh.cashiro.presentation.effects.overScrollVertical
import com.ritesh.cashiro.presentation.effects.rememberOverscrollFlingBehavior
import com.ritesh.cashiro.presentation.ui.components.CashiroCard
import com.ritesh.cashiro.presentation.ui.components.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.components.LoadingCircle
import com.ritesh.cashiro.presentation.ui.components.RulesBatchApplyDialog
import com.ritesh.cashiro.presentation.ui.components.RulesDeleteDialog
import com.ritesh.cashiro.presentation.ui.components.RulesResetDialog
import com.ritesh.cashiro.presentation.ui.components.SectionHeader
import com.ritesh.cashiro.presentation.ui.features.categories.NavigationContent
import com.ritesh.cashiro.presentation.ui.icons.Bag
import com.ritesh.cashiro.presentation.ui.icons.Edit2
import com.ritesh.cashiro.presentation.ui.icons.History
import com.ritesh.cashiro.presentation.ui.icons.Iconax
import com.ritesh.cashiro.presentation.ui.theme.Dimensions
import com.ritesh.cashiro.presentation.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RulesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateRule: () -> Unit,
    onEditRule: (TransactionRule) -> Unit,
    rulesViewModel: RulesViewModel = hiltViewModel(),
    blurEffects: Boolean
) {
    val rules by rulesViewModel.rules.collectAsStateWithLifecycle()
    val uiState by rulesViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.isLoading
    val batchApplyProgress = uiState.batchApplyProgress
    val batchApplyResult = uiState.batchApplyResult

    var showBatchApplyDialog by remember { mutableStateOf(false) }
    var selectedRuleForBatch by remember { mutableStateOf<TransactionRule?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                title = "Smart Rules",
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehavior,
                hazeState = hazeState,
                hasBackButton = true,
                hasActionButton = true,
                navigationContent = { NavigationContent(onNavigateBack) },
                actionContent = {
                    // Add reset button for advanced users
                    var showResetDialog by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showResetDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shapes =  IconButtonDefaults.shapes(),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Reset to defaults"
                        )
                    }

                    if (showResetDialog) {
                        RulesResetDialog(
                            onDismiss = { showResetDialog = false },
                            onConfirm = {
                                rulesViewModel.resetToDefaults()
                                showResetDialog = false
                            },
                            blurEffects = blurEffects,
                            hazeState = hazeState
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRule,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Rule")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                LoadingCircle()
            }
        } else {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .overScrollVertical(),
                contentPadding = PaddingValues(
                    start = Dimensions.Padding.content,
                    end = Dimensions.Padding.content,
                    top = Dimensions.Padding.content + paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                ),
                state = lazyListState,
                flingBehavior = rememberOverscrollFlingBehavior { lazyListState },

                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Info Card
                item{
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.Padding.content),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                text = "Automatic Categorization",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Enable rules to automatically categorize your transactions based on patterns",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f)
                            )
                        }
                    }
                }
                }

                item {// Group rules by category for better organization
                    val groupedRules = rules.groupBy { rule ->
                        when {
                            rule.name.contains("Food", ignoreCase = true) ||
                                    rule.name.contains(
                                        "Fuel",
                                        ignoreCase = true
                                    ) -> "Daily Expenses"

                            rule.name.contains("Salary", ignoreCase = true) ||
                                    rule.name.contains(
                                        "Cashback",
                                        ignoreCase = true
                                    ) -> "Income & Cashback"

                            rule.name.contains("Rent", ignoreCase = true) ||
                                    rule.name.contains("EMI", ignoreCase = true) ||
                                    rule.name.contains(
                                        "Subscription",
                                        ignoreCase = true
                                    ) -> "Recurring Payments"

                            rule.name.contains("Investment", ignoreCase = true) ||
                                    rule.name.contains(
                                        "Transfer",
                                        ignoreCase = true
                                    ) -> "Banking & Investments"

                            rule.name.contains("Healthcare", ignoreCase = true) -> "Healthcare"

                            else -> "Other"
                        }
                    }

                    groupedRules.forEach { (category, categoryRules) ->
                        if (categoryRules.isNotEmpty()) {
                            SectionHeader(
                                title = category,
                                modifier = Modifier.padding(Spacing.md)
                            )

                            categoryRules.forEach { rule ->
                                RuleCard(
                                    rule = rule,
                                    onToggle = { isActive ->
                                        rulesViewModel.toggleRule(rule.id, isActive)
                                    },
                                    onDelete = {
                                        rulesViewModel.deleteRule(rule.id)
                                    },
                                    onApplyToPast = {
                                        selectedRuleForBatch = rule
                                        showBatchApplyDialog = true
                                    },
                                    onEdit = { onEditRule(rule) },
                                    blurEffects = blurEffects,
                                    hazeState = hazeState
                                )
                            }
                        }
                    }
                }

                // Help text at the bottom
                item {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text(
                        text = "Rules are applied automatically to new transactions. Higher priority rules run first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                }
            }
        }
    }

    // Batch Apply Dialog
    if (showBatchApplyDialog && selectedRuleForBatch != null) {
        RulesBatchApplyDialog(
            rule = selectedRuleForBatch!!,
            progress = batchApplyProgress,
            result = batchApplyResult,
            onDismiss = {
                showBatchApplyDialog = false
                selectedRuleForBatch = null
                rulesViewModel.clearBatchApplyResult()
            },
            onApplyToAll = {
                rulesViewModel.applyRuleToPastTransactions(selectedRuleForBatch!!, applyToUncategorizedOnly = false)
            },
            onApplyToUncategorized = {
                rulesViewModel.applyRuleToPastTransactions(selectedRuleForBatch!!, applyToUncategorizedOnly = true)
            },
            blurEffects = blurEffects,
            hazeState = hazeState
        )
    }
}

@Composable
private fun RuleCard(
    rule: TransactionRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onApplyToPast: () -> Unit,
    onEdit: () -> Unit,
    blurEffects: Boolean,
    hazeState: HazeState = remember { HazeState() }
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    CashiroCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = rule.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )

                rule.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Show simple condition summary
                val conditionSummary = when {
                    rule.name.contains("Small Payments", ignoreCase = true) -> "Amount < 200"
                    rule.name.contains("UPI Cashback", ignoreCase = true) -> "Amount < 10 from NPCI"
                    rule.name.contains("Salary", ignoreCase = true) -> "Credits with salary keywords"
                    rule.name.contains("Rent", ignoreCase = true) -> "Payments with rent keywords"
                    rule.name.contains("EMI", ignoreCase = true) -> "EMI/loan keywords"
                    rule.name.contains("Investment", ignoreCase = true) -> "Mutual funds, stocks keywords"
                    rule.name.contains("Subscription", ignoreCase = true) -> "Netflix, Spotify, etc."
                    rule.name.contains("Fuel", ignoreCase = true) -> "Petrol pump transactions"
                    rule.name.contains("Healthcare", ignoreCase = true) -> "Hospital, pharmacy keywords"
                    rule.name.contains("Transfer", ignoreCase = true) -> "Self transfers, contra"
                    else -> null
                }

                conditionSummary?.let {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Priority badge (only show for non-default priority)
                if (rule.priority != 100) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Priority: ${rule.priority}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // More actions menu - only show when rule is active
                if (rule.isActive) {
                    Box {
                        IconButton(
                            onClick = { showActionsMenu = true }
                        ) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "More actions"
                            )
                        }

                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false },
                            shape = MaterialTheme.shapes.large
                        ) {
                             // Edit rule
                            DropdownMenuItem(
                                text = { Text("Edit Rule") },
                                leadingIcon = {
                                    Icon(
                                        Iconax.Edit2,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                onClick = {
                                    showActionsMenu = false
                                    onEdit()
                                },
                            )
                            HorizontalDivider(
                                thickness = 1.5.dp,
                                color = MaterialTheme.colorScheme.surface
                            )

                            // Apply to past transactions
                            DropdownMenuItem(
                                text = { Text("Apply to Past Transactions") },
                                leadingIcon = {
                                    Icon(
                                        Iconax.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp))
                                },
                                onClick = {
                                    showActionsMenu = false
                                    onApplyToPast()
                                },
                            )


                            // Only show delete for custom rules
                            if (!rule.isSystemTemplate) {
                                HorizontalDivider(
                                    thickness = 1.5.dp,
                                    color = MaterialTheme.colorScheme.surface
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Delete",
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Iconax.Bag,
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showActionsMenu = false
                                        showDeleteDialog = true
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.error,
                                        leadingIconColor = MaterialTheme.colorScheme.error,
                                    )
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = rule.isActive,
                    onCheckedChange = onToggle
                )
            }
        }
    }

    if (showDeleteDialog) {
        RulesDeleteDialog(
            rule = rule,
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                onDelete()
                showDeleteDialog = false
            },
            blurEffects = blurEffects,
            hazeState = hazeState
        )
    }
}