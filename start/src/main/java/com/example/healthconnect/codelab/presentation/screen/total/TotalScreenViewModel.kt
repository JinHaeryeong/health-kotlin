package com.example.healthconnect.codelab.presentation.screen.total

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnect.codelab.data.HealthConnectManager
import java.time.Instant
import java.time.ZonedDateTime
import kotlinx.coroutines.launch

// Hilt 의존성 주입 없이 Manager를 생성자에서 직접 받습니다.
class TotalViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    fun loadAllHealthDataForLogging() {
        viewModelScope.launch {
            // 최근 7일간의 데이터를 읽도록 시간 범위를 설정합니다.
            val startTime = ZonedDateTime.now().minusDays(7).toInstant()
            val endTime = Instant.now()

            // Logcat에서 "ALL_DATA_CHECK" 태그로 필터링하세요.

            // 데이터 로그 함수 호출:

            // 1. 걸음 수 (Steps) - 현재 날짜의 총 걸음 수
            healthConnectManager.readTotalStepsForDay(ZonedDateTime.now())

            // 2. 활력 징후 (Vital Signs) - 7일치
            healthConnectManager.readAndLogBloodGlucose(startTime, endTime)
            healthConnectManager.readAndLogBloodPressure(startTime, endTime)
            healthConnectManager.readAndLogRespiratoryRate(startTime, endTime)

            // TODO: 매니페스트에 있는 다른 readAndLog 함수들을 여기에 추가하세요.
        }
    }
}