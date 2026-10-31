package com.medsafe.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsafe.mobile.data.MedSafeRepository
import com.medsafe.mobile.data.Resultado
import com.medsafe.mobile.model.Dose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class PeriodoHistorico(val rotulo: String, private val diasParaTras: Long) {
    HOJE("Hoje", 0),
    SEMANA("Semana", 6),
    MES("Mês", 29);

    fun inicio(hoje: LocalDate): LocalDate = hoje.minusDays(diasParaTras)
}

data class HistoricoUiState(
    val periodo: PeriodoHistorico = PeriodoHistorico.SEMANA,
    val carregando: Boolean = true,
    val doses: List<Dose> = emptyList(),
    val erro: String? = null,
)

class HistoricoViewModel(private val repositorio: MedSafeRepository) : ViewModel() {

    private val _estado = MutableStateFlow(HistoricoUiState())
    val estado: StateFlow<HistoricoUiState> = _estado.asStateFlow()

    fun carregar(periodo: PeriodoHistorico = _estado.value.periodo) {
        viewModelScope.launch {
            _estado.update { it.copy(periodo = periodo, carregando = true, erro = null) }
            val hoje = LocalDate.now()
            when (val resultado = repositorio.historico(periodo.inicio(hoje), hoje)) {
                is Resultado.Sucesso -> _estado.update { it.copy(carregando = false, doses = resultado.dados) }
                is Resultado.Erro -> _estado.update { it.copy(carregando = false, erro = resultado.mensagem) }
            }
        }
    }
}
