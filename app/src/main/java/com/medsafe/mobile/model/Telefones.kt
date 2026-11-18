package com.medsafe.mobile.model

import java.net.URLEncoder

/** Formatacao e links de telefone/WhatsApp, sem depender do Android (para poder ser testada na JVM). */
object Telefones {

    fun apenasDigitos(texto: String): String = texto.filter { it.isDigit() }

    /** "(11) 91234-5678" ou "(11) 3333-4444"; qualquer outro tamanho volta como foi digitado. */
    fun formatar(telefone: String): String {
        val digitos = apenasDigitos(telefone)
        return when (digitos.length) {
            11 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 7)}-${digitos.substring(7)}"
            10 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 6)}-${digitos.substring(6)}"
            else -> telefone
        }
    }

    /**
     * Numero com DDD tem 10 ou 11 digitos; so com 12 ou mais o "55" inicial e o codigo do pais
     * (senao pode ser o DDD 55, do interior do Rio Grande do Sul).
     */
    fun comCodigoDoPais(telefone: String): String {
        val digitos = apenasDigitos(telefone)
        return if (digitos.length >= 12 && digitos.startsWith("55")) digitos else "55$digitos"
    }

    fun linkWhatsApp(telefone: String, mensagem: String? = null): String {
        val base = "https://wa.me/${comCodigoDoPais(telefone)}"
        return if (mensagem == null) base else comMensagem(base, mensagem)
    }

    /** Acrescenta o texto pronto a um link de WhatsApp existente (o da farmacia pode ser personalizado). */
    fun comMensagem(link: String, mensagem: String): String {
        val separador = if ('?' in link) '&' else '?'
        return "$link${separador}text=${codificar(mensagem)}"
    }

    /** Endereco "tel:" para abrir o discador com o numero ja digitado. */
    fun enderecoDeDiscagem(telefone: String): String = "tel:${apenasDigitos(telefone)}"

    private fun codificar(texto: String): String = URLEncoder.encode(texto, "UTF-8").replace("+", "%20")
}
