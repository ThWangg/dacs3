package ltdd.dacsba.groceries.ui.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.Brush

// Brand colors
val BrandPrimary = Color(0xFF787FF6)      // Indigo/violet (30%)
val BrandSecondary = Color(0xFF1CA7EC)    // Xanh cyan/Light Blue (30%)
val BrandTertiary = Color(0xFF1F2F98)     // Xanh navy/Deep Blue (30%)

val Purple80 = Color(0xFF787FF6)
val PurpleGrey80 = Color(0xFF1CA7EC)
val Pink80 = Color(0xFF1F2F98)

val Purple40 = Color(0xFF787FF6)
val PurpleGrey40 = Color(0xFF1CA7EC)
val Pink40 = Color(0xFF1F2F98)

// Gradients using the three main colors
val BrandGradient = Brush.linearGradient(
    colors = listOf(BrandPrimary, BrandSecondary, BrandTertiary)
)

val BrandGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(BrandPrimary, BrandSecondary, BrandTertiary)
)

val BrandGradientVertical = Brush.verticalGradient(
    colors = listOf(BrandPrimary, BrandSecondary, BrandTertiary)
)