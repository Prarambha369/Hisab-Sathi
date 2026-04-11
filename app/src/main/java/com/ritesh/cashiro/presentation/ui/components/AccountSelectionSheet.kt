package com.ritesh.cashiro.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.data.database.entity.AccountBalanceEntity
import com.ritesh.cashiro.presentation.effects.overScrollVertical
import com.ritesh.cashiro.presentation.effects.rememberOverscrollFlingBehavior
import com.ritesh.cashiro.presentation.ui.theme.Spacing

@Composable
fun AccountSelectionSheet(
    accounts: List<AccountBalanceEntity>,
    selectedAccount: AccountBalanceEntity?,
    title: String = "Select Account",
    onAccountSelected: (AccountBalanceEntity?) -> Unit,
    isTransitioning: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp).fillMaxWidth()
        )

        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No accounts found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .overScrollVertical()
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(28.dp)),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = !isTransitioning
            ) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
                item {
                    // Option to deselect/None
                    Surface(
                        onClick = { onAccountSelected(null) },
                        shape = RoundedCornerShape(24.dp),
                        color = if (selectedAccount == null) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        border = if (selectedAccount == null) null
                        else BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "None (Manual Entry)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                items(accounts) { account ->
                    val isSelected = selectedAccount?.id == account.id
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AccountCard(
                            account = account,
                            showMoreOptions = false,
                            onClick = {
                                onAccountSelected(account)
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}
