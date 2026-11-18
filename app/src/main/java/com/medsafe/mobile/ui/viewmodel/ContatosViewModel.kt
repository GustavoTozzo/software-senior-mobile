package com.medsafe.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsafe.mobile.data.MedSafeRepository
import com.medsafe.mobile.data.Resultado
import com.medsafe.mobile.model.ContatoEmergencia
import com.medsafe.mobile.model.Farmacia
import com.medsafe.mobile.model.Telefones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TipoContato(val titulo: String) {
    FARMACIA("Nova farmácia"),
    FAMILIAR("Novo contato"),
}

/** Campos do dialogo de cadastro; [parentesco] so e usado para familiares. */
data class FormularioContato(
    val tipo: TipoContato,
    val nome: String = "",
    val telefone: String = "",
    val parentesco: String = "",
    val salvando: Boolean = false,
    val erro: String? = null,
    val errosPorCampo: Map<String, String> = emptyMap(),
)

data class ContatosUiState(
    val carregando: Boolean = true,
    val farmacias: List<Farmacia> = emptyList(),
    val familiares: List<ContatoEmergencia> = emptyList(),
    val erro: String? = null,
    val formulario: FormularioContato? = null,
)

/** Confere nome e telefone (com DDD) antes de chamar a API; as chaves sao as mesmas dos erros do servidor. */
fun validarContato(nome: String, telefone: String): Map<String, String> {
    val problemas = mutableMapOf<String, String>()
    if (nome.isBlank()) problemas["nome"] = "Informe o nome"
    if (Telefones.apenasDigitos(telefone).length !in 10..13) {
        problemas["telefone"] = "Informe o telefone com DDD, por exemplo (11) 91234-5678"
    }
    return problemas
}

class ContatosViewModel(private val repositorio: MedSafeRepository) : ViewModel() {

    private val _estado = MutableStateFlow(ContatosUiState())
    val estado: StateFlow<ContatosUiState> = _estado.asStateFlow()

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = it.farmacias.isEmpty() && it.familiares.isEmpty(), erro = null) }

            var erro: String? = null
            when (val farmacias = repositorio.farmacias()) {
                is Resultado.Sucesso -> _estado.update { it.copy(farmacias = farmacias.dados) }
                is Resultado.Erro -> erro = farmacias.mensagem
            }
            when (val familiares = repositorio.contatos()) {
                is Resultado.Sucesso -> _estado.update { it.copy(familiares = familiares.dados) }
                is Resultado.Erro -> erro = erro ?: familiares.mensagem
            }
            _estado.update { it.copy(carregando = false, erro = erro) }
        }
    }

    fun abrirFormulario(tipo: TipoContato) = _estado.update { it.copy(formulario = FormularioContato(tipo)) }

    fun fecharFormulario() = _estado.update { it.copy(formulario = null) }

    fun alterarNome(nome: String) = alterarFormulario { it.copy(nome = nome, errosPorCampo = it.errosPorCampo - "nome") }

    fun alterarTelefone(telefone: String) =
        alterarFormulario { it.copy(telefone = telefone, errosPorCampo = it.errosPorCampo - "telefone") }

    fun alterarParentesco(parentesco: String) = alterarFormulario { it.copy(parentesco = parentesco) }

    fun salvar() {
        val formulario = _estado.value.formulario ?: return
        if (formulario.salvando) return

        val problemas = validarContato(formulario.nome, formulario.telefone)
        if (problemas.isNotEmpty()) {
            alterarFormulario { it.copy(erro = null, errosPorCampo = problemas) }
            return
        }

        viewModelScope.launch {
            alterarFormulario { it.copy(salvando = true, erro = null, errosPorCampo = emptyMap()) }
            val nome = formulario.nome.trim()
            val resultado: Resultado<*> = when (formulario.tipo) {
                TipoContato.FARMACIA -> repositorio.criarFarmacia(nome, formulario.telefone)
                TipoContato.FAMILIAR -> repositorio.criarContato(nome, formulario.parentesco.trim(), formulario.telefone)
            }
            when (resultado) {
                is Resultado.Sucesso -> {
                    _estado.update { it.copy(formulario = null) }
                    carregar()
                }
                is Resultado.Erro -> alterarFormulario {
                    it.copy(salvando = false, erro = resultado.mensagem, errosPorCampo = resultado.camposInvalidos)
                }
            }
        }
    }

    fun tornarPreferida(farmacia: Farmacia) = executar { repositorio.definirFarmaciaPreferida(farmacia) }

    fun excluirFarmacia(farmacia: Farmacia) = executar { repositorio.excluirFarmacia(farmacia.id) }

    fun excluirFamiliar(contato: ContatoEmergencia) = executar { repositorio.excluirContato(contato.id) }

    /** Roda uma alteracao e recarrega as listas (a preferida pode mudar de dono, entao nao basta mexer em um item). */
    private fun executar(acao: suspend () -> Resultado<*>) {
        viewModelScope.launch {
            _estado.update { it.copy(erro = null) }
            when (val resultado = acao()) {
                is Resultado.Sucesso -> carregar()
                is Resultado.Erro -> _estado.update { it.copy(erro = resultado.mensagem) }
            }
        }
    }

    private fun alterarFormulario(transformacao: (FormularioContato) -> FormularioContato) {
        _estado.update { estado -> estado.copy(formulario = estado.formulario?.let(transformacao)) }
    }
}
