package com.example.healthconnect.codelab.data.source.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {

    //실물 기기 쓰니까 ip주소 쓰는거 에뮬레이터로 돌릴거면 10.0.2.2인가쓰면됨
    private const val BASE_URL = "http://192.168.219.110:8080"

    val authApi: AuthApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 개발 시 응답/요청 본문 로깅
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(AuthApi::class.java)
    }
}