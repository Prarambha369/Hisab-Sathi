package com.prarambha.hisabsathi.presentation.ui.features.settings.unrecognized

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prarambha.hisabsathi.data.database.entity.UnrecognizedSmsEntity
import com.prarambha.hisabsathi.presentation.effects.overScrollVertical
import com.prarambha.hisabsathi.presentation.effects.rememberOverscrollFlingBehavior
import com.prarambha.hisabsathi.presentation.ui.components.HisabSathiCard
import com.prarambha.hisabsathi.presentation.ui.components.CustomTitleTopAppBar
import com.prarambha.hisabsathi.presentation.ui.components.LoadingCircle
import com.prarambha.hisabsathi.presentation.ui.features.categories.NavigationContent
import com.prarambha.hisabsathi.presentation.ui.icons.Bag
import com.prarambha.hisabsathi.presentation.ui.icons.Iconax
import com.prarambha.hisabsathi.presentation.ui.icons.Information
import com.prarambha.hisabsathi.presentation.ui.icons.Send
import com.prarambha.hisabsathi.presentation.ui.theme.Dimensions
import com.prarambha.hisabsathi.presentation.ui.theme.Spacing
import com.prarambha.parser.core.ParsedTransaction
import com.prarambha.parser.core.SmsShapes
import com.prarambha.parser.core.TapWord
import com.prarambha.parser.core.TransactionType
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
    val matchedTransactions by unrecognizedSmsViewModel.matchedTransactions.collectAsStateWithLifecycle()
    val teachTarget by unrecognizedSmsViewModel.teachTarget.collectAsStateWithLifecycle()
    val userMessage by unrecognizedSmsViewModel.userMessage.collectAsStateWithLifecycle()
    val isLoading by unrecognizedSmsViewModel.isLoading.collectAsStateWithLifecycle()
    val showReported by unrecognizedSmsViewModel.showReported.collectAsStateWithLifecycle()
    var selectedMessage by remember { mutableStateOf<UnrecognizedSmsEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            unrecognizedSmsViewModel.clearUserMessage()
        }
    }

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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
        HisabSathiCard(
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
                          "Help improve HisabSathi by reporting them so we can add support for more banks.",
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
                        matched = matchedTransactions[message.id],
                        onReport = {
                            unrecognizedSmsViewModel.reportMessage(message)
                        },
                        onTeach = {
                            unrecognizedSmsViewModel.startTeaching(message)
                        },
                        onAdd = {
                            matchedTransactions[message.id]?.let { parsed ->
                                unrecognizedSmsViewModel.addMatchedTransaction(message, parsed)
                            }
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

    // Teaching sheet for the message the user chose
    teachTarget?.let { message ->
        TeachSmsDialog(
            message = message,
            onDismiss = { unrecognizedSmsViewModel.cancelTeaching() },
            onSave = { amount, merchant, account, direction, currency ->
                unrecognizedSmsViewModel.saveTaughtShape(amount, merchant, account, direction, currency)
            }
        )
    }
}

@Composable
private fun UnrecognizedSmsItem(
    message: UnrecognizedSmsEntity,
    matched: ParsedTransaction?,
    onReport: () -> Unit,
    onTeach: () -> Unit,
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    HisabSathiCard(
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

            // A reading the user already taught, waiting to be confirmed
            if (matched != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Matches your taught shape",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = buildString {
                                append(matched.currency)
                                append(' ')
                                append(matched.amount.toPlainString())
                                matched.merchant?.let { append(" • ").append(it) }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onAdd,
                        contentPadding = PaddingValues(
                            horizontal = Dimensions.Padding.content,
                            vertical = Spacing.sm
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Add")
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onTeach) {
                    Text("Teach")
                }

                Spacer(modifier = Modifier.width(Spacing.sm))

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

/**
 * Teaching one message: pick the amount, then the merchant and account if the message names
 * them. The picked words are what a later message of the same shape is read with.
 */
@Composable
private fun TeachSmsDialog(
    message: UnrecognizedSmsEntity,
    onDismiss: () -> Unit,
    onSave: (amount: IntRange, merchant: IntRange?, account: IntRange?, direction: TransactionType, currency: String) -> Unit
) {
    val words = remember(message.id) { SmsShapes.tapWords(message.smsBody) }
    val guess = remember(message.id) { SmsShapes.suggest(message.sender, message.smsBody) }
    fun tappedText(range: IntRange) = message.smsBody.substring(range.first, range.last + 1)

    var amount by remember(message.id) { mutableStateOf(guess.amount) }
    var merchant by remember(message.id) { mutableStateOf(guess.merchant) }
    var account by remember(message.id) { mutableStateOf(guess.account) }
    var direction by remember(message.id) { mutableStateOf(guess.direction ?: TransactionType.EXPENSE) }
    val currency = guess.currency ?: "NPR"

    val numbers = words.filter { it.numeric }
    val names = words.filter { !it.numeric }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Teach this message") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = message.smsBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Which number is the amount?", style = MaterialTheme.typography.labelLarge)
                TapWordRow(
                    words = numbers,
                    selected = amount,
                    textOf = { tappedText(it) },
                    onSelect = { amount = it }
                )

                Text("Which words name the merchant? (optional)", style = MaterialTheme.typography.labelLarge)
                TapWordRow(
                    words = names,
                    selected = merchant,
                    textOf = { tappedText(it) },
                    onSelect = { merchant = it }
                )

                Text("Which number is the account? (optional)", style = MaterialTheme.typography.labelLarge)
                TapWordRow(
                    words = numbers.filter { it.range != amount },
                    selected = account,
                    textOf = { tappedText(it) },
                    onSelect = { account = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    FilterChip(
                        selected = direction == TransactionType.EXPENSE,
                        onClick = { direction = TransactionType.EXPENSE },
                        label = { Text("Money out") }
                    )
                    FilterChip(
                        selected = direction == TransactionType.INCOME,
                        onClick = { direction = TransactionType.INCOME },
                        label = { Text("Money in") }
                    )
                }

                Text(
                    text = "Amounts are read as $currency. Nothing is added until you confirm it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amount?.let { onSave(it, merchant, account, direction, currency) } },
                enabled = amount != null
            ) {
                Text("Save shape")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/** One row of tappable words; tapping the selected word again clears the pick. */
@Composable
private fun TapWordRow(
    words: List<TapWord>,
    selected: IntRange?,
    textOf: (IntRange) -> String,
    onSelect: (IntRange?) -> Unit
) {
    // A suggested pick can span several words ("Juice Bar"), which no single chip covers, so
    // it gets a chip of its own; tapping it clears the pick.
    val options = if (selected != null && words.none { it.range == selected }) {
        (words + TapWord(selected, numeric = false)).sortedBy { it.range.first }
    } else {
        words
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        items(items = options, key = { it.range }) { word ->
            FilterChip(
                selected = word.range == selected,
                onClick = { onSelect(if (word.range == selected) null else word.range) },
                label = { Text(textOf(word.range), maxLines = 1) }
            )
        }
    }
}