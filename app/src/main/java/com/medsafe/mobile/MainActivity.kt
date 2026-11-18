package com.medsafe.mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medsafe.mobile.ui.components.DestinoPrincipal
import com.medsafe.mobile.ui.screens.CadastroMedicamentoScreen
import com.medsafe.mobile.ui.screens.ContatosScreen
import com.medsafe.mobile.ui.screens.DashboardScreen
import com.medsafe.mobile.ui.screens.HistoricoScreen
import com.medsafe.mobile.ui.screens.LoginScreen
import com.medsafe.mobile.ui.theme.MedSafeSeniorTheme

private object Rotas {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CADASTRO_MEDICAMENTO = "cadastro_medicamento"
    const val HISTORICO = "historico"
    const val CONTATOS = "contatos"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedSafeSeniorTheme {
                MedSafeSeniorApp()
            }
        }
    }
}

/**
 * Navegacao do app: Login -> Dashboard <-> Historico <-> Contatos, e Dashboard -> Cadastro de Medicamento.
 * Quem esta logado entra direto no Dashboard; ao sair (ou quando o servidor recusa o token
 * expirado) a pilha e limpa e o app volta para o Login.
 */
@Composable
fun MedSafeSeniorApp(navController: NavHostController = rememberNavController()) {
    val contexto = LocalContext.current
    val sessionStore = remember { (contexto.applicationContext as MedSafeApp).container.sessionStore }
    val sessao by sessionStore.sessao.collectAsStateWithLifecycle()
    val destinoInicial = remember { if (sessionStore.sessao.value != null) Rotas.DASHBOARD else Rotas.LOGIN }

    LaunchedEffect(sessao) {
        if (sessao == null && navController.currentDestination?.route != Rotas.LOGIN) {
            navController.navigate(Rotas.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val aoNavegar: (DestinoPrincipal) -> Unit = { destino ->
        when (destino) {
            DestinoPrincipal.INICIO -> navController.navigate(Rotas.DASHBOARD) {
                popUpTo(Rotas.DASHBOARD)
                launchSingleTop = true
            }
            DestinoPrincipal.HISTORICO -> navController.navigate(Rotas.HISTORICO) {
                popUpTo(Rotas.DASHBOARD)
                launchSingleTop = true
            }
            DestinoPrincipal.CONTATOS -> navController.navigate(Rotas.CONTATOS) {
                popUpTo(Rotas.DASHBOARD)
                launchSingleTop = true
            }
            DestinoPrincipal.REMEDIOS ->
                Toast.makeText(contexto, "Esta tela chega nas próximas entregas", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(navController = navController, startDestination = destinoInicial) {
        composable(Rotas.LOGIN) {
            LoginScreen(
                aoAutenticar = {
                    navController.navigate(Rotas.DASHBOARD) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Rotas.DASHBOARD) {
            DashboardScreen(
                aoAdicionarMedicamento = { navController.navigate(Rotas.CADASTRO_MEDICAMENTO) },
                aoNavegar = aoNavegar,
            )
        }
        composable(Rotas.CADASTRO_MEDICAMENTO) {
            CadastroMedicamentoScreen(
                aoVoltar = { navController.popBackStack() },
                aoSalvar = { navController.popBackStack() },
            )
        }
        composable(Rotas.HISTORICO) {
            HistoricoScreen(aoNavegar = aoNavegar)
        }
        composable(Rotas.CONTATOS) {
            ContatosScreen(aoNavegar = aoNavegar)
        }
    }
}
