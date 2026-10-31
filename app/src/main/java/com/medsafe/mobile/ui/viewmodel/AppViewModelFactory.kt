package com.medsafe.mobile.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medsafe.mobile.AppContainer
import com.medsafe.mobile.MedSafeApp

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(container.repository) as T
        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(container.repository) as T
        modelClass.isAssignableFrom(CadastroMedicamentoViewModel::class.java) ->
            CadastroMedicamentoViewModel(container.repository) as T
        else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}

@Composable
fun rememberViewModelFactory(): AppViewModelFactory {
    val contexto = LocalContext.current.applicationContext
    return remember(contexto) { AppViewModelFactory((contexto as MedSafeApp).container) }
}
