package com.example.healthconnect.codelab.data.source.api

import com.example.healthconnect.codelab.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<Unit>
}