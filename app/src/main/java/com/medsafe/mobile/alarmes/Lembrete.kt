package com.medsafe.mobile.alarmes

import java.time.LocalDate

/** Um aviso diario: tomar [nome] as [horario] (formato HH:mm). */
data class Lembrete(
    val medicamentoId: Long,
    val nome: String,
    val horario: String,
) {
    /** Identifica o alarme no AlarmManager: um por medicamento e horario. */
    val codigo: Int get() = "$medicamentoId|$horario".hashCode()
}

/** Aviso unico: o proximo fornecimento de [nome] na Farmacia Popular e liberado em [data]. */
data class LembreteRetirada(
    val medicamentoId: Long,
    val nome: String,
    val data: LocalDate,
) {
    /** Identifica o alarme no AlarmManager: um por medicamento e data de liberacao. */
    val codigo: Int get() = "R|$medicamentoId|$data".hashCode()
}
