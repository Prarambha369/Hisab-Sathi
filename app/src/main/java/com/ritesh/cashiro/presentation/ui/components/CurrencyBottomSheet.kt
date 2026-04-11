package com.ritesh.cashiro.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ritesh.cashiro.data.currency.model.CurrencyConversion
import com.ritesh.cashiro.data.model.Currency
import com.ritesh.cashiro.presentation.accounts.CurrencyViewModel
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.effects.overScrollVertical
import com.ritesh.cashiro.presentation.effects.rememberOverscrollFlingBehavior
import com.ritesh.cashiro.presentation.ui.theme.Dimensions
import com.ritesh.cashiro.presentation.ui.icons.CloseCircle
import com.ritesh.cashiro.presentation.ui.icons.Iconax
import com.ritesh.cashiro.presentation.ui.icons.Information
import com.ritesh.cashiro.presentation.ui.icons.Search
import com.ritesh.cashiro.presentation.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CurrencyBottomSheet(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: CurrencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showAllCurrencies by rememberSaveable { mutableStateOf(false) }
    var showExchangeRateInfo by rememberSaveable { mutableStateOf(false) }
    var showExchangeRateSheet by rememberSaveable { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val exchangeRatesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Currencies",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                // Search Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBarBox(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        label = { Text("Search currencies...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Iconax.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.text.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                                    Icon(
                                        imageVector = Iconax.CloseCircle,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                    )
                                }
                            } else {
                                IconButton(onClick = { showExchangeRateInfo = true }) {
                                    Icon(
                                        imageVector = Iconax.Information,
                                        contentDescription = "Info",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = Spacing.md).padding(bottom = 16.dp)
                    )
                }


                // Scrollable List Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            state = scrollState,
                            flingBehavior = rememberOverscrollFlingBehavior { scrollState }
                        )
                ) {
                    val sourceCurrencies =
                        (if (uiState.currencies.isNotEmpty()) uiState.currencies else Currency.SUPPORTED_CURRENCIES)
                            .sortedBy { it.name }
                    val filteredCurrencies = sourceCurrencies.filter {
                        it.code.contains(searchQuery.text, ignoreCase = true) ||
                                it.name.contains(searchQuery.text, ignoreCase = true) ||
                                it.symbol.contains(searchQuery.text, ignoreCase = true)
                    }

                    if (filteredCurrencies.isEmpty()) {
                        Text(
                            text = "Currency not available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        // Display currencies in a FlowRow
                        val displayedCurrencies =
                            if (showAllCurrencies || searchQuery.text.isNotEmpty()) {
                                filteredCurrencies
                            } else {
                                filteredCurrencies.filter { currency ->
                                    Currency.POPULAR_CURRENCY_CODES.contains(currency.code)
                                }.take(15)
                            }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Dimensions.Radius.md)),
                            maxItemsInEachRow = 3,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            displayedCurrencies.forEach { currency ->
                                CurrencyCard(
                                    currency = currency,
                                    isSelected = currency.code.equals(
                                        selectedCurrency,
                                        ignoreCase = true
                                    ),
                                    onCurrencyCardClick = {
                                        scope.launch { sheetState.hide() }
                                            .invokeOnCompletion {
                                                if (!sheetState.isVisible) {
                                                    onCurrencySelected(currency.code)
                                                }
                                            }
                                    }
                                )
                            }
                        }

                        // View All Currencies button
                        if (!showAllCurrencies && searchQuery.text.isEmpty() && filteredCurrencies.size > 15) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { showAllCurrencies = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.inverseSurface.copy(
                                            0.5f
                                        )
                                    ),
                                    shapes = ButtonDefaults.shapes()
                                ) {
                                    Text(
                                        text = "View All Currencies",
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                shape = RoundedCornerShape(15.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Exchange Rate Info Dialog
            BlurredAnimatedVisibility(
                visible = showExchangeRateInfo,
                enter = fadeIn() + scaleIn(
                    animationSpec = tween(durationMillis = 300),
                    initialScale = 0f,
                ),
                exit = fadeOut() + scaleOut(
                    animationSpec = tween(durationMillis = 100),
                    targetScale = 0f,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showExchangeRateInfo = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 25.dp,
                                RoundedCornerShape(15.dp),
                                clip = true,
                                spotColor = Color.Black,
                                ambientColor = Color.Black
                            )
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(15.dp)
                            )
                            .clickable(onClick = {})
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary.copy(0.5f),
                                modifier = Modifier.size(50.dp)
                            )
                            Text(
                                text = "Exchange Rates Notice",
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.inverseSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "The exchange rates displayed within this app are for informational purposes only and should not be used for investment decisions. These rates are estimates and may not reflect actual rates. By using this app you acknowledge that you understand and accept these limitations.",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { showExchangeRateInfo = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        contentColor = MaterialTheme.colorScheme.inverseSurface
                                    ),
                                ) {
                                    Text(text = "Ok")
                                }
                                Button(
                                    onClick = {
                                        showExchangeRateSheet = true
                                        showExchangeRateInfo = false
                                        viewModel.loadConversions(selectedCurrency)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                ) {
                                    Text(text = "Exchange Rates", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }

            if (showExchangeRateSheet) {
                ExchangeRatesBottomSheet(
                    uiState = uiState,
                    onDismiss = { showExchangeRateSheet = false },
                    sheetState = exchangeRatesSheetState
                )
            }
        }
    }
}

@Composable
fun CurrencyCard(
    currency: Currency,
    isSelected: Boolean = false,
    onCurrencyCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .sizeIn(minWidth = 90.dp, minHeight = 70.dp, maxHeight = 90.dp, maxWidth = 110.dp)
            .clip(RoundedCornerShape(Dimensions.Radius.md))
            .clickable(onClick = onCurrencyCardClick)
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.inverseSurface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currency.code.uppercase(),
                lineHeight = 12.sp,
                fontSize = 12.sp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                else
                    MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
            )
            Text(
                text = currency.symbol,
                lineHeight = 20.sp,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Text(
                text = currency.name,
                lineHeight = 10.sp,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                else
                    MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 1000,
                        velocity = 30.dp
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRatesBottomSheet(
    uiState: CurrencyViewModel.CurrencyUiState,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val filteredConversions = uiState.conversions.filter {
        it.currencyCode.contains(searchQuery.text, ignoreCase = true) ||
                it.symbol.contains(searchQuery.text, ignoreCase = true)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Exchange Rates",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(12.dp))

            SearchBarBox(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                label = { Text("Search currencies...") },
                leadingIcon = {
                    Icon(
                        imageVector = Iconax.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.isLoadingConversions) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.conversionError != null) {
                Text(
                    text = uiState.conversionError,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    item {
                        Text(
                            text = "Rates relative to ${uiState.selectedCurrency?.code ?: "Base"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                    itemsIndexed(filteredConversions) { index, conversion ->
                        ExchangeRateItem(
                            conversion = conversion,
                            baseCurrency = uiState.selectedCurrency,
                            isFirst = index == 0,
                            isLast = index == filteredConversions.size - 1
                        )
                    }

                    if (uiState.lastUpdated > 0) {
                        item {
                            val formatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
                            val dateStr = remember(uiState.lastUpdated) { formatter.format(Date(uiState.lastUpdated * 1000)) }
                            Text(
                                text = "Last updated: $dateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ExchangeRateItem(
    conversion: CurrencyConversion,
    baseCurrency: Currency?,
    isFirst: Boolean,
    isLast: Boolean
) {
    val baseSymbol = baseCurrency?.symbol ?: ""
    
    ListItem(
        headlineContent = {
            Text(
                text = "${conversion.currencyCode} (${conversion.symbol})",
                fontWeight = FontWeight.Bold,
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
            )
        },
        supportingContent = {
            Text(
                text = "1 $baseSymbol = ${conversion.displayRate} ${conversion.symbol}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = if (isFirst) 16.dp else 0.dp,
                    topEnd = if (isFirst) 16.dp else 0.dp,
                    bottomStart = if (isLast) 16.dp else 0.dp,
                    bottomEnd = if (isLast) 16.dp else 0.dp
                )
            )
    )
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}
