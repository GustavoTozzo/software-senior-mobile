package com.medsafe.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medsafe.mobile.ui.screens.CadastroMedicamentoScreen
import com.medsafe.mobile.ui.screens.DashboardScreen
import com.medsafe.mobile.ui.screens.LoginScreen
import com.medsafe.mobile.ui.theme.MedSafeSeniorTheme

private object Rotas {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CADASTRO_MEDICAMENTO = "cadastro_medicamento"
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
 * Navegacao entre as telas da Entrega 3: Login/Cadastro -> Dashboard -> Cadastro de
 * Medicamento. Ainda sem autenticacao ou persistencia reais — isso entra com o
 * Retrofit e o Spring Security na Entrega 4.
 */
@Composable
fun MedSafeSeniorApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rotas.LOGIN) {
        composable(Rotas.LOGIN) {
            LoginScreen(
                onEntrar = {
                    navController.navigate(Rotas.DASHBOARD) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Rotas.DASHBOARD) {
            DashboardScreen(
                nomeUsuario = "Maria",
                onAdicionarMedicamento = { navController.navigate(Rotas.CADASTRO_MEDICAMENTO) },
            )
        }
        composable(Rotas.CADASTRO_MEDICAMENTO) {
            CadastroMedicamentoScreen(
                onVoltar = { navController.popBackStack() },
                onSalvar = { navController.popBackStack() },
            )
        }
    }
}
