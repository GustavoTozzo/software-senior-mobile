package com.medsafe.mobile

import android.app.Application
import android.content.Context
import com.medsafe.mobile.alarmes.AlarmScheduler
import com.medsafe.mobile.alarmes.Notificacoes
import com.medsafe.mobile.data.MedSafeRepository
import com.medsafe.mobile.data.SessionStore
import com.medsafe.mobile.data.api.ApiFactory

/** Dependencias compartilhadas pelo app inteiro (criadas uma vez, na abertura do processo). */
class AppContainer(context: Context) {
    val sessionStore = SessionStore(context)
    val repository = MedSafeRepository(ApiFactory.criar(sessionStore), sessionStore)
    val alarmes = AlarmScheduler(context)
}

class MedSafeApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notificacoes.criarCanal(this)
    }
}
