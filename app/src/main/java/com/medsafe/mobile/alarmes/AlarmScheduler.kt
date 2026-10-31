package com.medsafe.mobile.alarmes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medsafe.mobile.model.Medicamento
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Agenda os avisos de remedio no AlarmManager (alarmes exatos, que disparam mesmo com o celular
 * em repouso). Um alarme exato dispara uma vez so, entao o AlarmeReceiver agenda o proximo dia
 * a cada disparo. A lista de lembretes fica salva localmente para os avisos sobreviverem a
 * reinicios do celular e funcionarem sem internet.
 */
class AlarmScheduler(context: Context) {

    private val context = context.applicationContext
    private val alarmManager = this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val armazenamento = this.context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    /** Troca todos os alarmes pelos horarios atuais dos medicamentos. */
    fun reagendar(medicamentos: List<Medicamento>) {
        val novos = medicamentos
            .flatMap { medicamento -> medicamento.horarios.map { Lembrete(medicamento.id, medicamento.nome, it) } }
            .distinct()
        lerSalvos().forEach { cancelar(it) }
        salvar(novos)
        novos.forEach { agendar(it) }
    }

    /** Reagenda o que estava salvo (usado apos reiniciar o celular). */
    fun reagendarSalvos() {
        lerSalvos().forEach { agendar(it) }
    }

    /** Remove todos os alarmes e a lista salva (ao sair da conta). */
    fun cancelarTodos() {
        lerSalvos().forEach { cancelar(it) }
        salvar(emptyList())
    }

    fun estaAtivo(lembrete: Lembrete): Boolean = lerSalvos().contains(lembrete)

    /** Agenda a proxima ocorrencia do lembrete: hoje, se o horario ainda nao passou, senao amanha. */
    fun agendar(lembrete: Lembrete) {
        val gatilho = proximoGatilho(lembrete.horario)
        val intencao = pendingIntent(lembrete)
        val podeAgendarExato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (podeAgendarExato) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, gatilho, intencao)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, gatilho, intencao)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, gatilho, intencao)
        }
    }

    private fun cancelar(lembrete: Lembrete) {
        alarmManager.cancel(pendingIntent(lembrete))
    }

    private fun pendingIntent(lembrete: Lembrete): PendingIntent {
        val intent = Intent(context, AlarmeReceiver::class.java)
            .putExtra(EXTRA_MEDICAMENTO_ID, lembrete.medicamentoId)
            .putExtra(EXTRA_NOME, lembrete.nome)
            .putExtra(EXTRA_HORARIO, lembrete.horario)
        return PendingIntent.getBroadcast(
            context,
            lembrete.codigo,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun salvar(lembretes: List<Lembrete>) {
        val lista = JSONArray()
        lembretes.forEach {
            lista.put(
                JSONObject()
                    .put("medicamentoId", it.medicamentoId)
                    .put("nome", it.nome)
                    .put("horario", it.horario),
            )
        }
        armazenamento.edit().putString(CHAVE_LISTA, lista.toString()).apply()
    }

    private fun lerSalvos(): List<Lembrete> {
        val texto = armazenamento.getString(CHAVE_LISTA, null) ?: return emptyList()
        return try {
            val lista = JSONArray(texto)
            (0 until lista.length()).map { indice ->
                val item = lista.getJSONObject(indice)
                Lembrete(item.getLong("medicamentoId"), item.getString("nome"), item.getString("horario"))
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    companion object {
        private const val ARQUIVO = "medsafe_lembretes"
        private const val CHAVE_LISTA = "lista"
        private const val EXTRA_MEDICAMENTO_ID = "medicamento_id"
        private const val EXTRA_NOME = "nome"
        private const val EXTRA_HORARIO = "horario"

        /** Reconstroi o lembrete a partir do Intent recebido no disparo do alarme. */
        fun lembreteDe(intent: Intent): Lembrete? {
            val medicamentoId = intent.getLongExtra(EXTRA_MEDICAMENTO_ID, -1L)
            val nome = intent.getStringExtra(EXTRA_NOME)
            val horario = intent.getStringExtra(EXTRA_HORARIO)
            if (medicamentoId < 0 || nome == null || horario == null) return null
            return Lembrete(medicamentoId, nome, horario)
        }

        /** Proximo instante (em milissegundos) em que [horario] acontece depois de [agora]. */
        fun proximoGatilho(horario: String, agora: ZonedDateTime = ZonedDateTime.now()): Long {
            var alvo = agora.with(LocalTime.parse(horario))
            if (!alvo.isAfter(agora)) {
                alvo = alvo.plusDays(1)
            }
            return alvo.toInstant().toEpochMilli()
        }
    }
}
