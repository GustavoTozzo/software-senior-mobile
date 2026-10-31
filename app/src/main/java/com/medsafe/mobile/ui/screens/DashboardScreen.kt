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
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.model.Dose
import com.medsafe.mobile.model.Medicamento
import com.medsafe.mobile.model.StatusDose
import com.medsafe.mobile.ui.components.BottomNavBar
import com.medsafe.mobile.ui.components.Carregando
import com.medsafe.mobile.ui.components.DestinoPrincipal
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.theme.MedSafeAccent
import com.medsafe.mobile.ui.theme.MedSafeOnSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeOnWarningContainer
import com.medsafe.mobile.ui.theme.MedSafeSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeWarningContainer
import com.medsafe.mobile.ui.viewmodel.DashboardViewModel
import com.medsafe.mobile.ui.viewmodel.rememberViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FormatoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val FormatoDataPorExtenso: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR"))

/** Doses de hoje (com botoes Tomei/Pular), alertas de estoque baixo e atalho para cadastrar remedio. */
@Composable
fun DashboardScreen(
    aoAdicionarMedicamento: () -> Unit,
    aoNavegar: (DestinoPrincipal) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sessao by viewModel.sessao.collectAsStateWithLifecycle()
    var confirmandoSaida by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavBar(destinoAtual = DestinoPrincipal.INICIO, aoNavegar = aoNavegar) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CabecalhoDashboard(
                nomeUsuario = sessao?.usuarioNome.orEmpty(),
                aoSair = { confirmandoSaida = true },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Remédios de hoje",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Button(
                            onClick = aoAdicionarMedicamento,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("+ Novo remédio")
                        }
                    }
                }

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
                            text = "Você ainda não tem remédios para hoje. Toque em \"+ Novo remédio\" para cadastrar o primeiro.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                items(estado.doses, key = { it.id }) { dose ->
                    CartaoDeDose(
                        dose = dose,
                        atualizando = estado.doseEmAtualizacao == dose.id,
                        aoTomar = { viewModel.marcarTomado(dose) },
                        aoPular = { viewModel.marcarPulado(dose) },
                    )
                }

                if (estado.estoqueBaixo.isNotEmpty()) {
                    item {
                        Text(
                            text = "Estoque baixo",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    items(estado.estoqueBaixo, key = { "estoque-${it.id}" }) { medicamento ->
                        CartaoDeEstoqueBaixo(medicamento)
                    }
                }
            }
        }
    }

    if (confirmandoSaida) {
        AlertDialog(
            onDismissRequest = { confirmandoSaida = false },
            title = { Text("Sair da conta?") },
            text = { Text("Os avisos de remédio deste celular serão desligados até você entrar de novo.") },
            confirmButton = {
                TextButton(onClick = { confirmandoSaida = false; viewModel.sair() }) { Text("Sair") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoSaida = false }) { Text("Continuar no app") }
            },
        )
    }
}

@Composable
private fun CabecalhoDashboard(nomeUsuario: String, aoSair: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (nomeUsuario.isBlank()) "Olá" else "Olá, ${nomeUsuario.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                TextButton(onClick = aoSair) {
                    Text("Sair", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                text = LocalDate.now().format(FormatoDataPorExtenso).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun CartaoDeDose(
    dose: Dose,
    atualizando: Boolean,
    aoTomar: () -> Unit,
    aoPular: () -> Unit,
) {
    val tomada = dose.status == StatusDose.TOMADO
    val corDeFundo = if (tomada) MedSafeSuccessContainer else MaterialTheme.colorScheme.surfaceVariant
    val corDoDestaque = if (tomada) MedSafeOnSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val horario = dose.programadaPara.toLocalTime().format(FormatoHora)

    Surface(color = corDeFundo, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (dose.status) {
                                StatusDose.TOMADO -> Icons.Filled.Check
                                StatusDose.PULADO -> Icons.Filled.Close
                                StatusDose.PENDENTE -> Icons.Filled.Medication
                            },
                            contentDescription = null,
                            tint = corDoDestaque,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = dose.medicamentoNome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = when (dose.status) {
                            StatusDose.TOMADO -> "Tomado às ${dose.realizadaEm?.toLocalTime()?.format(FormatoHora) ?: horario}"
                            StatusDose.PULADO -> "Pulado - programado para $horario"
                            StatusDose.PENDENTE -> "Às $horario"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = corDoDestaque,
                    )
                }
            }

            if (dose.status == StatusDose.PENDENTE) {
                if (atualizando) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = aoTomar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MedSafeAccent,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                            modifier = Modifier.weight(1f).padding(vertical = 2.dp),
                        ) {
                            Text("Tomei", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(onClick = aoPular, modifier = Modifier.weight(1f).padding(vertical = 2.dp)) {
                            Text("Pular", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoDeEstoqueBaixo(medicamento: Medicamento) {
    Surface(color = MedSafeWarningContainer, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Filled.WarningAmber, contentDescription = null, tint = MedSafeOnWarningContainer)
            Text(
                text = "${medicamento.nome}: restam ${medicamento.quantidade} " +
                    if (medicamento.quantidade == 1) "unidade" else "unidades",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MedSafeOnWarningContainer,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
