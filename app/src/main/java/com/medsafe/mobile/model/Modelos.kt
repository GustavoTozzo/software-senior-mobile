package com.medsafe.mobile.model

import java.time.LocalDateTime

enum class StatusDose {
    PENDENTE,
    TOMADO,
    PULADO,
}

data class Medicamento(
    val id: Long,
    val nome: String,
    val codigoBarras: String?,
    val quantidade: Int,
    val estoqueBaixo: Boolean,
    val frequencia: String,
    val horarios: List<String>,
)

data class Dose(
    val id: Long,
    val medicamentoId: Long,
    val medicamentoNome: String,
    val programadaPara: LocalDateTime,
    val realizadaEm: LocalDateTime?,
    val status: StatusDose,
)

data class Sessao(
    val token: String,
    val usuarioId: Long,
    val usuarioNome: String,
)
