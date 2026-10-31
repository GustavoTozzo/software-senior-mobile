package com.medsafe.mobile.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.ui.components.CampoDeTexto
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.components.OpcaoSelecionavel
import com.medsafe.mobile.ui.scanner.BarcodeScannerScreen
import com.medsafe.mobile.ui.theme.MedSafeOnSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeSuccessContainer
import com.medsafe.mobile.ui.viewmodel.CadastroMedicamentoViewModel
import com.medsafe.mobile.ui.viewmodel.Frequencia
import com.medsafe.mobile.ui.viewmodel.rememberViewModelFactory

/** Cadastro de medicamento: nome, codigo de barras (digitado ou lido pela camera), estoque, frequencia e horarios. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroMedicamentoScreen(
    aoVoltar: () -> Unit,
    aoSalvar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CadastroMedicamentoViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var escaneando by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    LaunchedEffect(estado.salvo) {
        if (estado.salvo) {
            Toast.makeText(contexto, "Remédio salvo", Toast.LENGTH_SHORT).show()
            aoSalvar()
        }
    }

    if (escaneando) {
        BarcodeScannerScreen(
            aoLerCodigo = { codigo ->
                viewModel.alterarCodigoBarras(codigo)
                escaneando = false
            },
            aoFechar = { escaneando = false },
            modifier = modifier,
        )
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Novo medicamento") },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = viewModel::salvar,
                    enabled = !estado.salvando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (estado.salvando) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp),
                        )
                    } else {
                        Text("Salvar medicamento", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            estado.erro?.let { MensagemDeErro(it) }

            CampoDeTexto(
                rotulo = "Nome do medicamento",
                valor = estado.nome,
                aoAlterar = viewModel::alterarNome,
                placeholder = "Losartana 50mg",
                erro = estado.errosPorCampo["nomeComercial"],
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoDeTexto(
                    rotulo = "Código de barras (opcional)",
                    valor = estado.codigoBarras,
                    aoAlterar = viewModel::alterarCodigoBarras,
                    placeholder = "Digite ou escaneie",
                    tipoTeclado = KeyboardType.Number,
                    erro = estado.errosPorCampo["codigoBarras"],
                )
                OutlinedButton(
                    onClick = { escaneando = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MedSafeSuccessContainer,
                        contentColor = MedSafeOnSuccessContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Text(
                        text = "Escanear código de barras",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Rotulo("Quantidade em estoque")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BotaoDeEstoque(Icons.Filled.Remove, "Diminuir quantidade", viewModel::diminuirQuantidade)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = estado.quantidade.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    BotaoDeEstoque(Icons.Filled.Add, "Aumentar quantidade", viewModel::aumentarQuantidade)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Rotulo("Frequência")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Frequencia.entries.forEach { opcao ->
                        OpcaoSelecionavel(
                            rotulo = opcao.rotulo,
                            selecionado = estado.frequencia == opcao,
                            aoSelecionar = { viewModel.escolherFrequencia(opcao) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Rotulo("Horários")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    estado.horarios.forEach { horario ->
                        ChipDeHorario(horario = horario, aoRemover = { viewModel.removerHorario(horario) })
                    }
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            contexto,
                            { _, hora, minuto -> viewModel.adicionarHorario("%02d:%02d".format(hora, minuto)) },
                            8,
                            0,
                            true,
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text("+ Adicionar horário", style = MaterialTheme.typography.bodyLarge)
                }
                estado.errosPorCampo["horariosProgramados"]?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun ChipDeHorario(horario: String, aoRemover: () -> Unit) {
    Surface(color = MedSafeSuccessContainer, shape = RoundedCornerShape(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 18.dp)) {
            Text(
                text = horario,
                color = MedSafeOnSuccessContainer,
                style = MaterialTheme.typography.bodyLarge,
            )
            IconButton(onClick = aoRemover, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remover horário $horario",
                    tint = MedSafeOnSuccessContainer,
                )
            }
        }
    }
}

@Composable
private fun BotaoDeEstoque(icone: ImageVector, descricao: String, aoClicar: () -> Unit) {
    Surface(
        onClick = aoClicar,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(56.dp)
            .aspectRatio(1f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icone, contentDescription = descricao, tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}
