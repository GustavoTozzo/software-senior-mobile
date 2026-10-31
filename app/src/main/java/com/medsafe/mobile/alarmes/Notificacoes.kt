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

    fun criarCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_LEMBRETES,
            "Lembretes de remédios",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisos na hora de tomar cada remédio"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // A permissao POST_NOTIFICATIONS (Android 13+) e verificada em areNotificationsEnabled().
    @SuppressLint("MissingPermission")
    fun mostrarLembrete(context: Context, lembrete: Lembrete) {
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

        val texto = "${lembrete.nome} - ${lembrete.horario}"
        val notificacao = NotificationCompat.Builder(context, CANAL_LEMBRETES)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Hora do remédio")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        gerenciador.notify(lembrete.codigo, notificacao)
    }
}
