package com.medsafe.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.medsafe.mobile.ui.theme.MedSafeDangerContainer
import com.medsafe.mobile.ui.theme.MedSafeEmergency
import com.medsafe.mobile.ui.theme.MedSafeOnDangerContainer

/** Numeros de emergencia do Brasil: SAMU (192, urgencia medica) e Policia Militar (190). */
private data class NumeroDeEmergencia(val numero: String, val nome: String, val artigoENome: String)

private val Numeros = listOf(
    NumeroDeEmergencia("192", "SAMU", "o SAMU"),
    NumeroDeEmergencia("190", "Polícia", "a Polícia"),
)

/**
 * Atalhos para ligar para a emergencia. Abrem o discador com o numero pronto (a pessoa confirma a
 * chamada com um toque), entao nao precisam de permissao de ligacao nem ligam sem querer.
 * No modo [compacto] so aparecem os botoes (Dashboard); no completo, com titulo e explicacao (Contatos).
 */
@Composable
fun BotoesDeEmergencia(modifier: Modifier = Modifier, compacto: Boolean = false) {
    Surface(color = MedSafeDangerContainer, shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!compacto) {
                Text(
                    text = "Emergência",
                    style = MaterialTheme.typography.titleLarge,
                    color = MedSafeOnDangerContainer,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Toque para abrir o telefone com o número pronto e depois toque em chamar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MedSafeOnDangerContainer,
                )
            }
            // Com a fonte em tamanho muito grande, dois botoes lado a lado quebrariam as palavras: empilha.
            if (LocalDensity.current.fontScale >= 1.4f) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Numeros.forEach { BotaoDeEmergencia(it, Modifier.fillMaxWidth()) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Numeros.forEach { BotaoDeEmergencia(it, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun BotaoDeEmergencia(emergencia: NumeroDeEmergencia, modifier: Modifier) {
    val contexto = LocalContext.current
    Button(
        onClick = { contexto.discar(emergencia.numero) },
        colors = ButtonDefaults.buttonColors(containerColor = MedSafeEmergency, contentColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .heightIn(min = 56.dp)
            .semantics { contentDescription = "Ligar para ${emergencia.artigoENome}, número ${emergencia.numero}" },
    ) {
        Icon(Icons.Filled.Call, contentDescription = null)
        Text(
            text = "${emergencia.nome} ${emergencia.numero}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
