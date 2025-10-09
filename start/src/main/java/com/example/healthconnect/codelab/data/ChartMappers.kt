package com.example.healthconnect.codelab.data

import androidx.health.connect.client.records.HeartRateRecord
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// 시각화를 위한 단순 데이터 모델 정의
data class HeartRateDataPoint(
    val time: ZonedDateTime,
    val beatsPerMinute: Long
)


fun List<HeartRateRecord>.toChartDataPoints(): List<HeartRateDataPoint> {
    return this.flatMap { record ->
        record.samples.mapNotNull { sample ->
            // timeZoneOffset이 null일 수 있으므로 안전하게 처리
            val zoneOffset = record.startZoneOffset ?: record.endZoneOffset ?: return@mapNotNull null
            
            val zonedDateTime = ZonedDateTime.ofInstant(
                sample.time,
                zoneOffset
            )

            HeartRateDataPoint(
                time = zonedDateTime.truncatedTo(ChronoUnit.SECONDS), // 초 단위로 자르기
                beatsPerMinute = sample.beatsPerMinute
            )
        }
    }.sortedBy { it.time }
}