package com.medsafe.mobile

import com.medsafe.mobile.data.Resultado
import com.medsafe.mobile.data.mapear
import com.medsafe.mobile.ui.viewmodel.Frequencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicaDeDadosTest {

    @Test
    fun mapearTransformaSucessoEPreservaErro() {
        val sucesso: Resultado<Int> = Resultado.Sucesso(2)
        assertEquals(Resultado.Sucesso(4), sucesso.mapear { it * 2 })

        val erro: Resultado<Int> = Resultado.Erro("falhou", 500)
        assertTrue(erro.mapear { it * 2 } is Resultado.Erro)
    }

    @Test
    fun frequenciaSugereTantosHorariosQuantoVezesAoDia() {
        assertEquals(1, Frequencia.UMA_VEZ.horariosSugeridos.size)
        assertEquals(2, Frequencia.DUAS_VEZES.horariosSugeridos.size)
        assertEquals(3, Frequencia.TRES_VEZES.horariosSugeridos.size)
    }
}
