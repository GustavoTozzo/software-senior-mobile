package com.medsafe.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsafe.mobile.data.MedSafeRepository
import com.medsafe.mobile.data.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Quantas vezes ao dia o remedio e tomado; ja sugere os horarios mais comuns. */
enum class Frequencia(val rotulo: String, val horariosSugeridos: List<String>) {
    UMA_VEZ("1x ao dia", listOf("08:00")),
    DUAS_VEZES("2x ao dia", listOf("08:00", "20:00")),
    TRES_VEZES("3x ao dia", listOf("08:00", "14:00", "20:00")),
}

data class CadastroUiState(
    val nome: String = "",
    val codigoBarras: String = "",
    val quantidade: Int = 30,
    val frequencia: Frequencia = Frequencia.UMA_VEZ,
    val horarios: List<String> = Frequencia.UMA_VEZ.horariosSugeridos,
    val salvando: Boolean = false,
    val erro: String? = null,
    val errosPorCampo: Map<String, String> = emptyMap(),
    val salvo: Boolean = false,
)

class CadastroMedicamentoViewModel(private val repositorio: MedSafeRepository) : ViewModel() {

    private val _estado = MutableStateFlow(CadastroUiState())
    val estado: StateFlow<CadastroUiState> = _estado.asStateFlow()

    fun alterarNome(nome: String) = _estado.update { it.copy(nome = nome, errosPorCampo = it.errosPorCampo - "nomeComercial") }

    fun alterarCodigoBarras(codigo: String) = _estado.update { it.copy(codigoBarras = codigo.trim()) }

    fun aumentarQuantidade() = _estado.update { it.copy(quantidade = it.quantidade + 1) }

    fun diminuirQuantidade() = _estado.update { it.copy(quantidade = maxOf(0, it.quantidade - 1)) }

    fun escolherFrequencia(frequencia: Frequencia) = _estado.update {
        it.copy(frequencia = frequencia, horarios = frequencia.horariosSugeridos, errosPorCampo = it.errosPorCampo - "horariosProgramados")
    }

    fun adicionarHorario(horario: String) = _estado.update {
        if (horario in it.horarios) it else it.copy(horarios = (it.horarios + horario).sorted(), errosPorCampo = it.errosPorCampo - "horariosProgramados")
    }

    fun removerHorario(horario: String) = _estado.update { it.copy(horarios = it.horarios - horario) }

    fun salvar() {
        val atual = _estado.value
        val problemas = mutableMapOf<String, String>()
        if (atual.nome.isBlank()) problemas["nomeComercial"] = "Informe o nome do remédio"
        if (atual.horarios.isEmpty()) problemas["horariosProgramados"] = "Adicione pelo menos um horário"
        if (problemas.isNotEmpty()) {
            _estado.update { it.copy(erro = null, errosPorCampo = problemas) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(salvando = true, erro = null, errosPorCampo = emptyMap()) }
            val resultado = repositorio.criarMedicamento(
                nome = atual.nome.trim(),
                codigoBarras = atual.codigoBarras,
                quantidade = atual.quantidade,
                frequencia = atual.frequencia.rotulo,
                horarios = atual.horarios,
            )
            when (resultado) {
                is Resultado.Sucesso -> _estado.update { it.copy(salvando = false, salvo = true) }
                is Resultado.Erro -> _estado.update {
                    it.copy(salvando = false, erro = resultado.mensagem, errosPorCampo = resultado.camposInvalidos)
                }
            }
        }
    }
}
