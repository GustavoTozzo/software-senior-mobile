package com.medsafe.mobile

import com.medsafe.mobile.alarmes.AlarmScheduler
import com.medsafe.mobile.alarmes.LembreteRetirada
import com.medsafe.mobile.model.FarmaciaPopular
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FarmaciaPopularTest {

    private val hoje = LocalDate.of(2026, 11, 18)

    @Test
    fun contaOsDiasAteALiberacao() {
        assertEquals(3, FarmaciaPopular.diasAteLiberar(hoje, LocalDate.of(2026, 11, 21)))
        assertEquals(0, FarmaciaPopular.diasAteLiberar(hoje, hoje))
        assertEquals(-2, FarmaciaPopular.diasAteLiberar(hoje, LocalDate.of(2026, 11, 16)))
    }

    @Test
    fun avisaComSeteDiasOuMenosInclusiveDepoisDaLiberacao() {
        assertTrue(FarmaciaPopular.deveAvisar(7))
        assertTrue(FarmaciaPopular.deveAvisar(0))
        assertTrue(FarmaciaPopular.deveAvisar(-10))
        assertFalse(FarmaciaPopular.deveAvisar(8))
    }

    @Test
    fun descricaoFalaEmDiasEmLinguagemSimples() {
        val data = LocalDate.of(2026, 11, 25)
        assertEquals("Libera em 7 dias (25/11)", FarmaciaPopular.descricao(7, data))
        assertEquals("Libera amanhã (25/11)", FarmaciaPopular.descricao(1, data))
        assertEquals("Libera hoje", FarmaciaPopular.descricao(0, data))
        assertEquals("Liberado desde ontem", FarmaciaPopular.descricao(-1, data))
        assertEquals("Liberado há 4 dias (desde 25/11)", FarmaciaPopular.descricao(-4, data))
    }

    @Test
    fun unidadesSugeridasSaoAsDosesDeTodosOsDiasDoPrazo() {
        assertEquals(30, FarmaciaPopular.unidadesSugeridas(dosesPorDia = 1, prazoEmDias = 30))
        assertEquals(180, FarmaciaPopular.unidadesSugeridas(dosesPorDia = 2, prazoEmDias = 90))
        assertEquals(1, FarmaciaPopular.unidadesSugeridas(dosesPorDia = 0, prazoEmDias = 30))
        assertEquals(9999, FarmaciaPopular.unidadesSugeridas(dosesPorDia = 9, prazoEmDias = 9999))
    }

    @Test
    fun formataADataCompleta() {
        assertEquals("05/01/2027", FarmaciaPopular.formatarData(LocalDate.of(2027, 1, 5)))
    }

    // ---- alarme de retirada

    private val fuso = ZoneId.of("America/Sao_Paulo")

    private fun instante(dia: Int, hora: Int): Long =
        ZonedDateTime.of(2026, 11, dia, hora, 0, 0, 0, fuso).toInstant().toEpochMilli()

    @Test
    fun avisoDeRetiradaDisparaAsNoveHorasDoDiaDaLiberacao() {
        val agora = ZonedDateTime.of(2026, 11, 18, 15, 0, 0, 0, fuso)
        assertEquals(instante(25, 9), AlarmScheduler.gatilhoDaRetirada(LocalDate.of(2026, 11, 25), agora))
    }

    @Test
    fun liberacaoHojeAntesDasNoveAindaAgenda() {
        val agora = ZonedDateTime.of(2026, 11, 18, 7, 30, 0, 0, fuso)
        assertEquals(instante(18, 9), AlarmScheduler.gatilhoDaRetirada(LocalDate.of(2026, 11, 18), agora))
    }

    @Test
    fun liberacaoQueJaPassouNaoAgendaNada() {
        val depoisDasNove = ZonedDateTime.of(2026, 11, 18, 9, 0, 0, 0, fuso)
        assertNull(AlarmScheduler.gatilhoDaRetirada(LocalDate.of(2026, 11, 18), depoisDasNove))
        assertNull(AlarmScheduler.gatilhoDaRetirada(LocalDate.of(2026, 11, 10), depoisDasNove))
    }

    @Test
    fun avisosDeRetiradaDeDatasOuRemediosDiferentesTemCodigosDiferentes() {
        val base = LembreteRetirada(1, "Losartana", LocalDate.of(2026, 12, 10))
        assertNotEquals(base.codigo, LembreteRetirada(1, "Losartana", LocalDate.of(2026, 12, 11)).codigo)
        assertNotEquals(base.codigo, LembreteRetirada(2, "Losartana", LocalDate.of(2026, 12, 10)).codigo)
        assertEquals(base.codigo, LembreteRetirada(1, "Outro nome", LocalDate.of(2026, 12, 10)).codigo)
    }
}
