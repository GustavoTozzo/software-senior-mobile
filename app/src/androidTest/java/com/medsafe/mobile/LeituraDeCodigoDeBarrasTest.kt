package com.medsafe.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.medsafe.mobile.ui.scanner.criarLeitorDeCodigos
import com.medsafe.mobile.ui.scanner.primeiroValor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Entrega ao ML Kit imagens de codigos de barras de verdade (desenhadas aqui, com as barras do padrao
 * EAN-13) e confere que o leitor do app devolve o numero certo. Roda no emulador ou no celular:
 * ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class LeituraDeCodigoDeBarrasTest {

    private fun ler(bitmap: Bitmap): List<Barcode> {
        val leitor = criarLeitorDeCodigos()
        try {
            return Tasks.await(leitor.process(InputImage.fromBitmap(bitmap, 0)))
        } finally {
            leitor.close()
        }
    }

    @Test
    fun leUmEan13DeCaixaDeRemedio() {
        val codigos = ler(desenharEan13("7891000100103"))
        assertEquals("7891000100103", primeiroValor(codigos))
        assertEquals(Barcode.FORMAT_EAN_13, codigos.first().format)
    }

    @Test
    fun leOutroEan13ComPrimeiroDigitoDiferente() {
        assertEquals("4006381333931", primeiroValor(ler(desenharEan13("4006381333931"))))
    }

    @Test
    fun leOCodigoMesmoComAImagemMenorEComMargem() {
        assertEquals("7891000100103", primeiroValor(ler(desenharEan13("7891000100103", moduloEmPx = 3, margemEmModulos = 20))))
    }

    @Test
    fun imagemSemCodigoNaoDevolveNada() {
        val branca = Bitmap.createBitmap(600, 300, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        assertNull(primeiroValor(ler(branca)))
    }

    // ---- desenho do EAN-13 (padrao GS1): guarda, 6 digitos em L/G, guarda central, 6 digitos em R, guarda

    private companion object {
        val L = listOf("0001101", "0011001", "0010011", "0111101", "0100011", "0110001", "0101111", "0111011", "0110111", "0001011")
        val G = listOf("0100111", "0110011", "0011011", "0100001", "0011101", "0111001", "0000101", "0010001", "0001001", "0010111")
        val R = listOf("1110010", "1100110", "1101100", "1000010", "1011100", "1001110", "1010000", "1000100", "1001000", "1110100")
        val PARIDADE = listOf("LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG", "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL")

        fun modulos(codigo: String): String {
            require(codigo.length == 13 && codigo.all { it.isDigit() })
            val primeiro = codigo[0].digitToInt()
            val esquerda = codigo.substring(1, 7).mapIndexed { i, c ->
                val d = c.digitToInt()
                if (PARIDADE[primeiro][i] == 'L') L[d] else G[d]
            }.joinToString("")
            val direita = codigo.substring(7).map { R[it.digitToInt()] }.joinToString("")
            return "101" + esquerda + "01010" + direita + "101"
        }

        fun desenharEan13(codigo: String, moduloEmPx: Int = 5, margemEmModulos: Int = 12): Bitmap {
            val barras = modulos(codigo)
            val largura = (barras.length + 2 * margemEmModulos) * moduloEmPx
            val altura = 60 * moduloEmPx
            val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            val tinta = Paint().apply { color = Color.BLACK; isAntiAlias = false }
            barras.forEachIndexed { i, bit ->
                if (bit == '1') {
                    val x = (margemEmModulos + i) * moduloEmPx
                    canvas.drawRect(x.toFloat(), (6 * moduloEmPx).toFloat(), (x + moduloEmPx).toFloat(), (altura - 6 * moduloEmPx).toFloat(), tinta)
                }
            }
            return bitmap
        }
    }
}
