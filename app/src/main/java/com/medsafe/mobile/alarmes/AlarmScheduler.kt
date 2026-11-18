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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Agenda os avisos no AlarmManager (alarmes exatos, que disparam mesmo com o celular em repouso).
 *
 * - Lembretes de remedio: um alarme por remedio e horario. Um alarme exato dispara uma vez so, entao
 *   o AlarmeReceiver agenda o proximo dia a cada disparo.
 * - Retiradas na Farmacia Popular: um alarme unico, as 9h do dia em que o proximo fornecimento e liberado.
 *
 * As duas listas ficam salvas localmente para os avisos sobreviverem a reinicios do celular e
 * funcionarem sem internet.
 */
class AlarmScheduler(context: Context) {

    private val context = context.applicationContext
    private val alarmManager = this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val armazenamento = this.context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    /** Troca todos os alarmes diarios pelos horarios atuais dos medicamentos. */
    fun reagendar(medicamentos: List<Medicamento>) {
        val novos = medicamentos
            .flatMap { medicamento -> medicamento.horarios.map { Lembrete(medicamento.id, medicamento.nome, it) } }
            .distinct()
        lerSalvos().forEach { cancelar(it) }
        salvar(novos)
        novos.forEach { agendar(it) }
    }

    /** Troca os alarmes de retirada pelas proximas liberacoes dos remedios da Farmacia Popular. */
    fun reagendarRetiradas(medicamentos: List<Medicamento>, agora: ZonedDateTime = ZonedDateTime.now()) {
        val novas = medicamentos
            .mapNotNull { m -> m.proximaRetirada?.takeIf { m.farmaciaPopular }?.let { LembreteRetirada(m.id, m.nome, it) } }
            .distinct()
        lerRetiradasSalvas().forEach { cancelarRetirada(it) }
        salvarRetiradas(novas)
        novas.forEach { agendarRetirada(it, agora) }
    }

    /** Reagenda o que estava salvo (usado apos reiniciar o celular). */
    fun reagendarSalvos() {
        lerSalvos().forEach { agendar(it) }
        lerRetiradasSalvas().forEach { agendarRetirada(it) }
    }

    /** Remove os alarmes diarios de remedio e a lista salva. */
    fun cancelarLembretes() {
        lerSalvos().forEach { cancelar(it) }
        salvar(emptyList())
    }

    /** Remove os alarmes de retirada na Farmacia Popular e a lista salva. */
    fun cancelarRetiradas() {
        lerRetiradasSalvas().forEach { cancelarRetirada(it) }
        salvarRetiradas(emptyList())
    }

    /** Remove todos os alarmes (ao sair da conta). */
    fun cancelarTodos() {
        cancelarLembretes()
        cancelarRetiradas()
    }

    fun estaAtivo(lembrete: Lembrete): Boolean = lerSalvos().contains(lembrete)

    fun estaAtiva(retirada: LembreteRetirada): Boolean = lerRetiradasSalvas().contains(retirada)

    /** Agenda a proxima ocorrencia do lembrete: hoje, se o horario ainda nao passou, senao amanha. */
    fun agendar(lembrete: Lembrete) {
        agendarEm(proximoGatilho(lembrete.horario), pendingIntent(lembrete))
    }

    /** Agenda o aviso de retirada; se as 9h do dia da liberacao ja passaram, nao ha o que agendar. */
    fun agendarRetirada(retirada: LembreteRetirada, agora: ZonedDateTime = ZonedDateTime.now()) {
        val gatilho = gatilhoDaRetirada(retirada.data, agora) ?: return
        agendarEm(gatilho, pendingIntentRetirada(retirada))
    }

