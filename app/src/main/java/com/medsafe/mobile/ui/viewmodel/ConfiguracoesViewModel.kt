package com.medsafe.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsafe.mobile.alarmes.AlarmScheduler
import com.medsafe.mobile.data.MedSafeRepository
import com.medsafe.mobile.data.PreferenciasStore
import com.medsafe.mobile.data.Resultado
import com.medsafe.mobile.model.Preferencias
import com.medsafe.mobile.model.TamanhoDoTexto
import com.medsafe.mobile.model.TemaDoApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Tema, tamanho do texto e avisos. Ligar ou desligar os avisos ja agenda ou cancela os alarmes do celular. */
class ConfiguracoesViewModel(
    private val preferenciasStore: PreferenciasStore,
    private val repositorio: MedSafeRepository,
    private val alarmes: AlarmScheduler,
) : ViewModel() {

    val preferencias: StateFlow<Preferencias> get() = preferenciasStore.preferencias

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    fun escolherTema(tema: TemaDoApp) = preferenciasStore.alterarTema(tema)

    fun escolherTamanhoDoTexto(tamanho: TamanhoDoTexto) = preferenciasStore.alterarTamanhoDoTexto(tamanho)

    fun alternarLembretesDeRemedio(ativos: Boolean) {
        preferenciasStore.alterarLembretesDeRemedio(ativos)
        _aviso.value = null
        viewModelScope.launch {
            if (!ativos) {
                alarmes.cancelarTodos()
                return@launch
            }
            when (val medicamentos = repositorio.medicamentos()) {
                is Resultado.Sucesso -> alarmes.reagendar(medicamentos.dados)
                is Resultado.Erro -> _aviso.value =
                    "Sem conexão agora: os lembretes voltam a funcionar assim que o app carregar seus remédios."
            }
        }
    }
}
