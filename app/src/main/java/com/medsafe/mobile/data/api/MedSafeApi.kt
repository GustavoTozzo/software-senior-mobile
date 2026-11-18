package com.medsafe.mobile.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MedSafeApi {

    @POST("api/auth/register")
    suspend fun registrar(@Body corpo: RegistroRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun entrar(@Body corpo: LoginRequest): Response<AuthResponse>

    @GET("api/medicamentos")
    suspend fun medicamentos(): Response<List<MedicamentoResponse>>

    @POST("api/medicamentos")
    suspend fun criarMedicamento(@Body corpo: MedicamentoRequest): Response<MedicamentoResponse>

    @PUT("api/medicamentos/{id}")
    suspend fun atualizarMedicamento(
        @Path("id") id: Long,
        @Body corpo: MedicamentoRequest,
    ): Response<MedicamentoResponse>

    @POST("api/medicamentos/{id}/retirada")
    suspend fun registrarRetirada(
        @Path("id") id: Long,
        @Body corpo: RetiradaRequest,
    ): Response<MedicamentoResponse>

    @GET("api/farmacias")
    suspend fun farmacias(): Response<List<FarmaciaResponse>>

    @POST("api/farmacias")
    suspend fun criarFarmacia(@Body corpo: FarmaciaRequest): Response<FarmaciaResponse>

    @PUT("api/farmacias/{id}")
    suspend fun atualizarFarmacia(@Path("id") id: Long, @Body corpo: FarmaciaRequest): Response<FarmaciaResponse>

    @DELETE("api/farmacias/{id}")
    suspend fun excluirFarmacia(@Path("id") id: Long): Response<Unit>

    @GET("api/contatos-emergencia")
    suspend fun contatos(): Response<List<ContatoEmergenciaResponse>>

    @POST("api/contatos-emergencia")
    suspend fun criarContato(@Body corpo: ContatoEmergenciaRequest): Response<ContatoEmergenciaResponse>

    @DELETE("api/contatos-emergencia/{id}")
    suspend fun excluirContato(@Path("id") id: Long): Response<Unit>

    @GET("api/historico-ingestao/agenda")
    suspend fun agenda(@Query("data") data: String?): Response<List<HistoricoResponse>>

    @GET("api/historico-ingestao")
    suspend fun historico(
        @Query("de") de: String?,
        @Query("ate") ate: String?,
    ): Response<List<HistoricoResponse>>

    @PATCH("api/historico-ingestao/{id}/status")
    suspend fun atualizarStatus(
        @Path("id") id: Long,
        @Body corpo: StatusRequest,
    ): Response<HistoricoResponse>
}
