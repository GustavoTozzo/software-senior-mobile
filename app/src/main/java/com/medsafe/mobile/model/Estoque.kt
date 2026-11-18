package com.medsafe.mobile.model

/** Textos e links do aviso de estoque baixo, sem dependencia do Android (para poder ser testada na JVM). */
object Estoque {

    const val MAXIMO_POR_REPOSICAO = 9999

    /** Quanto resta de estoque, em linguagem simples: dias estimados pelo ritmo das doses, ou unidades. */
    fun descricao(quantidade: Int, diasRestantes: Int?): String {
        val unidades = "$quantidade ${if (quantidade == 1) "unidade" else "unidades"}"
        return when {
            quantidade <= 0 -> "O estoque acabou"
            diasRestantes == null -> "Restam $unidades"
            diasRestantes == 0 -> "Dura menos de 1 dia (restam $unidades)"
            diasRestantes == 1 -> "Dá para 1 dia (restam $unidades)"
            else -> "Dá para $diasRestantes dias (restam $unidades)"
        }
    }

    fun mensagemDePedido(nomeDoRemedio: String): String = "Olá! Gostaria de repor o medicamento $nomeDoRemedio."

    /** Link do WhatsApp da farmacia (o proprio, se ela tiver um) ja com o pedido digitado. */
    fun linkDoPedido(farmacia: Farmacia, nomeDoRemedio: String): String {
        val base = farmacia.whatsappLink?.takeIf { it.isNotBlank() } ?: Telefones.linkWhatsApp(farmacia.telefone)
        return Telefones.comMensagem(base, mensagemDePedido(nomeDoRemedio))
    }

    /** Quantidade informada na reposicao: um numero inteiro de 1 ate [MAXIMO_POR_REPOSICAO]; senao null. */
    fun unidadesDaReposicao(texto: String): Int? =
        texto.trim().toIntOrNull()?.takeIf { it in 1..MAXIMO_POR_REPOSICAO }
}
