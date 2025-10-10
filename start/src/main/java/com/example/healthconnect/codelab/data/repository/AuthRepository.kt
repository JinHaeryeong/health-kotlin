package com.example.healthconnect.codelab.data.repository

import com.example.healthconnect.codelab.data.model.SignupRequest
import com.example.healthconnect.codelab.data.source.api.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {

    private val authApi = RetrofitClient.authApi
    suspend fun signup(request: SignupRequest) {
        val response = authApi.signup(request)

        // 응답이 성공(2xx)이 아닐 경우 예외 처리
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}
