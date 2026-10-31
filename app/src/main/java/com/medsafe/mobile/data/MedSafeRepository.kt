package com.medsafe.mobile.data

import com.google.gson.Gson
import com.medsafe.mobile.data.api.AuthResponse
import com.medsafe.mobile.data.api.ErroApiResponse
import com.medsafe.mobile.data.api.HistoricoResponse
import com.medsafe.mobile.data.api.LoginRequest
import com.medsafe.mobile.data.api.MedSafeApi
import com.medsafe.mobile.data.api.MedicamentoRequest
import com.medsafe.mobile.data.api.MedicamentoResponse
import com.medsafe.mobile.data.api.RegistroRequest
import com.medsafe.mobile.data.api.StatusRequest
import com.medsafe.mobile.model.Dose
import com.medsafe.mobile.model.Medicamento
import com.medsafe.mobile.model.Sessao
import com.medsafe.mobile.model.StatusDose
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

/** Unico ponto de acesso do app a API: converte respostas em modelos e erros em mensagens. */
class MedSafeRepository(
    private val api: MedSafeApi,
    private val sessionStore: SessionStore,
) {

    private val gson = Gson()

    /** Usuario logado; vira null no logout ou quando o servidor recusa o token (expirado). */
    val sessao: StateFlow<Sessao?> get() = sessionStore.sessao

    suspend fun entrar(telefone: String, senha: String): Resultado<Sessao> =
        chamar(autenticada = false) { api.entrar(LoginRequest(telefone, senha)) }
            .mapear { salvarSessao(it) }

    suspend fun registrar(
        nome: String,
        telefone: String,
        idade: Int?,
        perfil: String,
        senha: String,
    ): Resultado<Sessao> =
        chamar(autenticada = false) { api.registrar(RegistroRequest(nome, telefone, idade, perfil, senha)) }
            .mapear { salvarSessao(it) }

    fun sair() {
        sessionStore.limpar()
    }

    suspend fun medicamentos(): Resultado<List<Medicamento>> =
        chamar { api.medicamentos() }.mapear { lista -> lista.map { it.paraModelo() } }

    suspend fun criarMedicamento(
        nome: String,
        codigoBarras: String?,
        quantidade: Int,
        frequencia: String,
        horarios: List<String>,
    ): Resultado<Medicamento> {
        val usuarioId = sessionStore.sessao.value?.usuarioId
            ?: return Resultado.Erro("Sessão expirada. Entre novamente.", 401)
        val corpo = MedicamentoRequest(
            usuarioId = usuarioId,
            nomeComercial = nome,
            codigoBarras = codigoBarras?.takeIf { it.isNotBlank() },
            quantidadeAtual = quantidade,
            doseFrequencia = frequencia,
            horariosProgramados = horarios.joinToString(","),
        )
        return chamar { api.criarMedicamento(corpo) }.mapear { it.paraModelo() }
    }

    /** Doses do dia (o backend cria as pendentes a partir dos horarios dos medicamentos). */
    suspend fun agenda(data: LocalDate? = null): Resultado<List<Dose>> =
        chamar { api.agenda(data?.toString()) }.mapear { lista -> lista.map { it.paraModelo() } }

    suspend fun historico(de: LocalDate, ate: LocalDate): Resultado<List<Dose>> =
        chamar { api.historico(de.toString(), ate.toString()) }.mapear { lista -> lista.map { it.paraModelo() } }

    suspend fun atualizarStatus(doseId: Long, status: StatusDose): Resultado<Dose> =
        chamar { api.atualizarStatus(doseId, StatusRequest(status.name)) }.mapear { it.paraModelo() }

    private fun salvarSessao(auth: AuthResponse): Sessao {
        val sessao = Sessao(auth.token, auth.usuario.id, auth.usuario.nome)
        sessionStore.salvar(sessao)
        return sessao
    }

    private suspend fun <T> chamar(
        autenticada: Boolean = true,
        bloco: suspend () -> Response<T>,
    ): Resultado<T> {
        return try {
            val resposta = bloco()
            if (resposta.isSuccessful) {
                val corpo = resposta.body()
                if (corpo != null) Resultado.Sucesso(corpo) else Resultado.Erro("O servidor devolveu uma resposta vazia.")
            } else {
                if (resposta.code() == 401 && autenticada) {
                    sessionStore.limpar()
                }
                erroDaResposta(resposta.code(), resposta.errorBody()?.string())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Resultado.Erro("Sem conexão com o servidor. Verifique a internet e tente novamente.")
        } catch (e: Exception) {
            Resultado.Erro("Algo deu errado. Tente novamente.")
        }
    }

    private fun erroDaResposta(codigo: Int, corpo: String?): Resultado.Erro {
        val erroApi = runCatching { gson.fromJson(corpo, ErroApiResponse::class.java) }.getOrNull()
        val camposInvalidos = erroApi?.fieldErrors.orEmpty()
        val mensagemPadrao = when {
            codigo == 401 -> "Sessão expirada. Entre novamente."
            codigo == 403 -> "Você não tem permissão para fazer isso."
            codigo == 404 -> "Não encontramos o que você procurou."
            codigo >= 500 -> "O servidor está com problemas. Tente novamente em instantes."
            else -> "Não foi possível concluir. Verifique os dados e tente novamente."
        }
        val mensagem = when {
            camposInvalidos.isNotEmpty() -> "Confira os dados informados: " + camposInvalidos.values.joinToString("; ")
            !erroApi?.message.isNullOrBlank() -> erroApi?.message ?: mensagemPadrao
            else -> mensagemPadrao
        }
        return Resultado.Erro(mensagem, codigo, camposInvalidos)
    }

    private fun MedicamentoResponse.paraModelo() = Medicamento(
        id = id,
        nome = nomeComercial,
        codigoBarras = codigoBarras,
        quantidade = quantidadeAtual,
        estoqueBaixo = estoqueBaixo,
        frequencia = doseFrequencia,
        horarios = horariosProgramados.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    )

    private fun HistoricoResponse.paraModelo() = Dose(
        id = id,
        medicamentoId = medicamentoId,
        medicamentoNome = medicamentoNome,
        programadaPara = LocalDateTime.parse(dataHoraProgramada),
        realizadaEm = dataHoraRealizada?.let { LocalDateTime.parse(it) },
        status = runCatching { StatusDose.valueOf(status) }.getOrDefault(StatusDose.PENDENTE),
    )
}
