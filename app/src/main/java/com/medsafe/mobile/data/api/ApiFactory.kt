package com.medsafe.mobile.data.api

import com.medsafe.mobile.BuildConfig
import com.medsafe.mobile.data.SessionStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    fun criar(sessionStore: SessionStore): MedSafeApi {
        // Anexa o token a todas as chamadas, exceto login e cadastro.
        val autenticacao = Interceptor { chain ->
            val original = chain.request()
            val token = sessionStore.tokenAtual()
            val requisicao = if (token != null && !original.url.encodedPath.startsWith("/api/auth")) {
                original.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                original
            }
            chain.proceed(requisicao)
        }

        val cliente = OkHttpClient.Builder()
            .addInterceptor(autenticacao)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    // BASIC registra so metodo/URL/status, sem corpos: senha e token nao vao para o logcat.
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedSafeApi::class.java)
    }
}
