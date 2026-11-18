package com.medsafe.mobile.alarmes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Disparado pelo AlarmManager. Para um remedio, mostra o aviso e agenda o mesmo horario do dia seguinte;
 * para uma retirada na Farmacia Popular, mostra o aviso unico do dia da liberacao.
 */
class AlarmeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val agendador = AlarmScheduler(context)

        val retirada = AlarmScheduler.retiradaDe(intent)
        if (retirada != null) {
            // Se o remedio mudou de prazo ou a pessoa saiu da conta, este aviso nao vale mais.
            if (!agendador.estaAtiva(retirada)) return
            Notificacoes.criarCanais(context)
            Notificacoes.mostrarRetirada(context, retirada)
            return
        }

        val lembrete = AlarmScheduler.lembreteDe(intent) ?: return
        // Se o medicamento foi removido ou o usuario saiu da conta, este alarme nao vale mais.
        if (!agendador.estaAtivo(lembrete)) return

        Notificacoes.criarCanais(context)
        Notificacoes.mostrarLembrete(context, lembrete)
        agendador.agendar(lembrete)
    }
}
