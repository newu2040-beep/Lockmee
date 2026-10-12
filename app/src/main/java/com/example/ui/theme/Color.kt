package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.domain.model.ExpressivePalette

// 1. INDIGO PALETTE (Default Material You Expressive)
val IndigoLightPrimary = Color(0xFF4F5D92)
val IndigoLightOnPrimary = Color(0xFFFFFFFF)
val IndigoLightPrimaryContainer = Color(0xFFDFE4FF)
val IndigoLightOnPrimaryContainer = Color(0xFF171F49)
val IndigoLightSecondary = Color(0xFF586078)
val IndigoLightBackground = Color(0xFFF8F7FC)
val IndigoLightSurface = Color(0xFFF8F7FC)
val IndigoLightSurfaceVariant = Color(0xFFE3E2EC)

val IndigoDarkPrimary = Color(0xFFB9C6FF)
val IndigoDarkOnPrimary = Color(0xFF24315F)
val IndigoDarkPrimaryContainer = Color(0xFF384574)
val IndigoDarkOnPrimaryContainer = Color(0xFFDFE4FF)
val IndigoDarkSecondary = Color(0xFFC1C5DD)
val IndigoDarkBackground = Color(0xFF121318)
val IndigoDarkSurface = Color(0xFF121318)
val IndigoDarkSurfaceVariant = Color(0xFF444650)

// 2. EMERALD CYBER PALETTE
val EmeraldLightPrimary = Color(0xFF006C4C)
val EmeraldLightOnPrimary = Color(0xFFFFFFFF)
val EmeraldLightPrimaryContainer = Color(0xFF89F8C7)
val EmeraldLightOnPrimaryContainer = Color(0xFF002114)
val EmeraldLightSecondary = Color(0xFF4D6357)
val EmeraldLightBackground = Color(0xFFF6FBF6)
val EmeraldLightSurface = Color(0xFFF6FBF6)
val EmeraldLightSurfaceVariant = Color(0xFFDCE5DD)

val EmeraldDarkPrimary = Color(0xFF6CDBAC)
val EmeraldDarkOnPrimary = Color(0xFF003825)
val EmeraldDarkPrimaryContainer = Color(0xFF005138)
val EmeraldDarkOnPrimaryContainer = Color(0xFF89F8C7)
val EmeraldDarkSecondary = Color(0xFFB4CCBD)
val EmeraldDarkBackground = Color(0xFF0F1512)
val EmeraldDarkSurface = Color(0xFF0F1512)
val EmeraldDarkSurfaceVariant = Color(0xFF404943)

// 3. AMBER SUNSET PALETTE
val AmberLightPrimary = Color(0xFF8B5000)
val AmberLightOnPrimary = Color(0xFFFFFFFF)
val AmberLightPrimaryContainer = Color(0xFFFFDCC1)
val AmberLightOnPrimaryContainer = Color(0xFF2D1600)
val AmberLightSecondary = Color(0xFF725A42)
val AmberLightBackground = Color(0xFFFFF8F5)
val AmberLightSurface = Color(0xFFFFF8F5)
val AmberLightSurfaceVariant = Color(0xFFF1DFD1)

val AmberDarkPrimary = Color(0xFFFFB77C)
val AmberDarkOnPrimary = Color(0xFF4A2800)
val AmberDarkPrimaryContainer = Color(0xFF693C00)
val AmberDarkOnPrimaryContainer = Color(0xFFFFDCC1)
val AmberDarkSecondary = Color(0xFFE1C1A4)
val AmberDarkBackground = Color(0xFF18120C)
val AmberDarkSurface = Color(0xFF18120C)
val AmberDarkSurfaceVariant = Color(0xFF50453A)

// 4. OLED MONOCHROME PALETTE
val MonoLightPrimary = Color(0xFF1E1E24)
val MonoLightOnPrimary = Color(0xFFFFFFFF)
val MonoLightPrimaryContainer = Color(0xFFE4E4E8)
val MonoLightOnPrimaryContainer = Color(0xFF121215)
val MonoLightSecondary = Color(0xFF5D5D68)
val MonoLightBackground = Color(0xFFFBFBFC)
val MonoLightSurface = Color(0xFFFBFBFC)
val MonoLightSurfaceVariant = Color(0xFFE2E2E6)

val MonoDarkPrimary = Color(0xFFEDEDF2)
val MonoDarkOnPrimary = Color(0xFF141418)
val MonoDarkPrimaryContainer = Color(0xFF33333C)
val MonoDarkOnPrimaryContainer = Color(0xFFF4F4F8)
val MonoDarkSecondary = Color(0xFFC7C7CF)
val MonoDarkBackground = Color(0xFF000000)
val MonoDarkSurface = Color(0xFF0A0A0C)
val MonoDarkSurfaceVariant = Color(0xFF24242A)