    private fun agendarEm(gatilho: Long, intencao: PendingIntent) {
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

    private fun cancelarRetirada(retirada: LembreteRetirada) {
        alarmManager.cancel(pendingIntentRetirada(retirada))
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

    private fun pendingIntentRetirada(retirada: LembreteRetirada): PendingIntent {
        val intent = Intent(context, AlarmeReceiver::class.java)
            .putExtra(EXTRA_MEDICAMENTO_ID, retirada.medicamentoId)
            .putExtra(EXTRA_NOME, retirada.nome)
            .putExtra(EXTRA_DATA_RETIRADA, retirada.data.toString())
        return PendingIntent.getBroadcast(
            context,
            retirada.codigo,
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

    private fun salvarRetiradas(retiradas: List<LembreteRetirada>) {
        val lista = JSONArray()
        retiradas.forEach {
            lista.put(
                JSONObject()
                    .put("medicamentoId", it.medicamentoId)
                    .put("nome", it.nome)
                    .put("data", it.data.toString()),
            )
        }
        armazenamento.edit().putString(CHAVE_RETIRADAS, lista.toString()).apply()
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

    private fun lerRetiradasSalvas(): List<LembreteRetirada> {
        val texto = armazenamento.getString(CHAVE_RETIRADAS, null) ?: return emptyList()
        return try {
            val lista = JSONArray(texto)
            (0 until lista.length()).map { indice ->
                val item = lista.getJSONObject(indice)
                LembreteRetirada(
                    item.getLong("medicamentoId"),
                    item.getString("nome"),
                    LocalDate.parse(item.getString("data")),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val ARQUIVO = "medsafe_lembretes"
        private const val CHAVE_LISTA = "lista"
        private const val CHAVE_RETIRADAS = "retiradas"
        private const val EXTRA_MEDICAMENTO_ID = "medicamento_id"
        private const val EXTRA_NOME = "nome"
        private const val EXTRA_HORARIO = "horario"
        private const val EXTRA_DATA_RETIRADA = "data_retirada"

        /** Hora do aviso no dia em que o fornecimento da Farmacia Popular e liberado. */
        val HORA_DO_AVISO_DE_RETIRADA: LocalTime = LocalTime.of(9, 0)

        /** Reconstroi o lembrete a partir do Intent recebido no disparo do alarme. */
        fun lembreteDe(intent: Intent): Lembrete? {
            val medicamentoId = intent.getLongExtra(EXTRA_MEDICAMENTO_ID, -1L)
            val nome = intent.getStringExtra(EXTRA_NOME)
            val horario = intent.getStringExtra(EXTRA_HORARIO)
            if (medicamentoId < 0 || nome == null || horario == null) return null
            return Lembrete(medicamentoId, nome, horario)
        }

        /** Reconstroi o aviso de retirada a partir do Intent; null se o Intent e de um lembrete diario. */
        fun retiradaDe(intent: Intent): LembreteRetirada? {
            val medicamentoId = intent.getLongExtra(EXTRA_MEDICAMENTO_ID, -1L)
            val nome = intent.getStringExtra(EXTRA_NOME)
            val data = intent.getStringExtra(EXTRA_DATA_RETIRADA)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (medicamentoId < 0 || nome == null || data == null) return null
            return LembreteRetirada(medicamentoId, nome, data)
        }

        /** Proximo instante (em milissegundos) em que [horario] acontece depois de [agora]. */
        fun proximoGatilho(horario: String, agora: ZonedDateTime = ZonedDateTime.now()): Long {
            var alvo = agora.with(LocalTime.parse(horario))
            if (!alvo.isAfter(agora)) {
                alvo = alvo.plusDays(1)
            }
            return alvo.toInstant().toEpochMilli()
        }

        /** As 9h do dia [data] (em milissegundos), ou null se esse instante ja passou. */
        fun gatilhoDaRetirada(data: LocalDate, agora: ZonedDateTime = ZonedDateTime.now()): Long? {
            val alvo = data.atTime(HORA_DO_AVISO_DE_RETIRADA).atZone(agora.zone)
            return if (alvo.isAfter(agora)) alvo.toInstant().toEpochMilli() else null
        }
    }
}
