package com.ascdev.checkmo.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val Base = Typography()

// Tabular figures keep clock digits from shifting while the timer ticks.
private const val TabularNumbers = "tnum"

val Typography = Typography(
    displayLarge = Base.displayLarge.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = TabularNumbers),
    displayMedium = Base.displayMedium.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = TabularNumbers),
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = TabularNumbers),
    headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = Base.bodyLarge,
    bodyMedium = Base.bodyMedium,
    bodySmall = Base.bodySmall,
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = Base.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = Base.labelSmall.copy(fontWeight = FontWeight.Medium),
)
