package com.medsafe.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsafe.mobile.ui.theme.MedSafeSeniorTheme

/**
 * Cadastro/login do usuario (idoso ou cuidador). Ainda sem autenticacao real —
 * "Entrar" apenas avanca para o Dashboard; o backend (Spring Security + JWT) entra na Entrega 4.
 */
@Composable
fun LoginScreen(
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var idade by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 48.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 12.dp),
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

        Spacer(modifier = Modifier.height(40.dp))

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CampoDeTexto(
                rotulo = "Nome completo",
                valor = nome,
                aoAlterar = { nome = it },
                placeholder = "Maria da Silva",
            )
            CampoDeTexto(
                rotulo = "Telefone",
                valor = telefone,
                aoAlterar = { telefone = it },
                placeholder = "(11) 99999-0000",
                tipoTeclado = KeyboardType.Phone,
            )
            CampoDeTexto(
                rotulo = "Idade",
                valor = idade,
                aoAlterar = { idade = it },
                placeholder = "72",
                tipoTeclado = KeyboardType.Number,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onEntrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Entrar", style = MaterialTheme.typography.titleLarge)
        }

        TextButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ainda não tenho conta", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun CampoDeTexto(
    rotulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    placeholder: String,
    tipoTeclado: KeyboardType = KeyboardType.Text,
) {
    Column {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = valor,
            onValueChange = aoAlterar,
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = tipoTeclado),
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MedSafeSeniorTheme {
        LoginScreen(onEntrar = { })
    }
}