// Common Error and Accent Colors
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

fun getPaletteColorSchemes(palette: ExpressivePalette): Pair<ColorScheme, ColorScheme> {
    return when (palette) {
        ExpressivePalette.DYNAMIC, ExpressivePalette.INDIGO -> Pair(
            lightColorScheme(
                primary = IndigoLightPrimary,
                onPrimary = IndigoLightOnPrimary,
                primaryContainer = IndigoLightPrimaryContainer,
                onPrimaryContainer = IndigoLightOnPrimaryContainer,
                secondary = IndigoLightSecondary,
                background = IndigoLightBackground,
                surface = IndigoLightSurface,
                surfaceVariant = IndigoLightSurfaceVariant,
                error = LightError,
                onError = LightOnError,
                errorContainer = LightErrorContainer,
                onErrorContainer = LightOnErrorContainer
            ),
            darkColorScheme(
                primary = IndigoDarkPrimary,
                onPrimary = IndigoDarkOnPrimary,
                primaryContainer = IndigoDarkPrimaryContainer,
                onPrimaryContainer = IndigoDarkOnPrimaryContainer,
                secondary = IndigoDarkSecondary,
                background = IndigoDarkBackground,
                surface = IndigoDarkSurface,
                surfaceVariant = IndigoDarkSurfaceVariant,
                error = DarkError,
                onError = DarkOnError,
                errorContainer = DarkErrorContainer,
                onErrorContainer = DarkOnErrorContainer
            )
        )
        ExpressivePalette.EMERALD -> Pair(
            lightColorScheme(
                primary = EmeraldLightPrimary,
                onPrimary = EmeraldLightOnPrimary,
                primaryContainer = EmeraldLightPrimaryContainer,
                onPrimaryContainer = EmeraldLightOnPrimaryContainer,
                secondary = EmeraldLightSecondary,
                background = EmeraldLightBackground,
                surface = EmeraldLightSurface,
                surfaceVariant = EmeraldLightSurfaceVariant,
                error = LightError,
                onError = LightOnError
            ),
            darkColorScheme(
                primary = EmeraldDarkPrimary,
                onPrimary = EmeraldDarkOnPrimary,
                primaryContainer = EmeraldDarkPrimaryContainer,
                onPrimaryContainer = EmeraldDarkOnPrimaryContainer,
                secondary = EmeraldDarkSecondary,
                background = EmeraldDarkBackground,
                surface = EmeraldDarkSurface,
                surfaceVariant = EmeraldDarkSurfaceVariant,
                error = DarkError,
                onError = DarkOnError
            )
        )
        ExpressivePalette.AMBER -> Pair(
            lightColorScheme(
                primary = AmberLightPrimary,
                onPrimary = AmberLightOnPrimary,
                primaryContainer = AmberLightPrimaryContainer,
                onPrimaryContainer = AmberLightOnPrimaryContainer,
                secondary = AmberLightSecondary,
                background = AmberLightBackground,
                surface = AmberLightSurface,
                surfaceVariant = AmberLightSurfaceVariant,
                error = LightError,
                onError = LightOnError
            ),
            darkColorScheme(
                primary = AmberDarkPrimary,
                onPrimary = AmberDarkOnPrimary,
                primaryContainer = AmberDarkPrimaryContainer,
                onPrimaryContainer = AmberDarkOnPrimaryContainer,
                secondary = AmberDarkSecondary,
                background = AmberDarkBackground,
                surface = AmberDarkSurface,
                surfaceVariant = AmberDarkSurfaceVariant,
                error = DarkError,
                onError = DarkOnError
            )
        )
        ExpressivePalette.MONOCHROME -> Pair(
            lightColorScheme(
                primary = MonoLightPrimary,
                onPrimary = MonoLightOnPrimary,
                primaryContainer = MonoLightPrimaryContainer,
                onPrimaryContainer = MonoLightOnPrimaryContainer,
                secondary = MonoLightSecondary,
                background = MonoLightBackground,
                surface = MonoLightSurface,
                surfaceVariant = MonoLightSurfaceVariant,
                error = LightError,
                onError = LightOnError
            ),
            darkColorScheme(
                primary = MonoDarkPrimary,
                onPrimary = MonoDarkOnPrimary,
                primaryContainer = MonoDarkPrimaryContainer,
                onPrimaryContainer = MonoDarkOnPrimaryContainer,
                secondary = MonoDarkSecondary,
                background = MonoDarkBackground,
                surface = MonoDarkSurface,
                surfaceVariant = MonoDarkSurfaceVariant,
                error = DarkError,
                onError = DarkOnError
            )
        )
    }
}
