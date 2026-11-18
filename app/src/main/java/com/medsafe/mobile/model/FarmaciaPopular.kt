package com.medsafe.mobile.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Datas e textos do lembrete de retirada na Farmacia Popular, sem dependencia do Android (testavel na JVM). */
object FarmaciaPopular {

    /** O aviso aparece no Dashboard quando faltam 7 dias ou menos para o proximo fornecimento ser liberado. */
    const val DIAS_DE_ANTECEDENCIA = 7L

    /** Prazos entre retiradas oferecidos no cadastro; a pessoa deve confirmar o prazo certo com a farmacia. */
    val PRAZOS_EM_DIAS = listOf(30, 60, 90)

    private val Formato = DateTimeFormatter.ofPattern("dd/MM")
    private val FormatoCompleto = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Dias ate a liberacao; negativo quando ela ja aconteceu (-2 = liberado ha 2 dias). */
    fun diasAteLiberar(hoje: LocalDate, proximaRetirada: LocalDate): Long =
        ChronoUnit.DAYS.between(hoje, proximaRetirada)

    fun deveAvisar(diasAteLiberar: Long): Boolean = diasAteLiberar <= DIAS_DE_ANTECEDENCIA

    fun descricao(diasAteLiberar: Long, proximaRetirada: LocalDate): String {
        val data = proximaRetirada.format(Formato)
        return when {
            diasAteLiberar > 1 -> "Libera em $diasAteLiberar dias ($data)"
            diasAteLiberar == 1L -> "Libera amanhã ($data)"
            diasAteLiberar == 0L -> "Libera hoje"
            diasAteLiberar == -1L -> "Liberado desde ontem"
            else -> "Liberado há ${-diasAteLiberar} dias (desde $data)"
        }
    }

    /** Quantas unidades costumam vir na retirada: as doses de todos os dias do prazo. */
    fun unidadesSugeridas(dosesPorDia: Int, prazoEmDias: Int): Int =
        (dosesPorDia * prazoEmDias).coerceIn(1, Estoque.MAXIMO_POR_REPOSICAO)

    fun formatarData(data: LocalDate): String = data.format(FormatoCompleto)
}
