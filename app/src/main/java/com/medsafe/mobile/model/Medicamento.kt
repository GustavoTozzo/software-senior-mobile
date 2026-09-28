package com.medsafe.mobile.model

enum class StatusDose {
    PENDENTE,
    TOMADO,
    PULADO,
}

data class DoseDoDia(
    val id: Long,
    val nomeMedicamento: String,
    val horario: String,
    val instrucao: String,
    val status: StatusDose,
)

data class AlertaEstoque(
    val nomeMedicamento: String,
    val quantidadeRestante: Int,
)

/**
 * Dados de exemplo para as telas ate a integracao real com a API via Retrofit (Entrega 4).
 */
object DadosExemplo {
    val dosesDeHoje = listOf(
        DoseDoDia(1, "Losartana 50mg", "08:00", "em jejum", StatusDose.PENDENTE),
        DoseDoDia(2, "Metformina 850mg", "12:30", "apos o almoco", StatusDose.PENDENTE),
        DoseDoDia(3, "Vitamina D", "07:15", "tomado as 07:15", StatusDose.TOMADO),
    )

    val estoqueBaixo = listOf(
        AlertaEstoque("Losartana", 3),
    )
}
