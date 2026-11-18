package com.medsafe.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.medsafe.mobile.ui.theme.MedSafeDangerContainer
import com.medsafe.mobile.ui.theme.MedSafeOnDangerContainer

/** Aviso de erro em destaque (fundo avermelhado, texto grande), para falhas de rede ou validacao do servidor. */
@Composable
fun MensagemDeErro(texto: String, modifier: Modifier = Modifier) {
    Surface(
        color = MedSafeDangerContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MedSafeOnDangerContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun Carregando(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp).semantics { contentDescription = "Carregando" },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/** Botao de escolha unica (Entrar/Criar conta, 1x/2x/3x ao dia, Hoje/Semana/Mes): o selecionado fica verde cheio. */
@Composable
fun OpcaoSelecionavel(
    rotulo: String,
    selecionado: Boolean,
    aoSelecionar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = aoSelecionar,
        color = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        contentColor = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(12.dp),
        border = if (selecionado) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.heightIn(min = 52.dp).semantics {
            role = Role.RadioButton
            selected = selecionado
        },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(text = rotulo, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Campo de texto com rotulo acima, mensagem de erro abaixo e, para senhas, botao de mostrar/ocultar. */
@Composable
fun CampoDeTexto(
    rotulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    tipoTeclado: KeyboardType = KeyboardType.Text,
    erro: String? = null,
    ehSenha: Boolean = false,
) {
    var senhaVisivel by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OutlinedTextField(
            value = valor,
            onValueChange = aoAlterar,
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = erro != null,
            supportingText = erro?.let { mensagem -> { Text(mensagem, style = MaterialTheme.typography.bodyLarge) } },
            keyboardOptions = KeyboardOptions(keyboardType = if (ehSenha) KeyboardType.Password else tipoTeclado),
            visualTransformation = if (ehSenha && !senhaVisivel) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (ehSenha) {
                {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                        )
                    }
                }
            } else {
                null
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Linha inteira clicavel (e lida pelo TalkBack como uma chave unica), com o titulo, a explicacao e o interruptor. */
@Composable
fun LinhaComChave(
    titulo: String,
    descricao: String,
    ligada: Boolean,
    aoAlterar: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .toggleable(value = ligada, role = Role.Switch, onValueChange = aoAlterar)
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(descricao, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = ligada, onCheckedChange = null)
        }
    }
}
