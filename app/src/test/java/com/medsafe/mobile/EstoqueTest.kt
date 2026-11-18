package com.medsafe.mobile

import com.medsafe.mobile.model.Estoque
import com.medsafe.mobile.model.Farmacia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EstoqueTest {

    @Test
    fun descricaoFalaEmDiasQuandoHaPrevisao() {
        assertEquals("Dá para 4 dias (restam 12 unidades)", Estoque.descricao(12, 4))
        assertEquals("Dá para 1 dia (restam 3 unidades)", Estoque.descricao(3, 1))
        assertEquals("Dura menos de 1 dia (restam 2 unidades)", Estoque.descricao(2, 0))
    }

    @Test
    fun descricaoSemPrevisaoMostraSoAsUnidades() {
        assertEquals("Restam 1 unidade", Estoque.descricao(1, null))
        assertEquals("Restam 5 unidades", Estoque.descricao(5, null))
    }

    @Test
    fun estoqueZeradoDizQueAcabou() {
        assertEquals("O estoque acabou", Estoque.descricao(0, 0))
    }

    private fun farmacia(link: String?) = Farmacia(1, "Drogaria do Bairro", "11912345678", link, preferida = true)

    @Test
    fun pedidoUsaOLinkProprioDaFarmaciaQuandoHa() {
        assertEquals(
            "https://wa.me/5511900000000?text=Ol%C3%A1%21%20Gostaria%20de%20repor%20o%20medicamento%20Losartana.",
            Estoque.linkDoPedido(farmacia("https://wa.me/5511900000000"), "Losartana"),
        )
    }

    @Test
    fun pedidoMontaOLinkPeloTelefoneQuandoNaoHaLinkProprio() {
        val esperado = "https://wa.me/5511912345678?text=Ol%C3%A1%21%20Gostaria%20de%20repor%20o%20medicamento%20Losartana."
        assertEquals(esperado, Estoque.linkDoPedido(farmacia(null), "Losartana"))
        assertEquals(esperado, Estoque.linkDoPedido(farmacia("  "), "Losartana"))
    }

    @Test
    fun reposicaoAceitaSoInteirosDeUmAteOMaximo() {
        assertEquals(30, Estoque.unidadesDaReposicao(" 30 "))
        assertEquals(9999, Estoque.unidadesDaReposicao("9999"))
        assertNull(Estoque.unidadesDaReposicao("0"))
        assertNull(Estoque.unidadesDaReposicao("10000"))
        assertNull(Estoque.unidadesDaReposicao(""))
        assertNull(Estoque.unidadesDaReposicao("abc"))
    }
}
