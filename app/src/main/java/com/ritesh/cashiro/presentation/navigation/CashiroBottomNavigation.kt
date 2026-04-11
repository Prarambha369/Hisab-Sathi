package com.ritesh.cashiro.presentation.navigation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.ritesh.cashiro.data.preferences.NavigationBarStyle
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeDefaults.tint
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

/**
 * Bottom navigation bar component that supports both NORMAL and FLOATING styles.
 * Used in flat navigation structure where all screens are at the same NavHost level.
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeApi::class
)
@Composable
fun CashiroBottomNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    currentDestination: NavDestination?,
    navigationBarStyle: NavigationBarStyle,
    hideLabels: Boolean,
    hidePill: Boolean,
    blurEffects: Boolean,
    visible: Boolean,
    hazeState: HazeState = remember { HazeState() },
) {
    val navigationItems = listOf(BottomNavItem.Home, BottomNavItem.Analytics, BottomNavItem.Chat)
    val containerColor = MaterialTheme.colorScheme.surface
    val view = LocalView.current

    Box(modifier = modifier) {
        // NORMAL style NavigationBar
        BlurredAnimatedVisibility(
            visible = visible && navigationBarStyle == NavigationBarStyle.NORMAL,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f)
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = if (blurEffects) 0.5f else 1f
                    ),
                    tonalElevation = 2.dp,
                    modifier = Modifier.then(
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
                    )
                ) {
                    navigationItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route?.contains(item.destinationType.qualifiedName ?: "") == true
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                navController.safeNavigate(item.destination) {
                                    popUpTo(Home) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (selected) {
                                        if (hidePill) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onPrimaryContainer
                                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(
                                        if (hidePill && hideLabels) 28.dp else 24.dp
                                    )
                                )
                            },
                            label = if (hideLabels) null else {
                                {
                                    Text(
                                        text = item.title,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = if (hidePill) Color.Transparent
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        // FLOATING style HorizontalFloatingToolbar
        BlurredAnimatedVisibility(
            visible = visible && navigationBarStyle == NavigationBarStyle.FLOATING,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                HorizontalFloatingToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .animateContentSize(
                            MaterialTheme.motionScheme.fastSpatialSpec()
                        )
                        .navigationBarsPadding()
                        .clip(FloatingToolbarDefaults.ContainerShape)
                        .then(
                            if (blurEffects) Modifier.hazeEffect(
                                state = hazeState,
                                block = fun HazeEffectScope.() {
                                    style = HazeDefaults.style(
                                        backgroundColor = Color.Transparent,
                                        blurRadius = 20.dp,
                                        noiseFactor = -1f,
                                    )
                                    blurredEdgeTreatment = BlurredEdgeTreatment.Unbounded
                                }
                            ) else Modifier
                        )
                        .zIndex(1000f),
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(
                            alpha = if (blurEffects) 0.7f else 1f
                        ),
                    ),
                    expanded = true,
                ) {
                    navigationItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route?.contains(item.destinationType.qualifiedName ?: "") == true
                        } == true

                        TonalToggleButton(
                            checked = selected,
                            onCheckedChange = {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                navController.safeNavigate(item.destination) {
                                    popUpTo(Home) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                containerColor = if(blurEffects)
                                    MaterialTheme.colorScheme.surfaceBright.copy(0.6f)
                                else MaterialTheme.colorScheme.surfaceBright,
                                contentColor = MaterialTheme.colorScheme.inverseSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.7f),
                                disabledContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                checkedContainerColor =  if(blurEffects)
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(0.6f)
                                else MaterialTheme.colorScheme.tertiaryContainer,
                                checkedContentColor =  MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(imageVector = item.icon, contentDescription = item.title)
                            AnimatedVisibility(
                                visible = selected,
                                enter = fadeIn() + expandHorizontally(MaterialTheme.motionScheme.fastSpatialSpec()),
                                exit = fadeOut() + shrinkHorizontally(MaterialTheme.motionScheme.fastSpatialSpec())
                            ) {
                                Text(
                                    text = item.title,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
