package com.ritesh.cashiro.presentation.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility

@Composable
fun PreferenceSwitch(
    visible: Boolean = true,
    title: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: @Composable () -> Unit = {},
    padding: PaddingValues = listItemPadding,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isSingle: Boolean = false
) {
    BlurredAnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ListItem(
            headline = { Text(title) },
            supporting = { if (subtitle.isNotEmpty())Text(subtitle) },
            leading = {
                leadingIcon()
            },
            trailing = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    thumbContent = {
                        Icon(
                            if (checked) Icons.Outlined.Check else Icons.Outlined.Close,
                            "Thumb",
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    },
                )
            },
            shape = when {
                isSingle -> ListItemPosition.Single.toShape()
                isFirst -> ListItemPosition.Top.toShape()
                isLast -> ListItemPosition.Bottom.toShape()
                else -> ListItemPosition.Middle.toShape()
            },
            padding = padding
        )
    }
}