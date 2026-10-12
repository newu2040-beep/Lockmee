package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.domain.model.ExpressivePalette

@Composable
fun LockMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ExpressivePalette = ExpressivePalette.DYNAMIC,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val (fallbackLight, fallbackDark) = getPaletteColorSchemes(palette)

    val colorScheme = when {
        palette == ExpressivePalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            try {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } catch (e: Exception) {
                if (darkTheme) fallbackDark else fallbackLight
            }
        }
        darkTheme -> fallbackDark
        else -> fallbackLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
