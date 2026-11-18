package com.medsafe.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.model.ContatoEmergencia
import com.medsafe.mobile.model.Farmacia
import com.medsafe.mobile.model.Telefones
import com.medsafe.mobile.ui.components.BottomNavBar
import com.medsafe.mobile.ui.components.Carregando
import com.medsafe.mobile.ui.components.CampoDeTexto
import com.medsafe.mobile.ui.components.DestinoPrincipal
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.components.abrirWhatsApp
import com.medsafe.mobile.ui.components.discar
import com.medsafe.mobile.ui.theme.MedSafeOnSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeSuccessContainer
import com.medsafe.mobile.ui.viewmodel.ContatosViewModel
import com.medsafe.mobile.ui.viewmodel.FormularioContato
import com.medsafe.mobile.ui.viewmodel.TipoContato
import com.medsafe.mobile.ui.viewmodel.rememberViewModelFactory

/** Exclusao aguardando confirmacao: guarda o que mostrar no dialogo e o que fazer se a pessoa confirmar. */
private class ExclusaoPendente(val nome: String, val confirmar: () -> Unit)

/** Farmacias (com a preferida, que recebe o pedido de reposicao) e familiares, cada um a um toque de ligar ou chamar no WhatsApp. */
@Composable
fun ContatosScreen(
    aoNavegar: (DestinoPrincipal) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContatosViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var exclusaoPendente by remember { mutableStateOf<ExclusaoPendente?>(null) }

    LaunchedEffect(Unit) { viewModel.carregar() }

    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavBar(destinoAtual = DestinoPrincipal.CONTATOS, aoNavegar = aoNavegar) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        text = "Contatos",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = "Farmácias e familiares a um toque",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                estado.erro?.let { erro ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MensagemDeErro(erro)
                            OutlinedButton(onClick = viewModel::carregar) { Text("Tentar de novo") }
                        }
                    }
                }

                item {
                    TituloDaSecao("Minhas farmácias", "+ Farmácia") { viewModel.abrirFormulario(TipoContato.FARMACIA) }
                }
                if (estado.carregando) {
                    item { Carregando() }
                } else if (estado.farmacias.isEmpty() && estado.erro == null) {
                    item {
                        Texto("Cadastre a farmácia onde você compra seus remédios. É para ela que o aviso de estoque manda o pedido no WhatsApp.")
                    }
                }
                items(estado.farmacias, key = { "farmacia-${it.id}" }) { farmacia ->
                    CartaoDeContato(
                        nome = farmacia.nome,
                        detalhe = Telefones.formatar(farmacia.telefone),
                        selo = if (farmacia.preferida) "Preferida" else null,
                        aoLigar = { contexto.discar(farmacia.telefone) },
                        aoChamarNoWhatsApp = {
                            contexto.abrirWhatsApp(farmacia.whatsappLink ?: Telefones.linkWhatsApp(farmacia.telefone))
                        },
                        textoDaAcaoExtra = if (farmacia.preferida) null else "Usar como preferida",
                        aoAcaoExtra = { viewModel.tornarPreferida(farmacia) },
                        aoExcluir = { exclusaoPendente = ExclusaoPendente(farmacia.nome) { viewModel.excluirFarmacia(farmacia) } },
                    )
                }

                item {
                    TituloDaSecao("Familiares e cuidadores", "+ Contato") { viewModel.abrirFormulario(TipoContato.FAMILIAR) }
                }
                if (!estado.carregando && estado.familiares.isEmpty() && estado.erro == null) {
                    item { Texto("Adicione quem pode ajudar você: filhos, vizinhos ou cuidadores.") }
                }
                items(estado.familiares, key = { "familiar-${it.id}" }) { contato ->
                    CartaoDeContato(
                        nome = contato.nome,
                        detalhe = descricaoDoFamiliar(contato),
                        selo = null,
                        aoLigar = { contexto.discar(contato.telefone) },
                        aoChamarNoWhatsApp = { contexto.abrirWhatsApp(Telefones.linkWhatsApp(contato.telefone)) },
                        textoDaAcaoExtra = null,
                        aoAcaoExtra = {},
                        aoExcluir = { exclusaoPendente = ExclusaoPendente(contato.nome) { viewModel.excluirFamiliar(contato) } },
                    )
                }
            }
        }
    }

    estado.formulario?.let { formulario ->
        DialogoDeCadastro(
            formulario = formulario,
            aoAlterarNome = viewModel::alterarNome,
            aoAlterarTelefone = viewModel::alterarTelefone,
            aoAlterarParentesco = viewModel::alterarParentesco,
            aoSalvar = viewModel::salvar,
            aoCancelar = viewModel::fecharFormulario,
        )
    }

    exclusaoPendente?.let { pendente ->
        AlertDialog(
            onDismissRequest = { exclusaoPendente = null },
            title = { Text("Remover ${pendente.nome}?") },
            text = { Text("O contato sai da sua lista. Você pode cadastrá-lo de novo quando quiser.") },
            confirmButton = {
                TextButton(onClick = { exclusaoPendente = null; pendente.confirmar() }) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { exclusaoPendente = null }) { Text("Manter") } },
        )
    }
}

