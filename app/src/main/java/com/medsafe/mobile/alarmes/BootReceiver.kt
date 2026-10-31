package com.medsafe.mobile.alarmes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Os alarmes do AlarmManager somem ao reiniciar o celular (ou atualizar o app): recria a partir da lista salva. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            AlarmScheduler(context).reagendarSalvos()
        }
    }
}
