package com.example.healthconnect.codelab.presentation.screen.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.healthconnect.codelab.R
// ... (기타 임포트)

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
    onNavigateBack: () -> Unit
) {
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
            .padding(horizontal = 32.dp),
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

        // 복용 약/지병 Multi-Select UI
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.patient_condition_label), // "복용 약/지병" 라벨
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 선택된 항목 Chip 표시 영역
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedConditions.forEach { condition ->
                    // 선택된 항목이 '없음'이고 다른 항목도 같이 선택되어 있다면 '없음'을 제거하기ㅏ..~
                    if (condition == "없음" && selectedConditions.size > 1) return@forEach

                    InputChip(
                        onClick = { /* 칩 클릭 시 동작 (제거) */ },
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

            // 2. 새로운 항목을 추가하는 Dropdown 메뉴 (버튼 역할)
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

        // 기타 상세 입력 필드 (Multi-Select에 연결)
        if (isOtherSelected) {
            OutlinedTextField(
                value = otherConditionDetail,
                onValueChange = { otherConditionDetail = it },
                label = { Text("기타 질병 상세") },
                isError = otherConditionDetail.isBlank(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 회원가입 완료 버튼
        Button(
            enabled = isInputValid,
            onClick = {
                // TODO: ViewModel.signup() 호출
                onSignupSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.signup_button_label))
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