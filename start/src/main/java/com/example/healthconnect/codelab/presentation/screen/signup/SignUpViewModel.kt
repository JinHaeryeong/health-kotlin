package com.example.healthconnect.codelab.presentation.screen.signup

import android.util.Log // 🌟 Android 표준 로깅 import
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnect.codelab.data.model.SignupRequest
import com.example.healthconnect.codelab.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow // 🌟 이벤트 처리를 위한 import
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SignUpViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "SignUpViewModel"

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _signupSuccess = Channel<Boolean>(Channel.BUFFERED)
    val signupSuccessEvent = _signupSuccess.receiveAsFlow() // Compose Layer에서 observe할 Flow

    /**
     * 회원가입 버튼 클릭 시 호출되어 서버와 통신을 시작
     */
    fun signup(
        userId: String,
        password: String,
        patientName: String,
        patientAge: String,
        selectedConditions: Set<String>,
        otherConditionDetail: String
    ) {
        // Compose에서 이미 유효성 검사를 했지만, 안전을 위해 ViewModel에서 숫자 변환 확인
        val ageInt = patientAge.toIntOrNull()
        if (ageInt == null || ageInt <= 0) {
            errorMessage = "나이가 유효하지 않습니다."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // 1. 서버로 전송할 DTO 생성
            val request = SignupRequest(
                userId = userId,
                password = password,
                patientName = patientName,
                patientAge = ageInt,
                selectedConditions = selectedConditions.toList(), // Set을 List로 변환
                otherConditionDetail = otherConditionDetail
            )

            try {
                authRepository.signup(request)

                Log.d(TAG, "회원가입 성공: $userId")

                _signupSuccess.send(true)

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "알 수 없는 서버 오류 (Code: ${e.code()})"
                errorMessage = "회원가입 실패: ${if (e.code() == 409) "이미 존재하는 사용자 ID입니다." else errorBody}"
                Log.e(TAG, "HTTP 오류: ${e.code()}, 상세: $errorBody")

            } catch (e: IOException) {
                errorMessage = "네트워크 연결 오류입니다. 서버 주소(IP, 포트)를 확인해주세요."
                Log.e(TAG, "네트워크 오류: ${e.message}")

            } catch (e: Exception) {
                errorMessage = "예상치 못한 오류가 발생했습니다: ${e.localizedMessage}"
                Log.e(TAG, "일반 오류:", e)
            } finally {
                isLoading = false
            }
        }
    }
}
