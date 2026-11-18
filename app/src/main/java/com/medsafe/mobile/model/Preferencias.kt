package com.medsafe.mobile.model

/** Aparencia do app. "Alto contraste" usa preto sobre branco e bordas fortes, para quem enxerga pouco. */
enum class TemaDoApp(val rotulo: String) {
    AUTOMATICO("Igual ao celular"),
    CLARO("Claro"),
    ESCURO("Escuro"),
    ALTO_CONTRASTE("Alto contraste"),
}

/** Tamanho do texto do app, multiplicado pelo tamanho de fonte que a pessoa ja escolheu no celular. */
enum class TamanhoDoTexto(val rotulo: String, val escala: Float) {
    NORMAL("Normal", 1f),
    GRANDE("Grande", 1.15f),
    MUITO_GRANDE("Muito grande", 1.3f),
}

/** Escolhas da pessoa nos Ajustes; ficam no aparelho e valem para qualquer conta que entrar nele. */
data class Preferencias(
    val tema: TemaDoApp = TemaDoApp.AUTOMATICO,
    val tamanhoDoTexto: TamanhoDoTexto = TamanhoDoTexto.NORMAL,
    val lembretesDeRemedio: Boolean = true,
    val lembretesDeRetirada: Boolean = true,
)

/** O tema escuro vale quando escolhido, ou quando o tema segue o celular e o celular esta no escuro. */
fun TemaDoApp.ehEscuro(celularNoEscuro: Boolean): Boolean = when (this) {
    TemaDoApp.AUTOMATICO -> celularNoEscuro
    TemaDoApp.ESCURO -> true
    TemaDoApp.CLARO, TemaDoApp.ALTO_CONTRASTE -> false
}
