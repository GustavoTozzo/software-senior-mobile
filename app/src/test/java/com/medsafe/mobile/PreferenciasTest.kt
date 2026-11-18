package com.medsafe.mobile

import com.medsafe.mobile.model.Preferencias
import com.medsafe.mobile.model.TamanhoDoTexto
import com.medsafe.mobile.model.TemaDoApp
import com.medsafe.mobile.model.ehEscuro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenciasTest {

    @Test
    fun temaAutomaticoSegueOCelular() {
        assertTrue(TemaDoApp.AUTOMATICO.ehEscuro(celularNoEscuro = true))
        assertFalse(TemaDoApp.AUTOMATICO.ehEscuro(celularNoEscuro = false))
    }

    @Test
    fun temaEscolhidoVenceOCelular() {
        assertTrue(TemaDoApp.ESCURO.ehEscuro(celularNoEscuro = false))
        assertFalse(TemaDoApp.CLARO.ehEscuro(celularNoEscuro = true))
        assertFalse(TemaDoApp.ALTO_CONTRASTE.ehEscuro(celularNoEscuro = true))
    }

    @Test
    fun tamanhosDeTextoCrescemEmOrdem() {
        val escalas = TamanhoDoTexto.entries.map { it.escala }
        assertEquals(escalas.sorted(), escalas)
        assertEquals(1f, TamanhoDoTexto.NORMAL.escala, 0f)
    }

    @Test
    fun padraoMantemOComportamentoAnteriorEOsAvisosLigados() {
        val padrao = Preferencias()
        assertEquals(TemaDoApp.AUTOMATICO, padrao.tema)
        assertEquals(TamanhoDoTexto.NORMAL, padrao.tamanhoDoTexto)
        assertTrue(padrao.lembretesDeRemedio)
        assertTrue(padrao.lembretesDeRetirada)
    }
}
