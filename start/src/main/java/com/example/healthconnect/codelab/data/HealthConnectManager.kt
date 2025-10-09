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
package com.example.healthconnect.codelab.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthconnect.codelab.workers.ReadStepWorker
import java.io.IOException
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.health.connect.client.records.metadata.Metadata
import java.time.Duration
import java.time.temporal.ChronoUnit

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
  private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

  var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
    private set

  init {
    checkAvailability()
  }

  fun checkAvailability() {
    availability.value = when {
      HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
      isSupported() -> HealthConnectAvailability.NOT_INSTALLED
      else -> HealthConnectAvailability.NOT_SUPPORTED
    }
  }

  fun isFeatureAvailable(feature: Int): Boolean{
    return healthConnectClient
      .features
      .getFeatureStatus(feature) == HealthConnectFeatures.FEATURE_STATUS_AVAILABLE
  }

  /**
   * Determines whether all the specified permissions are already granted. It is recommended to
   * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
   * permissions are already granted then there is no need to request permissions via
   * [PermissionController.createRequestPermissionResultContract].
   */
  suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
    return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
  }

  fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
    return PermissionController.createRequestPermissionResultContract()
  }

  /**
   * TODO: Writes [WeightRecord] to Health Connect.
   */
  suspend fun writeWeightInput(weightInput: Double) {
    try {
      val time = Instant.now()
      val weightRecord = WeightRecord(
          weight = Mass.kilograms(weightInput),
          time = time,
          zoneOffset = ZonedDateTime.now().offset,
          metadata = Metadata.manualEntry()
      )
      
      healthConnectClient.insertRecords(listOf(weightRecord))

      Toast.makeText(context, "체중 ${weightInput}kg 기록 성공", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Log.e("HC_WRITE", "체중 기록 실패: ${e.message}")
      Toast.makeText(context, "체중 기록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * TODO: Reads in existing [WeightRecord]s.
   */
  suspend fun readWeightInputs(start: Instant, end: Instant): List<WeightRecord> {
    val request = ReadRecordsRequest(
      recordType = WeightRecord::class,
      timeRangeFilter = TimeRangeFilter.between(start, end)
    )
    val response = healthConnectClient.readRecords(request)
    return response.records
  }

  /**
   * TODO: Returns the weekly average of [WeightRecord]s.
   */
  suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
    // Toast.makeText(context, "TODO: get average weight", Toast.LENGTH_SHORT).show()
    val request = AggregateRequest(
      metrics = setOf(WeightRecord.WEIGHT_AVG),
      timeRangeFilter = TimeRangeFilter.between(start, end)
    )
    val response = healthConnectClient.aggregate(request)
    return response[WeightRecord.WEIGHT_AVG]
  }

  /**
   * TODO: Obtains a list of [ExerciseSessionRecord]s in a specified time frame.
   */
  suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
    // Toast.makeText(context, "TODO: read exercise sessions", Toast.LENGTH_SHORT).show()
    val request = ReadRecordsRequest(
      recordType = ExerciseSessionRecord::class,
      timeRangeFilter = TimeRangeFilter.between(start, end)
    )
    val response = healthConnectClient.readRecords(request)
    return response.records
  }

  /**
   * TODO: Writes an [ExerciseSessionRecord] to Health Connect.
   */
  @SuppressLint("RestrictedApi")
  @Override
  suspend fun writeExerciseSession(start: ZonedDateTime, end: ZonedDateTime) {
    try {
    healthConnectClient.insertRecords(
      listOf<Record>(
        ExerciseSessionRecord(
          metadata = Metadata.manualEntry(),
          startTime = start.toInstant(),
          startZoneOffset = start.offset,
          endTime = end.toInstant(),
          endZoneOffset = end.offset,
          exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
          title = "My Run #${Random.nextInt(0, 60)}"
        ),
        StepsRecord(
          metadata = Metadata.manualEntry(),
          startTime = start.toInstant(),
          startZoneOffset = start.offset,
          endTime = end.toInstant(),
          endZoneOffset = end.offset,
          count = (1000 + 1000 * Random.nextInt(3)).toLong()
        ),
        TotalCaloriesBurnedRecord(
          metadata = Metadata.manualEntry(),
          startTime = start.toInstant(),
          startZoneOffset = start.offset,
          endTime = end.toInstant(),
          endZoneOffset = end.offset,
          energy = Energy.calories((140 + Random.nextInt(20)) * 0.01)
        )
      ) + buildHeartRateSeries(start, end)
    )
    Log.d("HC_WRITE", "운동 세션 기록 성공!")
    Toast.makeText(context, "운동 세션 기록 성공", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Log.e("HC_WRITE", "운동 세션 기록 실패: ${e.message}")
      Toast.makeText(context, "운동 세션 기록 실패: ${e.message}", Toast.LENGTH_LONG).show()
    }
  }
  /**
   * TODO: Build [HeartRateRecord].
   */
  private fun buildHeartRateSeries(
    sessionStartTime: ZonedDateTime,
    sessionEndTime: ZonedDateTime,
  ): HeartRateRecord {
    val samples = mutableListOf<HeartRateRecord.Sample>()
    var time = sessionStartTime
    while (time.isBefore(sessionEndTime)) {
      samples.add(
        HeartRateRecord.Sample(
          time = time.toInstant(),
          beatsPerMinute = (80 + Random.nextInt(80)).toLong()
        )
      )
      time = time.plusSeconds(30)
    }
    return HeartRateRecord(
      metadata = Metadata.manualEntry(),
      startTime = sessionStartTime.toInstant(),
      startZoneOffset = sessionStartTime.offset,
      endTime = sessionEndTime.toInstant(),
      endZoneOffset = sessionEndTime.offset,
      samples = samples
    )
  }

  private suspend fun readStepsByTimeRange(
    startTime: Instant,
    endTime: Instant
  ): List<StepsRecord> { // List<StepsRecord>를 반환하도록 수정
    return try {
      val response = healthConnectClient.readRecords(
        ReadRecordsRequest(
          StepsRecord::class,
          timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
      )
      Log.d("HC_DEBUG", "Raw Steps Read Count: ${response.records.size}")
      response.records
    } catch (e: Exception) {
      Log.e("HC_DEBUG", "Error reading raw steps: ${e.message}")
      emptyList()
    }
  }


  suspend fun readTotalStepsForDay(day: ZonedDateTime): Long? {
    val startOfDay = day.truncatedTo(ChronoUnit.DAYS).toInstant()
    val endOfDay = startOfDay.plus(Duration.ofDays(1)).minusMillis(1) // 하루의 끝

    val request = AggregateRequest(
      metrics = setOf(StepsRecord.COUNT_TOTAL),
      timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
    )

    val response = healthConnectClient.aggregate(request)
    // Long? 타입으로 반환됩니다.
    return response[StepsRecord.COUNT_TOTAL]
  }

  /**
   * TODO: Reads aggregated data and raw data for selected data types, for a given [ExerciseSessionRecord].
   */
//  suspend fun readAssociatedSessionData(
//      uid: String,
//  ): ExerciseSessionData {
//    val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
//    // Use the start time and end time from the session, for reading raw and aggregate data.
//    val timeRangeFilter = TimeRangeFilter.between(
//      startTime = exerciseSession.record.startTime,
//      endTime = exerciseSession.record.endTime
//    )
//    val aggregateDataTypes = setOf(
//      ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
//      StepsRecord.COUNT_TOTAL,
//      TotalCaloriesBurnedRecord.ENERGY_TOTAL,
//      HeartRateRecord.BPM_AVG,
//      HeartRateRecord.BPM_MAX,
//      HeartRateRecord.BPM_MIN,
//    )
//    // Limit the data read to just the application that wrote the session. This may or may not
//    // be desirable depending on the use case: In some cases, it may be useful to combine with
//    // data written by other apps.
//    val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)
//    val aggregateRequest = AggregateRequest(
//      metrics = aggregateDataTypes,
//      timeRangeFilter = timeRangeFilter,
//      //이거때문에 걸음수 안나오는걸수도있음
//      dataOriginFilter = dataOriginFilter
//    )
//
//    val aggregateData = healthConnectClient.aggregate(aggregateRequest)
//
//    val stepsRequest = AggregateRequest(
//      metrics = setOf(StepsRecord.COUNT_TOTAL),
//      timeRangeFilter = timeRangeFilter,
//    )
//
////    val stepsTets = readStepsByTimeRange(healthConnectClient,exerciseSession.record.startTime, exerciseSession.record.endTime)
//
//    val stepsData = healthConnectClient.aggregate(stepsRequest)
////    Log.d("HC_DEBUG", "걍 토탈 찍어보자 아니 나도 모르겠음: ${stepsTets.toString()}")
//
//    Log.d("HC_DEBUG", "Aggregate Data Map (no steps): $aggregateData")
//    Log.d("HC_DEBUG", "Steps COUNT_TOTAL result: ${stepsData[StepsRecord.COUNT_TOTAL]}")
//    Log.d("HC_DEBUG", "Time Range: ${timeRangeFilter.startTime} ~ ${timeRangeFilter.endTime}")
//
//    Log.d("HC_DEBUG", "확인용: ${StepsRecord.COUNT_TOTAL}")
//
//    val heartRateData = readData<HeartRateRecord>(timeRangeFilter, dataOriginFilter)
//
//    return ExerciseSessionData(
//      uid = uid,
//      totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
//      totalSteps = stepsData[StepsRecord.COUNT_TOTAL],
//      totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
//      minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
//      maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
//      avgHeartRate = aggregateData[HeartRateRecord.BPM_AVG],
//      heartRateSeries = heartRateData,
//    )
//  }

//  suspend fun readAssociatedSessionData(
//    uid: String,
//  ): ExerciseSessionData {
//    val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
//    // Use the start time and end time from the session, for reading raw and aggregate data.
//    val timeRangeFilter = TimeRangeFilter.between(
//      startTime = exerciseSession.record.startTime,
//      endTime = exerciseSession.record.endTime
//    )
//    val aggregateDataTypes = setOf(
//      ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
//      StepsRecord.COUNT_TOTAL,
//      TotalCaloriesBurnedRecord.ENERGY_TOTAL,
//      HeartRateRecord.BPM_AVG,
//      HeartRateRecord.BPM_MAX,
//      HeartRateRecord.BPM_MIN,
//    )
//
//    // 이 필터는 StepsRecord의 null 원인이었으므로 HeartRateRecord를 제외한 모든 aggregate에서 사용X
//    val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)
//
//    // 걸음 수, 칼로리 등 범용 데이터 집계 요청 (필터 없이)
//    val aggregateRequestStepsEnergy = AggregateRequest(
//      metrics = setOf(StepsRecord.COUNT_TOTAL, TotalCaloriesBurnedRecord.ENERGY_TOTAL),
//      timeRangeFilter = timeRangeFilter
//    )
//    val aggregateDataStepsEnergy = healthConnectClient.aggregate(aggregateRequestStepsEnergy)
//
//    // 심박수 등 원본 앱의 데이터 집계 요청 (필터 포함)
//    val aggregateRequestHeartRate = AggregateRequest(
//      metrics = setOf(HeartRateRecord.BPM_AVG, HeartRateRecord.BPM_MAX, HeartRateRecord.BPM_MIN, ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
//      timeRangeFilter = timeRangeFilter,
//      dataOriginFilter = dataOriginFilter
//    )
//    val aggregateDataHeartRate = healthConnectClient.aggregate(aggregateRequestHeartRate)
//
//    // 로그 (깔끔하게 정리)
//    Log.d("HC_DEBUG", "Steps Result: ${aggregateDataStepsEnergy[StepsRecord.COUNT_TOTAL]}")
//    Log.d("HC_DEBUG", "Time Range: ${timeRangeFilter.startTime} ~ ${timeRangeFilter.endTime}")
//
//    val heartRateData = readData<HeartRateRecord>(timeRangeFilter, dataOriginFilter)
//
//    return ExerciseSessionData(
//      uid = uid,
//      totalActiveTime = aggregateDataHeartRate[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
//      totalSteps = aggregateDataStepsEnergy[StepsRecord.COUNT_TOTAL],
//      totalEnergyBurned = aggregateDataStepsEnergy[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
//      minHeartRate = aggregateDataHeartRate[HeartRateRecord.BPM_MIN],
//      maxHeartRate = aggregateDataHeartRate[HeartRateRecord.BPM_MAX],
//      avgHeartRate = aggregateDataHeartRate[HeartRateRecord.BPM_AVG],
//      heartRateSeries = heartRateData,
//    )
//  }

  // HealthConnectManager.kt 내부

  suspend fun readAssociatedSessionData(
    uid: String,
  ): ExerciseSessionData {
    val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
    // Use the start time and end time from the session, for reading raw and aggregate data.
    val timeRangeFilter = TimeRangeFilter.between(
      startTime = exerciseSession.record.startTime,
      endTime = exerciseSession.record.endTime
    )

    val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)

    // ... (aggregateRequestStepsEnergy, aggregateRequestHeartRate 등 기존 코드 생략)

    //걸음 수, 칼로리 등 범용 데이터 집계 요청 (필터 없이)
    val aggregateRequestStepsEnergy = AggregateRequest(
      metrics = setOf(StepsRecord.COUNT_TOTAL, TotalCaloriesBurnedRecord.ENERGY_TOTAL),
      timeRangeFilter = timeRangeFilter
    )
    val aggregateDataStepsEnergy = healthConnectClient.aggregate(aggregateRequestStepsEnergy)

    // 심박수 등 원본 앱의 데이터 집계 요청 (필터 포함)
    val aggregateRequestHeartRate = AggregateRequest(
      metrics = setOf(HeartRateRecord.BPM_AVG, HeartRateRecord.BPM_MAX, HeartRateRecord.BPM_MIN, ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
      timeRangeFilter = timeRangeFilter,
      dataOriginFilter = dataOriginFilter
    )
    val aggregateDataHeartRate = healthConnectClient.aggregate(aggregateRequestHeartRate)

    // 하루 총 걸음 수 데이터 로드
    // 세션의 시작 시간을 사용하여 해당 날짜 전체의 걸음 수를 요청
    val sessionDay = ZonedDateTime.ofInstant(exerciseSession.record.startTime, exerciseSession.record.startZoneOffset)
    val totalStepsForTheDay = readTotalStepsForDay(sessionDay)


    //  로그 (깔끔하게 정리)
    Log.d("HC_DEBUG", "Steps Result (Session): ${aggregateDataStepsEnergy[StepsRecord.COUNT_TOTAL]}")
    Log.d("HC_DEBUG", "Steps Result (Day Total): $totalStepsForTheDay") // 새로운 로그 추가
    Log.d("HC_DEBUG", "Time Range: ${timeRangeFilter.startTime} ~ ${timeRangeFilter.endTime}")

    val heartRateData = readData<HeartRateRecord>(timeRangeFilter, dataOriginFilter)

    return ExerciseSessionData(
      uid = uid,
      totalActiveTime = aggregateDataHeartRate[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
      totalSteps = aggregateDataStepsEnergy[StepsRecord.COUNT_TOTAL],
      totalEnergyBurned = aggregateDataStepsEnergy[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
      minHeartRate = aggregateDataHeartRate[HeartRateRecord.BPM_MIN],
      maxHeartRate = aggregateDataHeartRate[HeartRateRecord.BPM_MAX],
      avgHeartRate = aggregateDataHeartRate[HeartRateRecord.BPM_AVG],
      heartRateSeries = heartRateData,
      // 반환 객체에 totalStepsForDay 추가 (ExerciseSessionData 수정 필요)
      totalStepsForDay = totalStepsForTheDay
    )
  }
  /**
   * TODO: Obtains a changes token for the specified record types.
   */
  suspend fun getChangesToken(): String {
    return healthConnectClient.getChangesToken(
      ChangesTokenRequest(
        setOf(
          ExerciseSessionRecord::class
        )
      )
    )
  }

  /**
   * TODO: Retrieve changes from a changes token.
   */
  suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
    var nextChangesToken = token
    do {
      val response = healthConnectClient.getChanges(nextChangesToken)
      if (response.changesTokenExpired) {
        throw IOException("Changes token has expired")
      }
      emit(ChangesMessage.ChangeList(response.changes))
      nextChangesToken = response.nextChangesToken
    } while (response.hasMore)
    emit(ChangesMessage.NoMoreChanges(nextChangesToken))  }

  /**
   * Enqueue the ReadStepWorker
   */
  fun enqueueReadStepWorker(){
    val readRequest = OneTimeWorkRequestBuilder<ReadStepWorker>()
      .setInitialDelay(10, TimeUnit.SECONDS)
      .build()
    WorkManager.getInstance(context).enqueue(readRequest)
  }

  /**
   * Convenience function to reuse code for reading data.
   */
  private suspend inline fun <reified T : Record> readData(
      timeRangeFilter: TimeRangeFilter,
      dataOriginFilter: Set<DataOrigin> = setOf(),
  ): List<T> {
    val request = ReadRecordsRequest(
      recordType = T::class,
      dataOriginFilter = dataOriginFilter,
      timeRangeFilter = timeRangeFilter
    )
    return healthConnectClient.readRecords(request).records
  }

  private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

  // Represents the two types of messages that can be sent in a Changes flow.
  sealed class ChangesMessage {
    data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
    data class ChangeList(val changes: List<Change>) : ChangesMessage()
  }
}



/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
  INSTALLED,
  NOT_INSTALLED,
  NOT_SUPPORTED
}
