package com.medsafe.mobile

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.medsafe.mobile.alarmes.AlarmScheduler
import com.medsafe.mobile.alarmes.AlarmeReceiver
import com.medsafe.mobile.alarmes.Notificacoes
import com.medsafe.mobile.model.Medicamento
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Passa pelo caminho real dos avisos no aparelho: o agendador guarda os lembretes, o AlarmeReceiver e
 * disparado como o AlarmManager faria e a notificacao aparece (ou nao aparece, quando o aviso nao vale mais).
 */
@RunWith(AndroidJUnit4::class)
class AvisosNoAparelhoTest {

    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private val agendador = AlarmScheduler(contexto)
    private val gerenciador = contexto.getSystemService(NotificationManager::class.java)

    @Before
    fun limpar() {
        // Android 13+: sem esta permissao as notificacoes sao descartadas.
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(contexto.packageName, Manifest.permission.POST_NOTIFICATIONS)
        agendador.cancelarTodos()
        gerenciador.cancelAll()
        Notificacoes.criarCanais(contexto)
    }

    @After
    fun desfazer() {
        agendador.cancelarTodos()
        gerenciador.cancelAll()
    }

    private fun remedioDaFarmaciaPopular(id: Long, nome: String, libera: LocalDate) = Medicamento(
        id = id,
        nome = nome,
        codigoBarras = null,
        quantidade = 10,
        estoqueBaixo = false,
        diasRestantes = 10,
        frequencia = "1x ao dia",
        horarios = listOf("08:00"),
        farmaciaPopular = true,
        ultimaRetirada = libera.minusDays(30),
        intervaloRetiradaDias = 30,
        proximaRetirada = libera,
    )

    private fun disparoDeRetirada(id: Long, nome: String, data: LocalDate): Intent =
        Intent(contexto, AlarmeReceiver::class.java)
            .putExtra("medicamento_id", id)
            .putExtra("nome", nome)
            .putExtra("data_retirada", data.toString())

    private fun titulosAtivos(): List<String> =
        gerenciador.activeNotifications.map { it.notification.extras.getString(Notification.EXTRA_TITLE).orEmpty() }

    /** O receiver roda de forma assincrona: espera ate 5 s pela condicao. */
    private fun esperar(condicao: () -> Boolean): Boolean {
        repeat(25) {
            if (condicao()) return true
            Thread.sleep(200)
        }
        return condicao()
    }

    @Test
    fun avisoDeRetiradaAtivoApareceNaNotificacao() {
        val libera = LocalDate.now().plusDays(3)
        agendador.reagendarRetiradas(listOf(remedioDaFarmaciaPopular(7, "Losartana", libera)))

        contexto.sendBroadcast(disparoDeRetirada(7, "Losartana", libera))

        assertTrue("notificacao da Farmacia Popular nao apareceu", esperar { "Farmácia Popular" in titulosAtivos() })
        val texto = gerenciador.activeNotifications
            .first { it.notification.extras.getString(Notification.EXTRA_TITLE) == "Farmácia Popular" }
            .notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
        assertTrue(texto, texto.contains("Losartana"))
    }

    @Test
    fun avisoDeRetiradaCancelado_naoApareceMesmoSeOAlarmeDispara() {
        val libera = LocalDate.now().plusDays(3)
        agendador.reagendarRetiradas(listOf(remedioDaFarmaciaPopular(8, "Metformina", libera)))
        agendador.cancelarRetiradas()

        contexto.sendBroadcast(disparoDeRetirada(8, "Metformina", libera))
        Thread.sleep(2000)

        assertEquals(emptyList<String>(), titulosAtivos())
    }

    @Test
    fun remedioQueNaoEhDaFarmaciaPopularNaoGeraAlarmeDeRetirada() {
        val comum = remedioDaFarmaciaPopular(9, "Sinvastatina", LocalDate.now().plusDays(3)).copy(farmaciaPopular = false)
        agendador.reagendarRetiradas(listOf(comum))

        contexto.sendBroadcast(disparoDeRetirada(9, "Sinvastatina", LocalDate.now().plusDays(3)))
        Thread.sleep(2000)

        assertEquals(emptyList<String>(), titulosAtivos())
    }

    @Test
    fun lembreteDeRemedioAtivoAparece() {
        val remedio = remedioDaFarmaciaPopular(10, "Enalapril", LocalDate.now().plusDays(3)).copy(horarios = listOf("21:00"))
        agendador.reagendar(listOf(remedio))

        contexto.sendBroadcast(
            Intent(contexto, AlarmeReceiver::class.java)
                .putExtra("medicamento_id", 10L)
                .putExtra("nome", "Enalapril")
                .putExtra("horario", "21:00"),
        )

        assertTrue("notificacao do remedio nao apareceu", esperar { "Hora do remédio" in titulosAtivos() })
    }
}
