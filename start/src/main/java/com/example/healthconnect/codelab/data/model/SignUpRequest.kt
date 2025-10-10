package com.example.healthconnect.codelab.data.model

data class SignupRequest(
    // 필드명은 서버의 DTO와 100% 일치해야 함
    val userId: String,
    val password: String,
    val patientName: String,
    val patientAge: Int,
    val selectedConditions: List<String>,
    val otherConditionDetail: String
)