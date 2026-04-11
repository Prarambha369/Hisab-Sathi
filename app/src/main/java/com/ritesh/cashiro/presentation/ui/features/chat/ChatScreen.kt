package com.ritesh.cashiro.presentation.ui.features.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritesh.cashiro.data.database.entity.ChatMessage
import com.ritesh.cashiro.data.repository.ModelState
import com.ritesh.cashiro.presentation.effects.overScrollVertical
import com.ritesh.cashiro.presentation.effects.rememberOverscrollFlingBehavior
import com.ritesh.cashiro.presentation.ui.components.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.components.LoadingCircle
import com.ritesh.cashiro.presentation.ui.components.LoadingLine
import com.ritesh.cashiro.presentation.ui.features.categories.NavigationContent
import com.ritesh.cashiro.presentation.ui.icons.Iconax
import com.ritesh.cashiro.presentation.ui.icons.Send
import com.ritesh.cashiro.presentation.ui.theme.Dimensions
import com.ritesh.cashiro.presentation.ui.theme.Spacing
import com.ritesh.cashiro.utils.TokenUtils
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val modelState by chatViewModel.modelState.collectAsStateWithLifecycle()
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val currentResponse by chatViewModel.currentResponse.collectAsStateWithLifecycle()
    val isDeveloperMode by chatViewModel.isDeveloperModeEnabled.collectAsStateWithLifecycle()
    val chatStats by chatViewModel.chatStats.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var showMenu by remember { mutableStateOf(false) }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, currentResponse) {
        if (messages.isNotEmpty() || currentResponse.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(
                    index = if (currentResponse.isNotEmpty()) messages.size else messages.size - 1
                )
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                title = "Cashiro AI",
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehavior,
                hazeState = hazeState,
                hasBackButton = true,
                hasActionButton = true,
                navigationContent = {NavigationContent(onNavigateBack)},
                actionContent = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            shapes =  IconButtonDefaults.shapes(),
                            modifier = Modifier.padding(end =Spacing.md)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "More options",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                onClick = {
                                    showMenu = false
                                    chatViewModel.clearChat()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null)
                                },
                                enabled = messages.isNotEmpty()
                            )
                        }
                    }
                },
                extraInfoCard = {
                    // Developer info card
                    if(isDeveloperMode && messages.isNotEmpty()) {
                        DeveloperInfoCard(
                            chatStats = chatStats,
                            modifier = Modifier.padding(end =Spacing.md)
                        )
                    }
                }
            )
        },

    ) { paddingValues ->
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            when (modelState) {
                ModelState.NOT_DOWNLOADED, ModelState.ERROR -> {
                    // Show existing messages if any, but disable input
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding())
                    ) {
                        // If no messages, show the download prompt centered
                        if (messages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                                ) {
                                    Icon(
                                        Icons.Rounded.CloudDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Qwen Model Required",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "Download the AI model from Settings to start chatting",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(onClick = onNavigateToSettings) {
                                        Text("Go to Settings")
                                    }
                                }
                            }
                        } else {
                            // Show existing messages (read-only)
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .overScrollVertical(),
                                flingBehavior = rememberOverscrollFlingBehavior { listState },
                                contentPadding = PaddingValues(
                                    start = Dimensions.Padding.content,
                                    end = Dimensions.Padding.content,
                                    top = Dimensions.Padding.content,
                                    bottom = Spacing.lg
                                ),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                items(messages) { message ->
                                    ChatMessageItem(message = message)
                                }
                            }
                        }

                        // Show model required banner at bottom
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            tonalElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.Padding.content),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Model Required",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Download to continue chatting",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Button(
                                    onClick = onNavigateToSettings,
                                    modifier = Modifier.padding(start = Spacing.sm)
                                ) {
                                    Text("Download")
                                }
                            }
                        }
                    }
                }

                ModelState.DOWNLOADING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            LoadingCircle(modifier = Modifier.size(64.dp))
                            Text(
                                text = "Downloading Model... ${uiState.downloadProgress}%",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            LoadingLine(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Spacing.xs)
                                    .clip(RoundedCornerShape(Spacing.xs)),
                                progress = uiState.downloadProgress / 100f
                            )
                            Text(
                                text = "Check Settings for more details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                ModelState.READY, ModelState.LOADING -> {
                    // Show loading overlay when model is loading
                    if (modelState == ModelState.LOADING) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                LoadingCircle()
                                Text(
                                    text = "Initializing AI Model...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "This may take a few seconds",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ){
                            Column{
                                // Messages list
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip( CardDefaults.shape)
                                        .overScrollVertical(),
                                    flingBehavior = rememberOverscrollFlingBehavior { listState },
                                    contentPadding = PaddingValues(
                                        start = Dimensions.Padding.content,
                                        end = Dimensions.Padding.content,
                                        top = Dimensions.Padding.content + paddingValues.calculateTopPadding(),
                                        bottom = Dimensions.Padding.content
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    reverseLayout = false
                                ) {
                                    items(messages) { message ->
                                        ChatMessageItem(message = message)
                                    }

                                    // Show streaming response if available
                                    if (currentResponse.isNotEmpty()) {
                                        item {
                                            ChatMessageItem(
                                                message = ChatMessage(
                                                    message = currentResponse,
                                                    isUser = false,
                                                    timestamp = System.currentTimeMillis()
                                                ),
                                                isStreaming = true
                                            )
                                        }
                                    } else if (uiState.isLoading) {
                                        // Show typing indicator while waiting for response
                                        item {
                                            TypingIndicator()
                                        }
                                    }
                                    item{
                                        Spacer(modifier = Modifier.height(180.dp))
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            ) {
                                // Error message
                                AnimatedVisibility(
                                    visible = uiState.error != null,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Dimensions.Padding.content),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(Dimensions.Padding.content),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = uiState.error ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = { chatViewModel.clearError() }) {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = "Dismiss",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                }

                                // Token limit warning
                                AnimatedVisibility(
                                    visible = chatStats.contextUsagePercent >= 80,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    TokenLimitWarning(
                                        usagePercent = chatStats.contextUsagePercent,
                                        onClearChat = { chatViewModel.clearChat() }
                                    )
                                }

                                // Input field
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.surface,
                                                )
                                            )
                                        )
                                        .navigationBarsPadding(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Dimensions.Padding.content),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        TextField(
                                            value = inputText,
                                            onValueChange = { inputText = it },
                                            placeholder = { Text("Ask about your expenses...") },
                                            enabled = !uiState.isLoading,
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .focusRequester(focusRequester),
                                            shape = RoundedCornerShape(Spacing.xxl),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    0.7f
                                                )
                                            ),
                                            trailingIcon = {
                                                FilledIconButton(
                                                    onClick = {
                                                        chatViewModel.sendMessage(inputText)
                                                        inputText = ""
                                                        // Keep keyboard open by requesting focus
                                                        focusRequester.requestFocus()
                                                    },
                                                    enabled = inputText.isNotBlank() && !uiState.isLoading,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .padding(Spacing.xs)
                                                ) {
                                                    if (uiState.isLoading) {
                                                        LoadingCircle(
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    } else {
                                                        Icon(
                                                            Iconax.Send,
                                                            contentDescription = "Send",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenLimitWarning(
    usagePercent: Int,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        usagePercent >= 95 -> MaterialTheme.colorScheme.errorContainer
        usagePercent >= 90 -> Color(0xFFFFF3E0) // Orange container
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    val contentColor = when {
        usagePercent >= 95 -> MaterialTheme.colorScheme.onErrorContainer
        usagePercent >= 90 -> Color(0xFF5D4037) // Dark orange
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    val icon = when {
        usagePercent >= 95 -> Icons.Rounded.Error
        else -> Icons.Rounded.Warning
    }
    
    val message = when {
        usagePercent >= 95 -> "Chat memory almost full! Clear chat to continue."
        usagePercent >= 90 -> "Chat memory is ${usagePercent}% full. Consider clearing soon."
        else -> "Chat memory is ${usagePercent}% full."
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.content),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
            if (usagePercent >= 90) {
                TextButton(
                    onClick = onClearChat,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text("Clear", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DeveloperInfoCard(
    chatStats: ChatStats,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val usageHint = remember(chatStats.contextUsagePercent) {
        TokenUtils.getUsageColorHint(chatStats.contextUsagePercent)
    }
    val usageColor = when (usageHint) {
        "critical" -> MaterialTheme.colorScheme.error
        "warning" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.Padding.content)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Qwen 2.5 • ${chatStats.messageCount} messages",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${TokenUtils.formatNumber(chatStats.estimatedTokens)} tokens",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = usageColor
                    )
                    Icon(
                        if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    
                    // Context usage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Context Usage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${chatStats.contextUsagePercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = usageColor
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { chatStats.contextUsagePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = usageColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {}
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${TokenUtils.formatNumber(chatStats.estimatedTokens)} / ${TokenUtils.formatNumber(chatStats.maxTokens)} tokens",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (chatStats.systemPromptTokens > 0) {
                            Text(
                                text = "System: ${TokenUtils.formatNumber(chatStats.systemPromptTokens)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = Dimensions.Padding.content,
                    vertical = Spacing.md
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Three animated dots
                val infiniteTransition = rememberInfiniteTransition(label = "typing")
                
                for (i in 0..2) {
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 1200
                                0.3f at 0
                                1f at 400
                                0.3f at 800
                            },
                            repeatMode = RepeatMode.Restart,
                            initialStartOffset = StartOffset(i * 200)
                        ),
                        label = "dot_alpha_$i"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = alpha),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isStreaming: Boolean = false
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.Padding.content)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (isStreaming) {
                        LoadingCircle(
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isUser)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
