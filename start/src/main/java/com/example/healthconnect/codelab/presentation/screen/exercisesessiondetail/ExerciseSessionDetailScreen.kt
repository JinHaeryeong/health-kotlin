/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.healthconnect.codelab.presentation.screen.exercisesessiondetail

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Velocity
import com.example.healthconnect.codelab.R
import com.example.healthconnect.codelab.data.ExerciseSessionData
import com.example.healthconnect.codelab.data.formatTime
import com.example.healthconnect.codelab.presentation.component.ExerciseSessionDetailsMinMaxAvg
import com.example.healthconnect.codelab.presentation.component.HeartRateChart
import com.example.healthconnect.codelab.presentation.component.sessionDetailsItem
import com.example.healthconnect.codelab.presentation.theme.HealthConnectTheme
import java.text.DecimalFormat
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * Shows a details of a given [ExerciseSessionRecord], including aggregates and underlying raw data.
 */
@Composable
fun ExerciseSessionDetailScreen(
  permissions: Set<String>,
  permissionsGranted: Boolean,
  sessionMetrics: ExerciseSessionData,
  uiState: ExerciseSessionDetailViewModel.UiState,
  onError: (Throwable?) -> Unit = {},
  onPermissionsResult: () -> Unit = {},
  onPermissionsLaunch: (Set<String>) -> Unit = {},
) {

  // Remember the last error ID, such that it is possible to avoid re-launching the error
  // notification for the same error when the screen is recomposed, or configuration changes etc.
  val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

  LaunchedEffect(uiState) {
    // If the initial data load has not taken place, attempt to load the data.
    if (uiState is ExerciseSessionDetailViewModel.UiState.Uninitialized) {
      onPermissionsResult()
    }

    // The [ExerciseSessionDetailViewModel.UiState] provides details of whether the last action
    // was a success or resulted in an error. Where an error occurred, for example in reading
    // and writing to Health Connect, the user is notified, and where the error is one that can
    // be recovered from, an attempt to do so is made.
    if (uiState is ExerciseSessionDetailViewModel.UiState.Error &&
      errorId.value != uiState.uuid
    ) {
      onError(uiState.exception)
      errorId.value = uiState.uuid
    }
  }

  if (uiState != ExerciseSessionDetailViewModel.UiState.Uninitialized) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (!permissionsGranted) {
        item {
          Button(
            onClick = { onPermissionsLaunch(permissions) }
          ) {
            Text(text = stringResource(R.string.permissions_button_label))
          }
        }
      } else {
        Log.v("HC_DEBUG", "sessionMetrics: $sessionMetrics")
        sessionDetailsItem(labelId = R.string.total_active_duration) {
          val activeDuration = sessionMetrics.totalActiveTime ?: Duration.ZERO
          Text(activeDuration.formatTime())
        }

        // totalSteps를 구글에서 안 받아 준다는 데 그럼 왜 함수는 만들어 둔거지????????

        sessionDetailsItem(labelId = R.string.total_distance) {
          val distanceKm = sessionMetrics.totalDistance?.inKilometers
          Text(
            distanceKm?.let { String.format("%.2f km", it) }
              ?: stringResource(id = R.string.not_available_abbrev)
          )
        }

        sessionDetailsItem(labelId = R.string.total_steps) {
          Text(sessionMetrics.totalSteps?.toString() ?: stringResource(id = R.string.not_available_abbrev))
        }
        sessionDetailsItem(labelId = R.string.total_steps_for_day) { // stringResource를 새로 정의 해야 함
          Text(sessionMetrics.totalStepsForDay?.toString() ?: stringResource(R.string.not_available_abbrev))
        }
        sessionDetailsItem(labelId = R.string.total_energy) {
          Text(formatEnergy(sessionMetrics.totalEnergyBurned))
        }

        sessionDetailsItem(labelId = R.string.speed_status) {
          ExerciseSessionDetailsMinMaxAvg(
            sessionMetrics.minSpeed?.toKmPerHour()?.let { "${"%.1f".format(it)} km/h" }
              ?: stringResource(id = R.string.not_available_abbrev),
            sessionMetrics.maxSpeed?.toKmPerHour()?.let { "${"%.1f".format(it)} km/h" }
              ?: stringResource(id = R.string.not_available_abbrev),
            sessionMetrics.avgSpeed?.toKmPerHour()?.let { "${"%.1f".format(it)} km/h" }
              ?: stringResource(id = R.string.not_available_abbrev)
          )
        }
        sessionDetailsItem(labelId = R.string.hr_stats) {
          ExerciseSessionDetailsMinMaxAvg(
            sessionMetrics.minHeartRate?.toString()
              ?: stringResource(id = R.string.not_available_abbrev),
            sessionMetrics.maxHeartRate?.toString()
              ?: stringResource(id = R.string.not_available_abbrev),
            sessionMetrics.avgHeartRate?.toString()
              ?: stringResource(id = R.string.not_available_abbrev)
          )
        }
        item {
          // 차트 제목 (기존 seriesHeading 대신 직접 Text로 구현)
          Text(
            stringResource(R.string.hr_series),
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
          )

          // HeartRateChart 컴포저블 호출
          HeartRateChart(
            labelId = R.string.hr_series,
            series = sessionMetrics.heartRateSeries
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun ExerciseSessionScreenPreview() {
  HealthConnectTheme {
    val uid = UUID.randomUUID().toString()
    val sessionMetrics = ExerciseSessionData(
      uid = uid,
      totalSteps = 5152,
      totalStepsForDay = 13943L,
      totalDistance = Length.meters(11923.4),
      totalEnergyBurned = Energy.calories(1131.2),
      minHeartRate = 55,
      maxHeartRate = 103,
      avgHeartRate = 77,
      heartRateSeries = generateHeartRateSeries(),
      minSpeed = Velocity.metersPerSecond(2.5),
      maxSpeed = Velocity.metersPerSecond(3.1),
      avgSpeed = Velocity.metersPerSecond(2.8),
      speedRecord = generateSpeedData(),
    )

    ExerciseSessionDetailScreen(
      permissions = setOf(),
      permissionsGranted = true,
      sessionMetrics = sessionMetrics,
      uiState = ExerciseSessionDetailViewModel.UiState.Done
    )
  }
}

private fun generateSpeedData(): List<SpeedRecord> {
  val data = mutableListOf<SpeedRecord.Sample>()
  val end = ZonedDateTime.now()
  var time = ZonedDateTime.now()
  for (index in 1..10) {
    time = end.minusMinutes(index.toLong())
    data.add(
      SpeedRecord.Sample(
        time = time.toInstant(),
        speed = Velocity.metersPerSecond((Random.nextDouble(1.0, 5.0)))
      )
    )
  }
  return listOf(
    SpeedRecord(
      metadata = Metadata.manualEntry(),
      startTime = time.toInstant(),
      startZoneOffset = time.offset,
      endTime = end.toInstant(),
      endZoneOffset = end.offset,
      samples = data
    )
  )
}

private fun generateHeartRateSeries(): List<HeartRateRecord> {
  val data = mutableListOf<HeartRateRecord.Sample>()
  val end = ZonedDateTime.now()
  var time = ZonedDateTime.now()
  for (index in 1..10) {
    time = end.minusMinutes(index.toLong())
    data.add(
      HeartRateRecord.Sample(
        time = time.toInstant(),
        beatsPerMinute = Random.nextLong(55, 180)
      )
    )
  }
  return listOf(
    HeartRateRecord(
      metadata = Metadata.manualEntry(),
      startTime = time.toInstant(),
      startZoneOffset = time.offset,
      endTime = end.toInstant(),
      endZoneOffset = end.offset,
      samples = data
    )
  )
}

//칼로리 표기가 이상해서 kcal로 변환하기
private fun formatEnergy(energy: Energy?): String {
  if (energy == null) {
    return "N/A"
  }
  val kcalValue = energy.inKilocalories

  val formatter = DecimalFormat("0.0")

  return "${formatter.format(kcalValue)} kcal"
}

fun Velocity.toKmPerHour(): Double = this.inMetersPerSecond * 3.6
