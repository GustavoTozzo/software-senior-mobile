package com.medsafe.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.model.Dose
import com.medsafe.mobile.model.StatusDose
import com.medsafe.mobile.ui.components.BottomNavBar
import com.medsafe.mobile.ui.components.Carregando
import com.medsafe.mobile.ui.components.DestinoPrincipal
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.components.OpcaoSelecionavel
import com.medsafe.mobile.ui.theme.MedSafeDangerContainer
import com.medsafe.mobile.ui.theme.MedSafeOnDangerContainer
import com.medsafe.mobile.ui.theme.MedSafeOnSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeOnWarningContainer
import com.medsafe.mobile.ui.theme.MedSafeSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeWarningContainer
import com.medsafe.mobile.ui.viewmodel.HistoricoViewModel
import com.medsafe.mobile.ui.viewmodel.PeriodoHistorico
import com.medsafe.mobile.ui.viewmodel.rememberViewModelFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FormatoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val FormatoDia: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("pt-BR"))

/** Doses ja programadas, do dia mais recente para o mais antigo, filtradas por Hoje/Semana/Mes. */
@Composable
fun HistoricoScreen(
    aoNavegar: (DestinoPrincipal) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoricoViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.carregar() }

    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavBar(destinoAtual = DestinoPrincipal.HISTORICO, aoNavegar = aoNavegar) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Text(
                    text = "Histórico",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                PeriodoHistorico.entries.forEach { periodo ->
                    OpcaoSelecionavel(
                        rotulo = periodo.rotulo,
                        selecionado = estado.periodo == periodo,
                        aoSelecionar = { viewModel.carregar(periodo) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                estado.erro?.let { erro ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MensagemDeErro(erro)
                            OutlinedButton(onClick = { viewModel.carregar() }) { Text("Tentar de novo") }
                        }
                    }
                }

                if (estado.carregando) {
                    item { Carregando() }
                } else if (estado.doses.isEmpty() && estado.erro == null) {
                    item {
                        Text(
                            text = "Nenhuma dose registrada neste período.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                estado.doses.groupBy { it.programadaPara.toLocalDate() }.forEach { (dia, doses) ->
                    item(key = "dia-$dia") {
                        Text(
                            text = tituloDoDia(dia),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(doses, key = { it.id }) { dose -> LinhaDoHistorico(dose) }
                }
            }
        }
    }
}

private fun tituloDoDia(dia: LocalDate): String {
    val hoje = LocalDate.now()
    return when (dia) {
        hoje -> "Hoje"
        hoje.minusDays(1) -> "Ontem"
        else -> dia.format(FormatoDia).replaceFirstChar { it.uppercase() }
    }
}

@Composable
private fun LinhaDoHistorico(dose: Dose) {
    val horario = dose.programadaPara.toLocalTime().format(FormatoHora)
    val naoRegistrada = dose.status == StatusDose.PENDENTE && dose.programadaPara.isBefore(LocalDateTime.now())

    val fundo: Color
    val destaque: Color
    val circulo: Color
    val icone = when (dose.status) {
        StatusDose.TOMADO -> Icons.Filled.Check
        StatusDose.PULADO -> Icons.Filled.Close
        StatusDose.PENDENTE -> Icons.Filled.Schedule
    }
    val situacao: String
    when {
        dose.status == StatusDose.TOMADO -> {
            fundo = MaterialTheme.colorScheme.surfaceVariant
            destaque = MedSafeOnSuccessContainer
            circulo = MedSafeSuccessContainer
            situacao = "Tomado às ${dose.realizadaEm?.toLocalTime()?.format(FormatoHora) ?: horario}"
        }
        dose.status == StatusDose.PULADO || naoRegistrada -> {
            fundo = MaterialTheme.colorScheme.surfaceVariant
            destaque = MedSafeOnDangerContainer
            circulo = MedSafeDangerContainer
            situacao = if (dose.status == StatusDose.PULADO) "Pulado" else "Não registrado"
        }
        else -> {
            fundo = MedSafeWarningContainer
            destaque = MedSafeOnWarningContainer
            circulo = MaterialTheme.colorScheme.background
            situacao = "Pendente"
        }
    }

    Surface(color = fundo, shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = circulo, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icone, contentDescription = null, tint = destaque)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                    text = dose.medicamentoNome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Programado para $horario",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = situacao,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = destaque,
                )
            }
        }
    }
}
