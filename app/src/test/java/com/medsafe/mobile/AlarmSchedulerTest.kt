package com.medsafe.mobile

import com.medsafe.mobile.alarmes.AlarmScheduler
import com.medsafe.mobile.alarmes.Lembrete
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmSchedulerTest {

    private val fuso = ZoneId.of("America/Sao_Paulo")

    private fun instante(dia: Int, hora: Int, minuto: Int): Long =
        ZonedDateTime.of(2026, 11, dia, hora, minuto, 0, 0, fuso).toInstant().toEpochMilli()

    @Test
    fun horarioQueAindaNaoPassouHojeAgendaParaHoje() {
        val agora = ZonedDateTime.of(2026, 11, 2, 7, 0, 0, 0, fuso)
        assertEquals(instante(2, 8, 0), AlarmScheduler.proximoGatilho("08:00", agora))
    }

    @Test
    fun horarioQueJaPassouAgendaParaAmanha() {
        val agora = ZonedDateTime.of(2026, 11, 2, 9, 30, 0, 0, fuso)
        assertEquals(instante(3, 8, 0), AlarmScheduler.proximoGatilho("08:00", agora))
    }

    @Test
    fun noExatoInstanteDoHorarioAgendaParaAmanha() {
        val agora = ZonedDateTime.of(2026, 11, 2, 8, 0, 0, 0, fuso)
        assertEquals(instante(3, 8, 0), AlarmScheduler.proximoGatilho("08:00", agora))
    }

    @Test
    fun lembretesDeHorariosOuMedicamentosDiferentesTemCodigosDiferentes() {
        val manha = Lembrete(1, "Losartana", "08:00")
        assertNotEquals(manha.codigo, Lembrete(1, "Losartana", "20:00").codigo)
        assertNotEquals(manha.codigo, Lembrete(2, "Losartana", "08:00").codigo)
        assertEquals(manha.codigo, Lembrete(1, "Outro nome", "08:00").codigo)
    }
}
