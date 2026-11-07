package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SpawnColorScheme = darkColorScheme(
    primary = NecroGreen,
    onPrimary = VoidBlack,
    primaryContainer = NecroGreenDark,
    onPrimaryContainer = BoneWhite,
    secondary = CapeRed,
    onSecondary = BoneWhite,
    secondaryContainer = CapeRedDark,
    onSecondaryContainer = BoneWhite,
    tertiary = ViolatorGold,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = BoneWhite,
    surface = DeepCharcoal,
    onSurface = BoneWhite,
    surfaceVariant = CryptStone,
    onSurfaceVariant = ChainSteel,
    outline = CapeRedDark,
    error = CapeRed,
    onError = BoneWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always preserve Spawn's iconic gothic aesthetic
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SpawnColorScheme,
        typography = Typography,
        content = content
    )
}
