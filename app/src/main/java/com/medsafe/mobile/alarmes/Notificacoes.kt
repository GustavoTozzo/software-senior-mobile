package com.medsafe.mobile.alarmes

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.medsafe.mobile.MainActivity
import com.medsafe.mobile.R

object Notificacoes {

    private const val CANAL_LEMBRETES = "lembretes_remedios"
    private const val CANAL_RETIRADAS = "retiradas_farmacia_popular"

    /** Cria (ou atualiza) os canais; ter um por tipo deixa a pessoa silenciar um sem perder o outro. */
    fun criarCanais(context: Context) {
        val gerenciador = context.getSystemService(NotificationManager::class.java)

        gerenciador.createNotificationChannel(
            NotificationChannel(CANAL_LEMBRETES, "Lembretes de remédios", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos na hora de tomar cada remédio"
                enableVibration(true)
            },
        )
        gerenciador.createNotificationChannel(
            NotificationChannel(CANAL_RETIRADAS, "Farmácia Popular", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Aviso no dia em que o próximo fornecimento é liberado"
            },
        )
    }

    // A permissao POST_NOTIFICATIONS (Android 13+) e verificada em areNotificationsEnabled().
    @SuppressLint("MissingPermission")
    fun mostrarLembrete(context: Context, lembrete: Lembrete) {
        mostrar(
            context = context,
            canal = CANAL_LEMBRETES,
            codigo = lembrete.codigo,
            titulo = "Hora do remédio",
            texto = "${lembrete.nome} - ${lembrete.horario}",
            alta = true,
        )
    }

    @SuppressLint("MissingPermission")
    fun mostrarRetirada(context: Context, retirada: LembreteRetirada) {
        mostrar(
            context = context,
            canal = CANAL_RETIRADAS,
            codigo = retirada.codigo,
            titulo = "Farmácia Popular",
            texto = "Já pode retirar ${retirada.nome}: o próximo fornecimento foi liberado.",
            alta = false,
        )
    }

    @SuppressLint("MissingPermission")
    private fun mostrar(context: Context, canal: String, codigo: Int, titulo: String, texto: String, alta: Boolean) {
        val gerenciador = NotificationManagerCompat.from(context)
        if (!gerenciador.areNotificationsEnabled()) return

        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacao = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(if (alta) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        gerenciador.notify(codigo, notificacao)
    }
}