private fun descricaoDoFamiliar(contato: ContatoEmergencia): String {
    val telefone = Telefones.formatar(contato.telefone)
    return if (contato.parentesco.isNullOrBlank()) telefone else "${contato.parentesco} · $telefone"
}

@Composable
private fun TituloDaSecao(titulo: String, textoDoBotao: String, aoAdicionar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = aoAdicionar) { Text(textoDoBotao) }
    }
}

@Composable
private fun Texto(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CartaoDeContato(
    nome: String,
    detalhe: String,
    selo: String?,
    aoLigar: () -> Unit,
    aoChamarNoWhatsApp: () -> Unit,
    textoDaAcaoExtra: String?,
    aoAcaoExtra: () -> Unit,
    aoExcluir: () -> Unit,
) {
    Surface(
        color = if (selo != null) MedSafeSuccessContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = detalhe,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (selo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MedSafeOnSuccessContainer)
                        Text(
                            text = selo,
                            style = MaterialTheme.typography.labelLarge,
                            color = MedSafeOnSuccessContainer,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = aoLigar, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Filled.Call, contentDescription = null)
                    Text("Ligar", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(onClick = aoChamarNoWhatsApp, modifier = Modifier.weight(1f).height(52.dp)) {
                    Text("WhatsApp", style = MaterialTheme.typography.labelLarge)
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                if (textoDaAcaoExtra != null) {
                    TextButton(onClick = aoAcaoExtra) { Text(textoDaAcaoExtra, style = MaterialTheme.typography.labelLarge) }
                } else {
                    Text("")
                }
                TextButton(onClick = aoExcluir) {
                    Text("Remover", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DialogoDeCadastro(
    formulario: FormularioContato,
    aoAlterarNome: (String) -> Unit,
    aoAlterarTelefone: (String) -> Unit,
    aoAlterarParentesco: (String) -> Unit,
    aoSalvar: () -> Unit,
    aoCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!formulario.salvando) aoCancelar() },
        title = { Text(formulario.tipo.titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                formulario.erro?.let { MensagemDeErro(it) }
                CampoDeTexto(
                    rotulo = if (formulario.tipo == TipoContato.FARMACIA) "Nome da farmácia" else "Nome",
                    valor = formulario.nome,
                    aoAlterar = aoAlterarNome,
                    placeholder = if (formulario.tipo == TipoContato.FARMACIA) "Farmácia Popular Centro" else "Maria",
                    erro = formulario.errosPorCampo["nome"],
                )
                if (formulario.tipo == TipoContato.FAMILIAR) {
                    CampoDeTexto(
                        rotulo = "Quem é (opcional)",
                        valor = formulario.parentesco,
                        aoAlterar = aoAlterarParentesco,
                        placeholder = "Filha, vizinho, cuidadora...",
                        erro = formulario.errosPorCampo["parentesco"],
                    )
                }
                CampoDeTexto(
                    rotulo = "Telefone com DDD",
                    valor = formulario.telefone,
                    aoAlterar = aoAlterarTelefone,
                    placeholder = "(11) 91234-5678",
                    tipoTeclado = KeyboardType.Phone,
                    erro = formulario.errosPorCampo["telefone"],
                )
            }
        },
        confirmButton = {
            TextButton(onClick = aoSalvar, enabled = !formulario.salvando) {
                Text(if (formulario.salvando) "Salvando..." else "Salvar", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar, enabled = !formulario.salvando) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}
