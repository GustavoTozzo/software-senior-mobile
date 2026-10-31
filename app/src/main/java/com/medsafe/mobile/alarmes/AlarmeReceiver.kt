package com.medsafe.mobile.alarmes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Disparado pelo AlarmManager na hora de um remedio: mostra o aviso e agenda o mesmo horario do dia seguinte. */
class AlarmeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val lembrete = AlarmScheduler.lembreteDe(intent) ?: return
        val agendador = AlarmScheduler(context)
        // Se o medicamento foi removido ou o usuario saiu da conta, este alarme nao vale mais.
        if (!agendador.estaAtivo(lembrete)) return

        Notificacoes.criarCanal(context)
        Notificacoes.mostrarLembrete(context, lembrete)
        agendador.agendar(lembrete)
    }
}
