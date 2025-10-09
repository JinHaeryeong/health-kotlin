package com.example.healthconnect.codelab.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healthconnect.codelab.R
import com.example.healthconnect.codelab.presentation.theme.HealthConnectTheme



@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    // 상태 변수 정의 (입력 값)
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isInputValid = userId.isNotBlank() && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // 화면 중앙에 배치
    ) {
        Text(
            text = stringResource(R.string.login_screen_title),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 사용자 ID 입력 필드
        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text(stringResource(R.string.user_id_label)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // 비밀번호 입력 필드
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // 환자 번호 입력 필드 (Patient ID/Parent ID)
//        OutlinedTextField(
//            value = patientId,
//            onValueChange = { patientId = it },
//            label = { Text(stringResource(R.string.patient_id_label)) },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
//        )

        Spacer(modifier = Modifier.height(32.dp))

        //  로그인 버튼
        Button(
            // 모든 필드가 채워져야 활성화
            enabled = isInputValid,
            onClick = {
                // TODO: 실제로는 여기서 Spring Boot 로그인 API 호출 및 JWT/UserID 저장
                // 테스트 목적으로는 바로 성공 콜백 호출
                onLoginSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.login_button_label))
        }

        // 회원가입 버튼 (테스트용)
        TextButton(
            onClick = {
                onNavigateToSignup()
            },
            modifier = Modifier.padding(top = 16.dp),

        ) {
            Text(stringResource(R.string.signup_button_label))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    HealthConnectTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToSignup = {}
        )
    }
}