package com.example.healthconnect.codelab.presentation.screen.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll // 스크롤을 위해 추가
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.InputChip
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel 주입을 위해 추가
import com.example.healthconnect.codelab.R

val ConditionOptions = listOf(
    "없음",
    "당뇨병 (Diabetes)",
    "고혈압 (Hypertension)",
    "심장 질환 (Heart Disease)",
    "갑상선 질환 (Thyroid Disease)",
    "기타 복용약/만성질환"
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        // success event Flow를 수집
        viewModel.signupSuccessEvent.collect { isSuccess ->
            if (isSuccess) {
                onSignupSuccess() // Navigation 실행
            }
        }
    }

    // 사용자 정보 상태
    var userId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // 환자 정보 상태
    var patientName by rememberSaveable { mutableStateOf("") }
    var patientAge by rememberSaveable { mutableStateOf("") }

    var selectedConditions by rememberSaveable { mutableStateOf(setOf<String>()) }
    var otherConditionDetail by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) } // 항목 추가 메뉴 상태

    // 유효성 검사 로직
    val isAgeValid = patientAge.toIntOrNull() != null && patientAge.toInt() > 0
    val isOtherSelected = selectedConditions.contains("기타 복용약/만성질환")

    val isInputValid = userId.isNotBlank() && password.isNotBlank() &&
            patientName.isNotBlank() && isAgeValid &&
            (!isOtherSelected || otherConditionDetail.isNotBlank()) &&
            selectedConditions.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()), // 🌟 세로 스크롤 가능하도록 추가
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.signup_screen_title),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = userId, onValueChange = { userId = it },
            label = { Text(stringResource(R.string.user_id_label)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "환자 정보 입력",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.align(Alignment.Start)
        )

        OutlinedTextField(
            value = patientName, onValueChange = { patientName = it },
            label = { Text(stringResource(R.string.patient_name_label)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = patientAge, onValueChange = { patientAge = it.filter { it.isDigit() } },
            label = { Text(stringResource(R.string.patient_age_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = patientAge.isNotBlank() && !isAgeValid,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.patient_condition_label), // "복용 약/지병" 라벨
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 선택된 항목 Chip 표시 영역 (가로 스크롤)
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().horizontalScroll(
                rememberScrollState()
            ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedConditions.forEach { condition ->
                    // '없음'과 다른 항목이 함께 선택된 경우 '없음'은 표시하지 않음
                    if (condition == "없음" && selectedConditions.size > 1) return@forEach

                    InputChip(
                        onClick = { /* 제거는 trailingIcon에서 처리 */ },
                        label = { Text(condition, style = MaterialTheme.typography.caption) },
                        selected = true,
                        // Chip 제거 아이콘
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                Modifier.size(18.dp).clickable {
                                    selectedConditions = selectedConditions.minus(condition)
                                    if (condition == "기타 복용약/만성질환") { otherConditionDetail = "" }
                                    // 모든 항목 제거 시 '없음'을 기본값으로 추가
                                    if (selectedConditions.isEmpty()) { selectedConditions = setOf("없음") }
                                }
                            )
                        },
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = "질병 추가", // 버튼 역할이므로 고정 텍스트
                    onValueChange = { },
                    trailingIcon = {
                        Icon(Icons.Filled.Add, contentDescription = "추가")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ConditionOptions.forEach { selectionOption ->
                        // 이미 선택된 항목은 메뉴에서 제외
                        val isAlreadySelected = selectedConditions.contains(selectionOption)

                        if (!isAlreadySelected || selectionOption == "없음") {
                            DropdownMenuItem(
                                onClick = {
                                    selectedConditions = when (selectionOption) {
                                        "없음" -> setOf("없음") // '없음' 선택 시 다른 모든 항목 제거
                                        else -> selectedConditions.minus("없음").plus(selectionOption) // 다른 항목 선택 시 '없음' 제거 및 항목 추가
                                    }
                                    expanded = false
                                }
                            ) {
                                Text(text = selectionOption)
                            }
                        }
                    }
                }
            }
        }

        if (isOtherSelected) {
            OutlinedTextField(
                value = otherConditionDetail,
                onValueChange = { otherConditionDetail = it },
                label = { Text("기타 질병 상세") },
                isError = otherConditionDetail.isBlank(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 회원가입 완료 버튼
        Button(
            enabled = isInputValid && !viewModel.isLoading,
            onClick = {
                viewModel.signup(
                    userId = userId,
                    password = password,
                    patientName = patientName,
                    patientAge = patientAge,
                    selectedConditions = selectedConditions,
                    otherConditionDetail = otherConditionDetail
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            // 로딩 중일 때 로딩 인디케이터 표시
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.signup_button_label))
            }
        }

        // 뒤로가기 버튼
        TextButton(
            onClick = { onNavigateBack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.back_to_login))
        }
    }
}
