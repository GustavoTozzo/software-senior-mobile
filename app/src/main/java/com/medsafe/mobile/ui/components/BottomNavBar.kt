package com.medsafe.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

enum class DestinoPrincipal(val rotulo: String) {
    INICIO("Início"),
    REMEDIOS("Remédios"),
    HISTORICO("Histórico"),
    FARMACIAS("Farmácias"),
}

/**
 * Barra inferior compartilhada. Somente INICIO esta implementado nesta entrega;
 * os demais destinos entram nas proximas entregas (Historico, Farmacias/estoque).
 */
@Composable
fun BottomNavBar(
    destinoAtual: DestinoPrincipal,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
            NavigationBarItem(
                selected = destinoAtual == DestinoPrincipal.INICIO,
                onClick = { },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text(DestinoPrincipal.INICIO.rotulo, textAlign = TextAlign.Center) },
                colors = navBarColors(),
            )
            NavigationBarItem(
                selected = destinoAtual == DestinoPrincipal.REMEDIOS,
                onClick = { },
                icon = { Icon(Icons.Filled.Medication, contentDescription = null) },
                label = { Text(DestinoPrincipal.REMEDIOS.rotulo, textAlign = TextAlign.Center) },
                colors = navBarColors(),
            )
            NavigationBarItem(
                selected = destinoAtual == DestinoPrincipal.HISTORICO,
                onClick = { },
                icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                label = { Text(DestinoPrincipal.HISTORICO.rotulo, textAlign = TextAlign.Center) },
                colors = navBarColors(),
            )
            NavigationBarItem(
                selected = destinoAtual == DestinoPrincipal.FARMACIAS,
                onClick = { },
                icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                label = { Text(DestinoPrincipal.FARMACIAS.rotulo, textAlign = TextAlign.Center) },
                colors = navBarColors(),
            )
        }
    }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
)
