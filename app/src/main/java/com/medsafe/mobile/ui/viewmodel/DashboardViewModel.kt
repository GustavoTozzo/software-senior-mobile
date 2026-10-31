package com.medsafe.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsafe.mobile.alarmes.AlarmScheduler
import com.medsafe.mobile.data.MedSafeRepository
import com.medsafe.mobile.data.Resultado
import com.medsafe.mobile.model.Dose
import com.medsafe.mobile.model.Medicamento
import com.medsafe.mobile.model.Sessao
import com.medsafe.mobile.model.StatusDose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val carregando: Boolean = true,
    val doses: List<Dose> = emptyList(),
    val estoqueBaixo: List<Medicamento> = emptyList(),
    val erro: String? = null,
    val doseEmAtualizacao: Long? = null,
)

class DashboardViewModel(
    private val repositorio: MedSafeRepository,
    private val alarmes: AlarmScheduler,
) : ViewModel() {

    private val _estado = MutableStateFlow(DashboardUiState())
    val estado: StateFlow<DashboardUiState> = _estado.asStateFlow()

    val sessao: StateFlow<Sessao?> get() = repositorio.sessao

    /** Busca as doses de hoje e os medicamentos e, com eles, atualiza os alarmes do celular. */
    fun carregar(mostrarCarregando: Boolean = true) {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = mostrarCarregando || it.doses.isEmpty(), erro = null) }

            // A data vem do aparelho: "hoje" do usuario nem sempre e o "hoje" do servidor (fusos diferentes).
            when (val agenda = repositorio.agenda(LocalDate.now())) {
                is Resultado.Sucesso -> _estado.update { it.copy(doses = agenda.dados) }
                is Resultado.Erro -> {
                    _estado.update { it.copy(carregando = false, erro = agenda.mensagem) }
                    return@launch
                }
            }

            when (val medicamentos = repositorio.medicamentos()) {
                is Resultado.Sucesso -> {
                    _estado.update { it.copy(estoqueBaixo = medicamentos.dados.filter { m -> m.estoqueBaixo }) }
                    alarmes.reagendar(medicamentos.dados)
                }
                is Resultado.Erro -> _estado.update { it.copy(erro = medicamentos.mensagem) }
            }
            _estado.update { it.copy(carregando = false) }
        }
    }

    fun marcarTomado(dose: Dose) = atualizarStatus(dose, StatusDose.TOMADO)

    fun marcarPulado(dose: Dose) = atualizarStatus(dose, StatusDose.PULADO)

    private fun atualizarStatus(dose: Dose, status: StatusDose) {
        viewModelScope.launch {
            _estado.update { it.copy(doseEmAtualizacao = dose.id, erro = null) }
            when (val resultado = repositorio.atualizarStatus(dose.id, status)) {
                is Resultado.Sucesso -> {
                    _estado.update { it.copy(doseEmAtualizacao = null) }
                    carregar(mostrarCarregando = false)
                }
                is Resultado.Erro -> _estado.update {
                    it.copy(doseEmAtualizacao = null, erro = resultado.mensagem)
                }
            }
        }
    }

    fun sair() {
        alarmes.cancelarTodos()
        repositorio.sair()
    }
}
