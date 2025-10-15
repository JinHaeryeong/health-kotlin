package com.example.healthconnect.codelab.presentation.screen.total

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthconnect.codelab.data.HealthConnectManager
import com.example.healthconnect.codelab.presentation.BaseApplication

@Composable
fun TotalScreen(
    // 매니저 객체를 외부(예: MainActivity)에서 받도록 Composable 함수를 수정해야 합니다.
    // 여기서는 Codelab의 BaseApplication에서 Manager를 가져온다고 가정합니다.
    healthConnectManager: HealthConnectManager = (LocalContext.current.applicationContext as BaseApplication).healthConnectManager
) {
    // ViewModel을 수동으로 생성하고 Manager를 전달합니다.
    val viewModel: TotalViewModel = remember {
        TotalViewModel(healthConnectManager = healthConnectManager)
    }

    // 1. 화면이 처음 로드되었을 때 (Composition이 처음 발생했을 때) 딱 한 번만 실행
    LaunchedEffect(Unit) {
        viewModel.loadAllHealthDataForLogging()
    }

    // 2. 간단한 UI 표시
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Health Connect 데이터 로깅 중", modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Logcat에서 'ALL_DATA_CHECK' 태그를 확인하세요.", modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "⚠️ 이 화면은 데이터 확인용이며, 실제 UI는 구현되지 않았습니다.", color = androidx.compose.ui.graphics.Color.Gray)
    }
}