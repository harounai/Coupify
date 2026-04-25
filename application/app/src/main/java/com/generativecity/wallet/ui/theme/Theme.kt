package com.generativecity.wallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Ocean,
    secondary = Emerald,
    tertiary = Ruby,
    background = Cloud,
    surface = Panel,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = Midnight,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = Charcoal,
    onSurface = Charcoal,
    outline = Mist
)

private val DarkColors = darkColorScheme(
    primary = Ocean,
    secondary = Emerald,
    tertiary = Ruby,
    background = Midnight,
    surface = Charcoal,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = Cloud,
    onSurface = Cloud
)

@Composable
fun GenerativeCityWalletTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
