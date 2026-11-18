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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

/** Quando a tela atual tem a barra superior na cor primaria, os icones da barra de status seguem essa cor. */
private val LocalBarraSobrePrimaria = compositionLocalOf<MutableState<Boolean>?> { null }

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

    // Os icones da barra de status precisam contrastar com o que fica atras deles: o fundo da tela ou, nas telas
    // com barra superior verde (Ajustes, Novo medicamento), a propria cor primaria. Uma unica regra decide,
    // para a troca de tema nao desfazer o que a tela pediu.
    val sobrePrimaria = remember { mutableStateOf(false) }
    val barraSobrePrimaria = sobrePrimaria.value
    // 0,18 e o ponto em que icones brancos e pretos tem o mesmo contraste: abaixo dele, icones claros.
    val primariaEscura = colorScheme.primary.luminance() < 0.18f
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val iconesClaros = if (barraSobrePrimaria) primariaEscura else escuro
            view.context.encontrarActivity()?.window?.let { janela ->
                WindowCompat.getInsetsController(janela, view).isAppearanceLightStatusBars = !iconesClaros
            }
        }
    }

    // O tamanho do texto do app multiplica o que a pessoa ja escolheu nas configuracoes do celular.
    val densidade = LocalDensity.current
    val densidadeAjustada = Density(densidade.density, densidade.fontScale * preferencias.tamanhoDoTexto.escala)

    CompositionLocalProvider(
        LocalDensity provides densidadeAjustada,
        LocalBarraSobrePrimaria provides sobrePrimaria,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MedSafeTypography,
            content = content,
        )
    }
}

/** Telas com barra superior na cor primaria (Ajustes, Novo medicamento) chamam isto; ao sair, volta ao normal. */
@Composable
fun IconesDaBarraDeStatusSobreAPrimaria() {
    val estado = LocalBarraSobrePrimaria.current
    DisposableEffect(estado) {
        estado?.value = true
        onDispose { estado?.value = false }
    }
}

private tailrec fun Context.encontrarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.encontrarActivity()
    else -> null
}
