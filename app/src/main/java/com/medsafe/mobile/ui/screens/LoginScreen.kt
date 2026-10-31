package com.medsafe.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medsafe.mobile.ui.components.CampoDeTexto
import com.medsafe.mobile.ui.components.MensagemDeErro
import com.medsafe.mobile.ui.components.OpcaoSelecionavel
import com.medsafe.mobile.ui.viewmodel.AuthViewModel
import com.medsafe.mobile.ui.viewmodel.PerfilUsuario
import com.medsafe.mobile.ui.viewmodel.rememberViewModelFactory

/** Entrar na conta ou criar uma nova (nome, telefone, idade, perfil e senha). */
@Composable
fun LoginScreen(
    aoAutenticar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = rememberViewModelFactory()),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    var modoCadastro by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var idade by rememberSaveable { mutableStateOf("") }
    var perfil by rememberSaveable { mutableStateOf(PerfilUsuario.IDOSO) }
    // A senha nao usa rememberSaveable para nao ser gravada no estado salvo da tela.
    var senha by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Cabecalho()

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OpcaoSelecionavel(
                rotulo = "Entrar",
                selecionado = !modoCadastro,
                aoSelecionar = { modoCadastro = false; viewModel.limparErros() },
                modifier = Modifier.weight(1f),
            )
            OpcaoSelecionavel(
                rotulo = "Criar conta",
                selecionado = modoCadastro,
                aoSelecionar = { modoCadastro = true; viewModel.limparErros() },
                modifier = Modifier.weight(1f),
            )
        }

        if (modoCadastro) {
            CampoDeTexto(
                rotulo = "Nome completo",
                valor = nome,
                aoAlterar = { nome = it },
                placeholder = "Maria da Silva",
                erro = estado.errosPorCampo["nome"],
            )
        }
        CampoDeTexto(
            rotulo = "Telefone",
            valor = telefone,
            aoAlterar = { telefone = it },
            placeholder = "(11) 99999-0000",
            tipoTeclado = KeyboardType.Phone,
            erro = estado.errosPorCampo["telefone"],
        )
        if (modoCadastro) {
            CampoDeTexto(
                rotulo = "Idade (opcional)",
                valor = idade,
                aoAlterar = { idade = it },
                placeholder = "72",
                tipoTeclado = KeyboardType.Number,
                erro = estado.errosPorCampo["idade"],
            )
            Text("Como você vai usar o app?", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PerfilUsuario.entries.forEach { opcao ->
                    OpcaoSelecionavel(
                        rotulo = opcao.rotulo,
                        selecionado = perfil == opcao,
                        aoSelecionar = { perfil = opcao },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        CampoDeTexto(
            rotulo = "Senha",
            valor = senha,
            aoAlterar = { senha = it },
            placeholder = if (modoCadastro) "Pelo menos 6 caracteres" else "Sua senha",
            ehSenha = true,
            erro = estado.errosPorCampo["senha"],
        )

        estado.erro?.let { MensagemDeErro(it) }

        Button(
            onClick = {
                if (modoCadastro) {
                    viewModel.cadastrar(nome, telefone, idade, perfil, senha, aoAutenticar)
                } else {
                    viewModel.entrar(telefone, senha, aoAutenticar)
                }
            },
            enabled = !estado.carregando,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            if (estado.carregando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp),
                )
            } else {
                Text(
                    text = if (modoCadastro) "Criar conta" else "Entrar",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun Cabecalho() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.size(72.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Filled.HealthAndSafety,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Text(
            text = "MedSafe Senior",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Seu lembrete de remédios",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
