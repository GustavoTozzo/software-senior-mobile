package com.medsafe.mobile.alarmes

/** Um aviso diario: tomar [nome] as [horario] (formato HH:mm). */
data class Lembrete(
    val medicamentoId: Long,
    val nome: String,
    val horario: String,
) {
    /** Identifica o alarme no AlarmManager: um por medicamento e horario. */
    val codigo: Int get() = "$medicamentoId|$horario".hashCode()
}
