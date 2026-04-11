package com.ritesh.cashiro.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.ritesh.cashiro.data.preferences.AppIcon

object IconSwitchingUtils {
    fun switchAppIcon(context: Context, targetIcon: AppIcon) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        val iconComponents = mapOf(
            AppIcon.ORIGINAL to "$packageName.MainActivityOriginal",
            AppIcon.ANARCHY to "$packageName.MainActivityAnarchy",
            AppIcon.ZENITH to "$packageName.MainActivityZenith"
        )

        // Enable new icon first
        iconComponents[targetIcon]?.let { componentName ->
            packageManager.setComponentEnabledSetting(
                ComponentName(context, componentName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                0 // Hard kill to apply changes
            )
        }

        // Disable others
        iconComponents.filter { it.key != targetIcon }.forEach { (_, componentName) ->
            packageManager.setComponentEnabledSetting(
                ComponentName(context, componentName),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0 // Hard kill to apply changes
            )
        }
    }
}
