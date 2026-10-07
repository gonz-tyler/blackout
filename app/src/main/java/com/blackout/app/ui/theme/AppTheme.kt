package com.blackout.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared radius / spacing tokens, mirroring AppTheme from the Flutter app. */
object AppTheme {
    val radiusS: Dp = 12.dp
    val radiusL: Dp = 20.dp
    val radiusXL: Dp = 28.dp
    val radiusFull: Dp = 999.dp

    // Clearance reserved at the bottom of scroll content so it isn't
    // obscured by a bottom navigation bar.
    val bottomNavClearance: Dp = 96.dp
}
