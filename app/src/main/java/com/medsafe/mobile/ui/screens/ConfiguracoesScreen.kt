package com.medsafe.mobile.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.model.TamanhoDoTexto
import com.medsafe.mobile.model.TemaDoApp
import com.medsafe.mobile.ui.components.LinhaComChave
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.components.OpcaoSelecionavel
import com.medsafe.mobile.ui.viewmodel.ConfiguracoesViewModel
import com.medsafe.mobile.ui.viewmodel.rememberViewModelFactory

/** Ajustes do app: tema (inclusive alto contraste), tamanho do texto e avisos. As mudancas valem na hora. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfiguracoesViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Titulo("Aparência")

            Rotulo("Tema")
            TemaDoApp.entries.chunked(2).forEach { linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    linha.forEach { tema ->
                        OpcaoSelecionavel(
                            rotulo = tema.rotulo,
                            selecionado = preferencias.tema == tema,
                            aoSelecionar = { viewModel.escolherTema(tema) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Rotulo("Tamanho do texto")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TamanhoDoTexto.entries.forEach { tamanho ->
                    OpcaoSelecionavel(
                        rotulo = tamanho.rotulo,
                        selecionado = preferencias.tamanhoDoTexto == tamanho,
                        aoSelecionar = { viewModel.escolherTamanhoDoTexto(tamanho) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = "Assim fica o texto no aplicativo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }

            Titulo("Avisos", modifier = Modifier.padding(top = 10.dp))

            LinhaComChave(
                titulo = "Lembrete na hora de cada remédio",
                descricao = "Uma notificação no horário de tomar, mesmo sem internet.",
                ligada = preferencias.lembretesDeRemedio,
                aoAlterar = viewModel::alternarLembretesDeRemedio,
            )
            LinhaComChave(
                titulo = "Lembrete da Farmácia Popular",
                descricao = "Um aviso no dia em que o próximo fornecimento é liberado.",
                ligada = preferencias.lembretesDeRetirada,
                aoAlterar = viewModel::alternarLembretesDeRetirada,
            )
            aviso?.let { MensagemDeErro(it) }

            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
                    try {
                        contexto.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(contexto, "Não foi possível abrir as configurações do celular.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text("Abrir configurações de notificação do celular", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun Titulo(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
private fun Rotulo(texto: String) {
    Text(text = texto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
}
