package com.medsafe.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.model.Dose
import com.medsafe.mobile.model.Estoque
import com.medsafe.mobile.model.Farmacia
import com.medsafe.mobile.model.Medicamento
import com.medsafe.mobile.model.StatusDose
import com.medsafe.mobile.ui.components.BotoesDeEmergencia
import com.medsafe.mobile.ui.components.BottomNavBar
import com.medsafe.mobile.ui.components.CampoDeTexto
import com.medsafe.mobile.ui.components.Carregando
import com.medsafe.mobile.ui.components.DestinoPrincipal
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.components.abrirWhatsApp
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
    aoAbrirConfiguracoes: () -> Unit,
    aoNavegar: (DestinoPrincipal) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sessao by viewModel.sessao.collectAsStateWithLifecycle()
    var confirmandoSaida by remember { mutableStateOf(false) }
    var medicamentoParaRepor by remember { mutableStateOf<Medicamento?>(null) }

    val contexto = LocalContext.current
    val pedirPermissaoDeAvisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        viewModel.carregar()
        // Android 13+: os avisos na hora do remedio dependem desta permissao.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermissaoDeAvisos.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavBar(destinoAtual = DestinoPrincipal.INICIO, aoNavegar = aoNavegar) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CabecalhoDashboard(
                nomeUsuario = sessao?.usuarioNome.orEmpty(),
                aoAbrirConfiguracoes = aoAbrirConfiguracoes,
                aoSair = { confirmandoSaida = true },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { BotoesDeEmergencia(compacto = true) }

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
                            modifier = Modifier.semantics { heading() },
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
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    items(estado.estoqueBaixo, key = { "estoque-${it.id}" }) { medicamento ->
                        CartaoDeEstoqueBaixo(
                            medicamento = medicamento,
                            farmaciaPreferida = estado.farmaciaPreferida,
                            repondo = estado.reposicaoEmAndamento == medicamento.id,
                            aoPedirNoWhatsApp = { farmacia ->
                                contexto.abrirWhatsApp(Estoque.linkDoPedido(farmacia, medicamento.nome))
                            },
                            aoCadastrarFarmacia = { aoNavegar(DestinoPrincipal.CONTATOS) },
                            aoRepor = { medicamentoParaRepor = medicamento },
                        )
                    }
                }
            }
        }
    }

    medicamentoParaRepor?.let { medicamento ->
        DialogoDeReposicao(
            medicamento = medicamento,
            aoConfirmar = { unidades ->
                medicamentoParaRepor = null
                viewModel.reporEstoque(medicamento, unidades)
            },
            aoCancelar = { medicamentoParaRepor = null },
        )
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
private fun CabecalhoDashboard(nomeUsuario: String, aoAbrirConfiguracoes: () -> Unit, aoSair: () -> Unit) {
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
                    modifier = Modifier.semantics { heading() },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = aoAbrirConfiguracoes, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Ajustes", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                    TextButton(onClick = aoSair, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Sair", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                    }
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) {}) {
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
                            modifier = Modifier.weight(1f).padding(vertical = 2.dp).semantics {
                                contentDescription = "Tomei: ${dose.medicamentoNome} das $horario"
                            },
                        ) {
                            Text("Tomei", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = aoPular,
                            modifier = Modifier.weight(1f).padding(vertical = 2.dp).semantics {
                                contentDescription = "Pular: ${dose.medicamentoNome} das $horario"
                            },
                        ) {
                            Text("Pular", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

/** Aviso de reposicao: quanto resta (em dias, pelo ritmo das doses), pedido por WhatsApp e registro da compra. */
@Composable
private fun CartaoDeEstoqueBaixo(
    medicamento: Medicamento,
    farmaciaPreferida: Farmacia?,
    repondo: Boolean,
    aoPedirNoWhatsApp: (Farmacia) -> Unit,
    aoCadastrarFarmacia: () -> Unit,
    aoRepor: () -> Unit,
) {
    Surface(color = MedSafeWarningContainer, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) {}) {
                Icon(imageVector = Icons.Filled.WarningAmber, contentDescription = null, tint = MedSafeOnWarningContainer)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = medicamento.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MedSafeOnWarningContainer,
                    )
                    Text(
                        text = Estoque.descricao(medicamento.quantidade, medicamento.diasRestantes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MedSafeOnWarningContainer,
                    )
                }
            }

            if (repondo) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                }
            } else {
                if (farmaciaPreferida != null) {
                    Button(
                        onClick = { aoPedirNoWhatsApp(farmaciaPreferida) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics {
                            contentDescription = "Pedir ${medicamento.nome} na ${farmaciaPreferida.nome} pelo WhatsApp"
                        },
                    ) {
                        Text("Pedir na ${farmaciaPreferida.nome} (WhatsApp)", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    OutlinedButton(onClick = aoCadastrarFarmacia, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text("Cadastrar minha farmácia", style = MaterialTheme.typography.labelLarge)
                    }
                }
                OutlinedButton(
                    onClick = aoRepor,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics {
                        contentDescription = "Já comprei ${medicamento.nome}: somar ao estoque"
                    },
                ) {
                    Text("Já comprei", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DialogoDeReposicao(
    medicamento: Medicamento,
    aoConfirmar: (Int) -> Unit,
    aoCancelar: () -> Unit,
) {
    var texto by remember { mutableStateOf("30") }
    var mostrarErro by remember { mutableStateOf(false) }
    val unidades = Estoque.unidadesDaReposicao(texto)

    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text("Já comprou ${medicamento.nome}?") },
        text = {
            CampoDeTexto(
                rotulo = "Quantas unidades vieram na compra?",
                valor = texto,
                aoAlterar = { texto = it.filter { c -> c.isDigit() }.take(4); mostrarErro = false },
                tipoTeclado = KeyboardType.Number,
                erro = if (mostrarErro && unidades == null) "Digite um número de 1 a ${Estoque.MAXIMO_POR_REPOSICAO}" else null,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (unidades != null) aoConfirmar(unidades) else mostrarErro = true }) {
                Text("Somar ao estoque", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) { Text("Cancelar", style = MaterialTheme.typography.labelLarge) }
        },
    )
}
