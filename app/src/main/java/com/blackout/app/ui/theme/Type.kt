@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
package com.blackout.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.blackout.app.R

val GoogleSansRounded = FontFamily(
//    Font(
//        resId = R.font.google_sans_rounded_variable,
//        weight = FontWeight.Normal,
//        variationSettings = FontVariation.Settings(
//            FontVariation.weight(400),
//            FontVariation.Setting("ROND", 100f)
//        )
//    ),
//    Font(
//        resId = R.font.google_sans_rounded_variable,
//        weight = FontWeight.Medium,
//        variationSettings = FontVariation.Settings(
//            FontVariation.weight(500),
//            FontVariation.Setting("ROND", 100f)
//        )
//    ),
    Font(
        resId = R.font.google_sans_rounded_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = GoogleSansRounded),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = GoogleSansRounded),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = GoogleSansRounded),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = GoogleSansRounded),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = GoogleSansRounded),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = GoogleSansRounded),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = GoogleSansRounded),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = GoogleSansRounded),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = GoogleSansRounded),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = GoogleSansRounded),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = GoogleSansRounded),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = GoogleSansRounded),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = GoogleSansRounded),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = GoogleSansRounded),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = GoogleSansRounded)
)