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

enum class PerfilUsuario(val rotulo: String, val valorApi: String) {
    IDOSO("Sou idoso", "IDOSO"),
    CUIDADOR("Sou cuidador", "CUIDADOR"),
}

data class AuthUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val errosPorCampo: Map<String, String> = emptyMap(),
)

class AuthViewModel(private val repositorio: MedSafeRepository) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    fun entrar(telefone: String, senha: String, aoConcluir: () -> Unit) {
        val problemas = validarTelefoneESenha(telefone, senha, exigirSenhaLonga = false)
        if (problemas.isNotEmpty()) {
            _estado.update { it.copy(erro = null, errosPorCampo = problemas) }
            return
        }
        executar(aoConcluir) { repositorio.entrar(telefone, senha) }
    }

    fun cadastrar(
        nome: String,
        telefone: String,
        idade: String,
        perfil: PerfilUsuario,
        senha: String,
        aoConcluir: () -> Unit,
    ) {
        val problemas = validarTelefoneESenha(telefone, senha, exigirSenhaLonga = true).toMutableMap()
        if (nome.isBlank()) problemas["nome"] = "Informe seu nome"
        val idadeNumero = idade.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        if (idade.isNotBlank() && idadeNumero == null) problemas["idade"] = "Use apenas números"
        if (problemas.isNotEmpty()) {
            _estado.update { it.copy(erro = null, errosPorCampo = problemas) }
            return
        }
        executar(aoConcluir) {
            repositorio.registrar(nome.trim(), telefone, idadeNumero, perfil.valorApi, senha)
        }
    }

    fun limparErros() {
        _estado.update { it.copy(erro = null, errosPorCampo = emptyMap()) }
    }

    private fun executar(aoConcluir: () -> Unit, chamada: suspend () -> Resultado<*>) {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null, errosPorCampo = emptyMap()) }
            when (val resultado = chamada()) {
                is Resultado.Sucesso -> {
                    _estado.update { it.copy(carregando = false) }
                    aoConcluir()
                }
                is Resultado.Erro -> _estado.update {
                    it.copy(carregando = false, erro = resultado.mensagem, errosPorCampo = resultado.camposInvalidos)
                }
            }
        }
    }

    private fun validarTelefoneESenha(
        telefone: String,
        senha: String,
        exigirSenhaLonga: Boolean,
    ): Map<String, String> {
        val problemas = mutableMapOf<String, String>()
        if (telefone.count { it.isDigit() } < 10) {
            problemas["telefone"] = "Informe o telefone com DDD"
        }
        if (senha.isBlank()) {
            problemas["senha"] = "Informe sua senha"
        } else if (exigirSenhaLonga && senha.length < 6) {
            problemas["senha"] = "A senha precisa ter pelo menos 6 caracteres"
        }
        return problemas
    }
}
