package com.medsafe.mobile.data.api

// Corpos de requisicao e resposta da API (Gson). Os nomes dos campos seguem o JSON do backend.

data class LoginRequest(
    val telefone: String,
    val senha: String,
)

data class RegistroRequest(
    val nome: String,
    val telefone: String,
    val idade: Int?,
    val tipoPerfil: String,
    val senha: String,
)

data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val telefone: String?,
    val idade: Int?,
    val tipoPerfil: String?,
)

data class AuthResponse(
    val token: String,
    val expiraEmSegundos: Long,
    val usuario: UsuarioResponse,
)

data class MedicamentoRequest(
    val usuarioId: Long,
    val nomeComercial: String,
    val codigoBarras: String?,
    val quantidadeAtual: Int,
    val doseFrequencia: String,
    val horariosProgramados: String,
)

data class MedicamentoResponse(
    val id: Long,
    val usuarioId: Long,
    val nomeComercial: String,
    val codigoBarras: String?,
    val quantidadeAtual: Int,
    val estoqueBaixo: Boolean,
    val doseFrequencia: String,
    val horariosProgramados: String,
)

data class FarmaciaRequest(
    val nome: String,
    val telefone: String,
    val whatsappLink: String?,
    val preferida: Boolean?,
)

data class FarmaciaResponse(
    val id: Long,
    val nome: String,
    val telefone: String,
    val whatsappLink: String?,
    val preferida: Boolean,
)

data class ContatoEmergenciaRequest(
    val nome: String,
    val parentesco: String?,
    val telefone: String,
)

data class ContatoEmergenciaResponse(
    val id: Long,
    val nome: String,
    val parentesco: String?,
    val telefone: String,
)

data class HistoricoResponse(
    val id: Long,
    val medicamentoId: Long,
    val medicamentoNome: String,
    val dataHoraProgramada: String,
    val dataHoraRealizada: String?,
    val status: String,
)

data class StatusRequest(
    val status: String,
)

/** Formato de erro devolvido pelo backend (GlobalExceptionHandler). */
data class ErroApiResponse(
    val status: Int?,
    val message: String?,
    val fieldErrors: Map<String, String>?,
)
