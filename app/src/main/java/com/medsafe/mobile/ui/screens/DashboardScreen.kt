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
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medsafe.mobile.model.AlertaEstoque
import com.medsafe.mobile.model.DadosExemplo
import com.medsafe.mobile.model.DoseDoDia
import com.medsafe.mobile.model.StatusDose
import com.medsafe.mobile.ui.components.BottomNavBar
import com.medsafe.mobile.ui.components.DestinoPrincipal
import com.medsafe.mobile.ui.theme.MedSafeAccent
import com.medsafe.mobile.ui.theme.MedSafeOnSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeOnWarningContainer
import com.medsafe.mobile.ui.theme.MedSafeSeniorTheme
import com.medsafe.mobile.ui.theme.MedSafeSuccessContainer
import com.medsafe.mobile.ui.theme.MedSafeWarningContainer

/**
 * Dashboard diario com os remedios de hoje e alertas de estoque baixo.
 * Usa DadosExemplo ate a integracao com a API via Retrofit (Entrega 4).
 */
@Composable
fun DashboardScreen(
    nomeUsuario: String,
    onAdicionarMedicamento: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { BottomNavBar(destinoAtual = DestinoPrincipal.INICIO) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CabecalhoDashboard(nomeUsuario)

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
                            onClick = onAdicionarMedicamento,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("+ Novo remédio")
                        }
                    }
                }

                items(DadosExemplo.dosesDeHoje) { dose ->
                    CartaoDeDose(dose)
                }

                if (DadosExemplo.estoqueBaixo.isNotEmpty()) {
                    item {
                        Text(
                            text = "Estoque baixo",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    items(DadosExemplo.estoqueBaixo) { alerta ->
                        CartaoDeEstoqueBaixo(alerta)
                    }
                }
            }
        }
    }
}

@Composable
private fun CabecalhoDashboard(nomeUsuario: String) {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Olá, $nomeUsuario",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = nomeUsuario.firstOrNull()?.uppercase() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
            Text(
                text = "Confira suas doses de hoje",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun CartaoDeDose(dose: DoseDoDia) {
    val corDeFundo = if (dose.status == StatusDose.TOMADO) {
        MedSafeSuccessContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val corDoIcone = if (dose.status == StatusDose.TOMADO) {
        MedSafeOnSuccessContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(color = corDeFundo, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = corDeFundo,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (dose.status == StatusDose.TOMADO) Icons.Filled.Check else Icons.Filled.Medication,
                        contentDescription = null,
                        tint = corDoIcone,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = dose.nomeMedicamento,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (dose.status == StatusDose.TOMADO) {
                        "Tomado às ${dose.horario}"
                    } else {
                        "${dose.horario} - ${dose.instrucao}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = corDoIcone,
                )
            }
            if (dose.status != StatusDose.TOMADO) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedSafeAccent,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                ) {
                    Text("Tomei")
                }
            }
        }
    }
}

@Composable
private fun CartaoDeEstoqueBaixo(alerta: AlertaEstoque) {
    Surface(color = MedSafeWarningContainer, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MedSafeOnWarningContainer,
            )
            Text(
                text = "${alerta.nomeMedicamento}: restam ${alerta.quantidadeRestante} comprimidos",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MedSafeOnWarningContainer,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    MedSafeSeniorTheme {
        DashboardScreen(nomeUsuario = "Maria", onAdicionarMedicamento = { })
    }
}
