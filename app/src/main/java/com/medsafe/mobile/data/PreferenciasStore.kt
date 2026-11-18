package com.medsafe.mobile.data

import android.content.Context
import com.medsafe.mobile.model.Preferencias
import com.medsafe.mobile.model.TamanhoDoTexto
import com.medsafe.mobile.model.TemaDoApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda as preferencias do app no aparelho. Sair da conta nao apaga: tema e fonte sao da pessoa, nao da conta. */
class PreferenciasStore(context: Context) {

    private val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    private val _preferencias = MutableStateFlow(ler())
    val preferencias: StateFlow<Preferencias> = _preferencias.asStateFlow()

    fun alterarTema(tema: TemaDoApp) = salvar(_preferencias.value.copy(tema = tema))

    fun alterarTamanhoDoTexto(tamanho: TamanhoDoTexto) = salvar(_preferencias.value.copy(tamanhoDoTexto = tamanho))

    fun alterarLembretesDeRemedio(ativos: Boolean) = salvar(_preferencias.value.copy(lembretesDeRemedio = ativos))

    fun alterarLembretesDeRetirada(ativos: Boolean) = salvar(_preferencias.value.copy(lembretesDeRetirada = ativos))

    private fun salvar(novas: Preferencias) {
        prefs.edit()
            .putString(CHAVE_TEMA, novas.tema.name)
            .putString(CHAVE_TAMANHO, novas.tamanhoDoTexto.name)
            .putBoolean(CHAVE_LEMBRETES_REMEDIO, novas.lembretesDeRemedio)
            .putBoolean(CHAVE_LEMBRETES_RETIRADA, novas.lembretesDeRetirada)
            .apply()
        _preferencias.value = novas
    }

    private fun ler() = Preferencias(
        tema = enumOu(prefs.getString(CHAVE_TEMA, null), TemaDoApp.AUTOMATICO),
        tamanhoDoTexto = enumOu(prefs.getString(CHAVE_TAMANHO, null), TamanhoDoTexto.NORMAL),
        lembretesDeRemedio = prefs.getBoolean(CHAVE_LEMBRETES_REMEDIO, true),
        lembretesDeRetirada = prefs.getBoolean(CHAVE_LEMBRETES_RETIRADA, true),
    )

    private inline fun <reified E : Enum<E>> enumOu(nome: String?, padrao: E): E =
        nome?.let { runCatching { enumValueOf<E>(it) }.getOrNull() } ?: padrao

    private companion object {
        const val ARQUIVO = "medsafe_preferencias"
        const val CHAVE_TEMA = "tema"
        const val CHAVE_TAMANHO = "tamanho_do_texto"
        const val CHAVE_LEMBRETES_REMEDIO = "lembretes_remedio"
        const val CHAVE_LEMBRETES_RETIRADA = "lembretes_retirada"
    }
}
