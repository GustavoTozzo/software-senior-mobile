package com.medsafe.mobile.model

import java.time.LocalDate
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
    /** Dias que o estoque cobre no ritmo das doses; null quando nao ha horarios para estimar. */
    val diasRestantes: Int?,
    val frequencia: String,
    val horarios: List<String>,
    /** Retirado na Farmacia Popular: o app lembra quando o proximo fornecimento e liberado. */
    val farmaciaPopular: Boolean = false,
    val ultimaRetirada: LocalDate? = null,
    val intervaloRetiradaDias: Int = 30,
    val proximaRetirada: LocalDate? = null,
) {
    val dosesPorDia: Int get() = horarios.size
}

data class Farmacia(
    val id: Long,
    val nome: String,
    val telefone: String,
    val whatsappLink: String?,
    val preferida: Boolean,
)

/** Familiar ou cuidador que a pessoa quer alcancar com um toque. */
data class ContatoEmergencia(
    val id: Long,
    val nome: String,
    val parentesco: String?,
    val telefone: String,
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
