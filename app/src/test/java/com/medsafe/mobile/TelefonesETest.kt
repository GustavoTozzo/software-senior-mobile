package com.medsafe.mobile

import com.medsafe.mobile.model.Telefones
import com.medsafe.mobile.ui.viewmodel.validarContato
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelefonesETest {

    @Test
    fun formataCelularEFixoComDdd() {
        assertEquals("(11) 91234-5678", Telefones.formatar("11912345678"))
        assertEquals("(11) 3333-4444", Telefones.formatar("1133334444"))
        assertEquals("(11) 91234-5678", Telefones.formatar("(11) 91234-5678"))
    }

    @Test
    fun telefoneForaDoPadraoVoltaComoFoiDigitado() {
        assertEquals("190", Telefones.formatar("190"))
        assertEquals("+55 11 91234-5678", Telefones.formatar("+55 11 91234-5678"))
    }

    @Test
    fun codigoDoPaisSoEAcrescentadoQuandoFalta() {
        assertEquals("5511912345678", Telefones.comCodigoDoPais("(11) 91234-5678"))
        assertEquals("5511912345678", Telefones.comCodigoDoPais("+55 11 91234-5678"))
        // DDD 55 (interior do RS): o 55 inicial e o DDD, nao o pais.
        assertEquals("5555999123456", Telefones.comCodigoDoPais("(55) 99912-3456"))
    }

    @Test
    fun linkDoWhatsappCodificaAMensagem() {
        assertEquals("https://wa.me/5511912345678", Telefones.linkWhatsApp("(11) 91234-5678"))
        assertEquals(
            "https://wa.me/5511912345678?text=Ol%C3%A1%21%20Quero%20repor%20Losartana.",
            Telefones.linkWhatsApp("(11) 91234-5678", "Olá! Quero repor Losartana."),
        )
    }

    @Test
    fun mensagemRespeitaLinkQueJaTemParametros() {
        assertEquals("https://wa.me/5511900000000?text=Oi", Telefones.comMensagem("https://wa.me/5511900000000", "Oi"))
        assertEquals(
            "https://api.whatsapp.com/send?phone=5511900000000&text=Oi",
            Telefones.comMensagem("https://api.whatsapp.com/send?phone=5511900000000", "Oi"),
        )
    }

    @Test
    fun enderecoDeDiscagemUsaSoDigitos() {
        assertEquals("tel:11912345678", Telefones.enderecoDeDiscagem("(11) 91234-5678"))
    }

    @Test
    fun validacaoDeContatoExigeNomeETelefoneComDdd() {
        assertTrue(validarContato("Maria", "(11) 91234-5678").isEmpty())

        val problemas = validarContato("  ", "9999")
        assertEquals(setOf("nome", "telefone"), problemas.keys)
        assertTrue(validarContato("Maria", "1191234567890123").containsKey("telefone"))
    }
}
