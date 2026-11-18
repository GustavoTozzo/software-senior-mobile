package com.medsafe.mobile.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.medsafe.mobile.model.Preferencias
import com.medsafe.mobile.model.TemaDoApp
import com.medsafe.mobile.model.ehEscuro

private val LightColors = lightColorScheme(
    primary = MedSafeGreen,
    onPrimary = MedSafeBackground,
    primaryContainer = MedSafeSuccessContainer,
    onPrimaryContainer = MedSafeOnSuccessContainer,
    secondary = MedSafeAccent,
    background = MedSafeBackground,
    onBackground = MedSafeOnBackground,
    surface = MedSafeBackground,
    onSurface = MedSafeOnBackground,
    surfaceVariant = MedSafeSurface,
    onSurfaceVariant = MedSafeMuted,
    outline = MedSafeOutline,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = MedSafeOnBackground,
    primaryContainer = MedSafeGreenDark,
    onPrimaryContainer = MedSafeBackground,
    secondary = MedSafeAccent,
    background = MedSafeGreenDark,
    onBackground = MedSafeBackground,
    surface = MedSafeGreenDark,
    onSurface = MedSafeBackground,
    surfaceVariant = MedSafeGreenDark,
    onSurfaceVariant = MedSafeSurface,
    outline = MedSafeGreenLight,
)

/** Preto sobre branco, verde bem escuro e bordas pretas: contraste acima de 7:1 em todos os textos. */
private val HighContrastColors = lightColorScheme(
    primary = Color(0xFF002A00),
    onPrimary = Color.White,
    primaryContainer = Color.White,
    onPrimaryContainer = Color.Black,
    secondary = MedSafeAccent,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    error = Color(0xFF8C0009),
)

@Composable
fun MedSafeSeniorTheme(
    preferencias: Preferencias = Preferencias(),
    content: @Composable () -> Unit,
) {
    val escuro = preferencias.tema.ehEscuro(isSystemInDarkTheme())
    val colorScheme = when {
        preferencias.tema == TemaDoApp.ALTO_CONTRASTE -> HighContrastColors
        escuro -> DarkColors
        else -> LightColors
    }

    // Os icones da barra de status precisam contrastar com o fundo escolhido no app (nao com o do celular).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            view.context.encontrarActivity()?.window?.let { janela ->
                WindowCompat.getInsetsController(janela, view).isAppearanceLightStatusBars = !escuro
            }
        }
    }

    // O tamanho do texto do app multiplica o que a pessoa ja escolheu nas configuracoes do celular.
    val densidade = LocalDensity.current
    val densidadeAjustada = Density(densidade.density, densidade.fontScale * preferencias.tamanhoDoTexto.escala)

    CompositionLocalProvider(LocalDensity provides densidadeAjustada) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MedSafeTypography,
            content = content,
        )
    }
}

private tailrec fun Context.encontrarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.encontrarActivity()
    else -> null
}

/**
 * Telas com barra superior na cor primaria (Ajustes, Novo medicamento) desenham essa cor atras da barra de
 * status; os icones dela precisam contrastar com a primaria do tema (claros sobre verde escuro, escuros
 * sobre o verde claro do tema escuro) e voltam ao normal ao sair da tela.
 */
@Composable
fun IconesDaBarraDeStatusSobreAPrimaria() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val primariaEscura = MaterialTheme.colorScheme.primary.luminance() < 0.5f
    DisposableEffect(primariaEscura) {
        val janela = view.context.encontrarActivity()?.window
        val controle = janela?.let { WindowCompat.getInsetsController(it, view) }
        val anterior = controle?.isAppearanceLightStatusBars
        controle?.isAppearanceLightStatusBars = !primariaEscura
        onDispose {
            if (controle != null && anterior != null) controle.isAppearanceLightStatusBars = anterior
        }
    }
}
