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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

enum class DestinoPrincipal(val rotulo: String, val icone: ImageVector) {
    INICIO("Início", Icons.Filled.Home),
    REMEDIOS("Remédios", Icons.Filled.Medication),
    HISTORICO("Histórico", Icons.Filled.DateRange),
    FARMACIAS("Farmácias", Icons.Filled.LocationOn),
}

/** Barra inferior compartilhada entre as telas principais. */
@Composable
fun BottomNavBar(
    destinoAtual: DestinoPrincipal,
    aoNavegar: (DestinoPrincipal) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
            DestinoPrincipal.entries.forEach { destino ->
                NavigationBarItem(
                    selected = destino == destinoAtual,
                    onClick = { aoNavegar(destino) },
                    icon = { Icon(destino.icone, contentDescription = null) },
                    label = { Text(destino.rotulo, textAlign = TextAlign.Center) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
        }
    }
}
