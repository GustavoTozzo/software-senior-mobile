package com.medsafe.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsafe.mobile.ui.theme.MedSafeSeniorTheme
import com.medsafe.mobile.ui.theme.MedSafeSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeOnSuccessContainer

private val HorariosSugeridos = listOf("08:00", "12:00", "16:00", "20:00")

/**
 * Cadastro de medicamento. O botao de escanear codigo de barras ainda nao abre a
 * camera de verdade (CameraX + ML Kit e um card separado da Entrega 4) — por ora
 * so reserva o espaco na tela. "Salvar" tambem nao persiste nada ainda: a integracao
 * com POST /api/medicamentos entra junto com o Retrofit, na Entrega 4.
 */
@Composable
fun CadastroMedicamentoScreen(
    onVoltar: () -> Unit,
    onSalvar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nome by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf(30) }
    var frequenciaSelecionada by remember { mutableStateOf(0) }
    var horarios by remember { mutableStateOf(listOf("08:00")) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Novo medicamento") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
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
                    onClick = onSalvar,
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
                    Text("Salvar medicamento", style = MaterialTheme.typography.titleLarge)
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
            RotuloDeCampo("Nome do medicamento")
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                placeholder = { Text("Losartana 50mg") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )

            RotuloDeCampo("Código de barras")
            OutlinedButton(
                onClick = { },
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

            RotuloDeCampo("Quantidade em estoque")
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotaoDeEstoque(icone = Icons.Filled.Remove) {
                    if (quantidade > 0) quantidade -= 1
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = quantidade.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                BotaoDeEstoque(icone = Icons.Filled.Add) {
                    quantidade += 1
                }
            }

            RotuloDeCampo("Frequência")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(52.dp),
            ) {
                items(listOf("1x ao dia", "2x ao dia", "3x ao dia")) { rotulo ->
                    val indice = listOf("1x ao dia", "2x ao dia", "3x ao dia").indexOf(rotulo)
                    val selecionado = indice == frequenciaSelecionada
                    Surface(
                        onClick = { frequenciaSelecionada = indice },
                        color = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp),
                        border = if (selecionado) null else androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.aspectRatio(2.2f),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = rotulo,
                                color = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            RotuloDeCampo("Horários")
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                horarios.forEach { horario ->
                    Surface(
                        color = MedSafeSuccessContainer,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(
                            text = horario,
                            color = MedSafeOnSuccessContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        )
                    }
                }
                val proximoHorario = HorariosSugeridos.firstOrNull { it !in horarios }
                if (proximoHorario != null) {
                    OutlinedButton(
                        onClick = { horarios = horarios + proximoHorario },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Text("+ adicionar", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun RotuloDeCampo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun BotaoDeEstoque(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    aoClicar: () -> Unit,
) {
    Surface(
        onClick = aoClicar,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(56.dp).aspectRatio(1f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CadastroMedicamentoScreenPreview() {
    MedSafeSeniorTheme {
        CadastroMedicamentoScreen(onVoltar = { }, onSalvar = { })
    }
}
