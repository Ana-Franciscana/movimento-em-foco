package br.com.movimentoemfoco.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Verde80,
    secondary = Azul80,
    tertiary = Azul80,
)

private val LightColorScheme = lightColorScheme(
    primary = Verde40,
    onPrimary = Branco,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = TextoEscuro,
    secondary = Azul40,
    onSecondary = Branco,
    secondaryContainer = AzulClaro,
    onSecondaryContainer = TextoEscuro,
    tertiary = Azul40,
    background = Branco,
    surface = Branco,
    onBackground = TextoEscuro,
    onSurface = TextoEscuro,
    error = ErroTexto,
)

@Composable
fun MovimentoEmFocoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Cores dinâmicas (Android 12+) ficam desligadas para manter a identidade verde/azul/branco.
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
