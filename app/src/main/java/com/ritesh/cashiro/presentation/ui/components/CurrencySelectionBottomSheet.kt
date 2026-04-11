package com.ritesh.cashiro.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionBottomSheet(
    selectedCurrency: String,
    availableCurrencies: List<String>,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
            )

            availableCurrencies.forEachIndexed { index, currency ->
                ListItem(
                    headline = {
                        Text(
                            text = when (currency) {
                                "INR" -> "Indian Rupee (₹)"
                                "USD" -> "US Dollar ($)"
                                "AED" -> "UAE Dirham (AED)"
                                "NPR" -> "Nepalese Rupee (₨)"
                                "ETB" -> "Ethiopian Birr (ብር)"
                                else -> currency
                            }
                        )
                    },
                    trailing = {
                        RadioButton(
                            selected = currency == selectedCurrency,
                            onClick = null
                        )
                    },
                    selected = currency == selectedCurrency,
                    onClick = {
                        onCurrencySelected(currency)
                        onDismiss()
                    },
                    shape = ListItemPosition.from(index, availableCurrencies.size).toShape()
                )
            }
        }
    }
}
