package com.mumtahin.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Sets the real Android status bar background color to [color] for as
 * long as the calling screen is on top. Call this once near the top of
 * any top-level screen that wants its status bar to match its own
 * toolbar/background — e.g.
 * `StatusBarColor(MaterialTheme.colorScheme.primary)`.
 *
 * Uses the classic imperative `window.statusBarColor` API rather than
 * relying on edge-to-edge content extending behind a transparent status
 * bar — that approach didn't reliably show through on every device/OEM
 * skin tested with this app, while this one does.
 *
 * @param darkIcons true if [color] is light (so the clock/battery icons
 * should render dark for contrast); false if [color] is dark (icons
 * render light/white). Defaults to false since this app's toolbar colors
 * are all on the darker/saturated side.
 */
@Composable
internal fun StatusBarColor(color: Color, darkIcons: Boolean = false) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val colorArgb = color.toArgb()
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        @Suppress("DEPRECATION")
        window.statusBarColor = colorArgb
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
    }
}
