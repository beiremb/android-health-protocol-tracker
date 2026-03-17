package com.example.protocoltracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BrandColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = BrandWhite,
    secondary = BrandTeal,
    onSecondary = BrandWhite,
    tertiary = BrandTeal,
    onTertiary = BrandWhite,
    background = BrandWhite,
    onBackground = BrandGray,
    surface = BrandWhite,
    onSurface = BrandGray,
    surfaceVariant = BrandWhite,
    onSurfaceVariant = BrandGray,
    outline = BrandGray,
    error = BrandOrange,
    onError = BrandWhite
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun ProtocolTrackerTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrandColorScheme,
        typography = Typography,
        content = content
    )
}