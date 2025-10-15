package com.example.healthconnect.codelab.presentation.screen.total

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.healthconnect.codelab.R // stringResource를 위해 필요
import com.example.healthconnect.codelab.data.HealthConnectManager
import com.example.healthconnect.codelab.presentation.BaseApplication

@Composable
fun TotalScreen(
    healthConnectManager: HealthConnectManager = (LocalContext.current.applicationContext as BaseApplication).healthConnectManager
) {
    val viewModel: TotalViewModel = remember {
        TotalViewModel(healthConnectManager = healthConnectManager)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        viewModel.permissionsLauncher
    ) {
        viewModel.initialLoad()
    }

    LaunchedEffect(Unit) {
        viewModel.initialLoad()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!viewModel.permissionsGranted.value) {
            Text(
                text = "Health Connect 데이터를 읽을 권한이 필요합니다.",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = {
                    // 권한 요청 런처 실행
                    permissionLauncher.launch(viewModel.permissions)
                }
            ) {
                Text(text = stringResource(R.string.permissions_button_label)) // 문자열 리소스 사용 권장
            }
        }
        else {
            Text(text = "Health Connect 데이터 로깅 중", modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "Logcat에서 'ALL_DATA_CHECK' 태그를 확인하세요.", modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "이 화면은 데이터 확인용이며, 실제 UI는 구현되지 않았습니다.", color = androidx.compose.ui.graphics.Color.Gray)
        }

    }
}