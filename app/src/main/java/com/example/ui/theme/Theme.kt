package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SacredExtendedColors(
    val dawnGold: Color,
    val dawnGoldContainer: Color,
    val sage: Color,
    val sageContainer: Color,
    val warmCard: Color,
    val hairline: Color,
    val subtleText: Color
)

val LocalSacredColors = staticCompositionLocalOf {
    SacredExtendedColors(
        dawnGold = DawnGold,
        dawnGoldContainer = DawnGoldSoft,
        sage = MutedSage,
        sageContainer = MutedSageSoft,
        warmCard = WarmBeige,
        hairline = HairlineWarm,
        subtleText = MutedWarmGray
    )
}

private val LightSacredColorScheme = lightColorScheme(
    primary = DeepSacredBlue,
    onPrimary = PureSanctuary,
    primaryContainer = GracefulSlateBlue,
    onPrimaryContainer = WarmSand,
    secondary = MutedSage,
    onSecondary = PureSanctuary,
    secondaryContainer = MutedSageSoft,
    onSecondaryContainer = MutedSageDeep,
    tertiary = DawnGoldDeep,
    onTertiary = PureSanctuary,
    tertiaryContainer = DawnGoldSoft,
    onTertiaryContainer = DeepSacredBlue,
    background = WarmSand,
    onBackground = OffBlack,
    surface = PureSanctuary,
    onSurface = OffBlack,
    surfaceVariant = WarmBeige,
    onSurfaceVariant = MutedWarmGray,
    outline = HairlineWarm,
    error = GentleTerracotta,
    onError = PureSanctuary
)

private val DarkSacredColorScheme = darkColorScheme(
    primary = DawnGoldDarkMode,
    onPrimary = MidnightBlueBg,
    primaryContainer = MidnightCard,
    onPrimaryContainer = WarmCreamText,
    secondary = MutedSageDarkMode,
    onSecondary = MidnightBlueBg,
    secondaryContainer = Color(0xFF28382A),
    onSecondaryContainer = WarmCreamText,
    tertiary = DawnGoldDarkMode,
    onTertiary = MidnightBlueBg,
    tertiaryContainer = Color(0xFF3A321C),
    onTertiaryContainer = WarmCreamText,
    background = MidnightBlueBg,
    onBackground = WarmCreamText,
    surface = MidnightSurface,
    onSurface = WarmCreamText,
    surfaceVariant = MidnightCard,
    onSurfaceVariant = SoftSandSecondary,
    outline = HairlineDark,
    error = Color(0xFFE08577),
    onError = MidnightBlueBg
)

private val SacredShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SacredStepsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkSacredColorScheme else LightSacredColorScheme
    val extended = if (darkTheme) {
        SacredExtendedColors(
            dawnGold = DawnGoldDarkMode,
            dawnGoldContainer = Color(0xFF362F1B),
            sage = MutedSageDarkMode,
            sageContainer = Color(0xFF243326),
            warmCard = MidnightCard,
            hairline = HairlineDark,
            subtleText = SoftSandSecondary
        )
    } else {
        SacredExtendedColors(
            dawnGold = DawnGold,
            dawnGoldContainer = DawnGoldSoft,
            sage = MutedSage,
            sageContainer = MutedSageSoft,
            warmCard = WarmBeige,
            hairline = HairlineWarm,
            subtleText = MutedWarmGray
        )
    }

    CompositionLocalProvider(LocalSacredColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SacredTypography,
            shapes = SacredShapes,
            content = content
        )
    }
}
