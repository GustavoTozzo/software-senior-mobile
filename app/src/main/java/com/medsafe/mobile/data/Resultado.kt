package com.medsafe.mobile.data

/** Resultado de uma chamada a API: dados ou uma mensagem pronta para mostrar ao usuario. */
sealed interface Resultado<out T> {

    data class Sucesso<T>(val dados: T) : Resultado<T>

    data class Erro(
        val mensagem: String,
        val codigoHttp: Int? = null,
        val camposInvalidos: Map<String, String> = emptyMap(),
    ) : Resultado<Nothing>
}

inline fun <T, R> Resultado<T>.mapear(transformacao: (T) -> R): Resultado<R> = when (this) {
    is Resultado.Sucesso -> Resultado.Sucesso(transformacao(dados))
    is Resultado.Erro -> this
}
