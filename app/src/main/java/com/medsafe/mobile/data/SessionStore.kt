package com.medsafe.mobile.data

import android.content.Context
import com.medsafe.mobile.model.Sessao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda o token de login e o usuario no armazenamento privado do app. `sessao` e observavel:
 * quando vira null (logout ou token expirado) a navegacao volta para o login.
 */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    private val _sessao = MutableStateFlow(ler())
    val sessao: StateFlow<Sessao?> = _sessao.asStateFlow()

    fun tokenAtual(): String? = _sessao.value?.token

    fun salvar(sessao: Sessao) {
        prefs.edit()
            .putString(CHAVE_TOKEN, sessao.token)
            .putLong(CHAVE_USUARIO_ID, sessao.usuarioId)
            .putString(CHAVE_USUARIO_NOME, sessao.usuarioNome)
            .apply()
        _sessao.value = sessao
    }

    fun limpar() {
        prefs.edit().clear().apply()
        _sessao.value = null
    }

    private fun ler(): Sessao? {
        val token = prefs.getString(CHAVE_TOKEN, null) ?: return null
        val usuarioId = prefs.getLong(CHAVE_USUARIO_ID, -1L)
        if (usuarioId < 0) return null
        return Sessao(token, usuarioId, prefs.getString(CHAVE_USUARIO_NOME, "") ?: "")
    }

    private companion object {
        const val ARQUIVO = "medsafe_sessao"
        const val CHAVE_TOKEN = "token"
        const val CHAVE_USUARIO_ID = "usuario_id"
        const val CHAVE_USUARIO_NOME = "usuario_nome"
    }
}
