package com.medsafe.mobile.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.medsafe.mobile.model.Telefones

/**
 * Abre o discador com o numero pronto. Usa ACTION_DIAL de proposito: nao precisa da permissao de
 * ligacao e a pessoa confirma a chamada com um toque, sem risco de ligar sem querer.
 */
fun Context.discar(telefone: String) {
    abrir(Intent(Intent.ACTION_DIAL, Uri.parse(Telefones.enderecoDeDiscagem(telefone))), "Não foi possível abrir o telefone.")
}

/** Abre um link do WhatsApp (wa.me); se o app nao estiver instalado, abre no navegador. */
fun Context.abrirWhatsApp(link: String) {
    abrir(Intent(Intent.ACTION_VIEW, Uri.parse(link)), "Não foi possível abrir o WhatsApp.")
}

private fun Context.abrir(intent: Intent, mensagemDeFalha: String) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, mensagemDeFalha, Toast.LENGTH_LONG).show()
    }
}
