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
package com.example.healthconnect.codelab.presentation.component

import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.example.healthconnect.codelab.data.toChartDataPoints // ChartMappers.kt의 확장 함수
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.annotation.StringRes
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.metadata.Metadata
import com.example.healthconnect.codelab.R
import com.example.healthconnect.codelab.data.dateTimeWithOffsetOrDefault
import com.example.healthconnect.codelab.presentation.theme.HealthConnectTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


/**
 * Displays a list of [SpeedRecord] data in the [LazyColumn].
 */
fun LazyListScope.speedSeries(
    @StringRes labelId: Int,
    series: List<SpeedRecord>,
) {
  seriesHeading(labelId)
  series.forEach { serie ->
    seriesDateTimeHeading(
      start = serie.startTime,
      startZoneOffset = serie.startZoneOffset,
      end = serie.endTime,
      endZoneOffset = serie.endZoneOffset
    )
    items(serie.samples) { SeriesRow(String.format("%.1f", it.speed.inMetersPerSecond)) }
  }
}

/**
 * Displays a list of [HeartRateRecord] data in the [LazyColumn].
 */
/* fun LazyListScope.heartRateSeries(
    @StringRes labelId: Int,
    series: List<HeartRateRecord>,
) {
  seriesHeading(labelId)
  series.forEach { serie ->
    seriesDateTimeHeading(
      start = serie.startTime,
      startZoneOffset = serie.startZoneOffset,
      end = serie.endTime,
      endZoneOffset = serie.endZoneOffset
    )
    items(serie.samples) { SeriesRow(it.beatsPerMinute.toString()) }
  }
}
*/



@Composable
fun HeartRateChart(
  @StringRes labelId: Int,
  series: List<HeartRateRecord>
) {
  val dataPoints = remember(series) {
    series.toChartDataPoints()
  }

  if (dataPoints.isEmpty()) {
    Text(stringResource(labelId) + ": 데이터 없음", modifier = Modifier.padding(22.dp))
    return
  }

  val heartRateValues: List<Double> = remember(dataPoints) {
    dataPoints.map { it.beatsPerMinute.toDouble() }
  }

  val chartLabel = stringResource(id = R.string.hr_series)
  LineChart(
    modifier = Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 22.dp, vertical = 8.dp),
    data = remember(heartRateValues, labelId) {
      listOf(
        Line(
          label = chartLabel, // 레이블을 설정
          values = heartRateValues,
          color = SolidColor(Color(0xFFE53935)),

           firstGradientFillColor = Color(0xFFE53935).copy(alpha = 0.3f),
           secondGradientFillColor = Color.Transparent,
          strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
          gradientAnimationDelay = 1000,
          drawStyle = DrawStyle.Stroke(width = 2.dp),
        )
      )
    },


     animationMode = AnimationMode.Together(delayBuilder = { it * 100L }),

    // Y축 최소/최대 값 설정 인수가 없다면 제거
    // minYValue = 50f,
    // maxYValue = 200f,

    // gridColor, labelColor 인수가 없다면 제거
  )

  val firstTime = dataPoints.first().time.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
  val lastTime = dataPoints.last().time.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(firstTime, style = MaterialTheme.typography.caption, color = Color.Gray)
    Text(lastTime, style = MaterialTheme.typography.caption, color = Color.Gray)
  }
}

fun LazyListScope.seriesHeading(labelId: Int) {
  item {
    Text(
      text = stringResource(id = labelId),
      style = MaterialTheme.typography.h5,
      color = MaterialTheme.colors.primary
    )
  }
}

fun LazyListScope.seriesDateTimeHeading(
    start: Instant,
    startZoneOffset: ZoneOffset?,
    end: Instant,
    endZoneOffset: ZoneOffset?,
) {
  item {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
    val startTime = dateTimeWithOffsetOrDefault(start, startZoneOffset)
    val endTime = dateTimeWithOffsetOrDefault(end, endZoneOffset)
    val dateLabel = dateFormatter.format(startTime)
    val startLabel = timeFormatter.format(startTime)
    val endLabel = timeFormatter.format(endTime)
    Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp),
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        color = MaterialTheme.colors.secondary,
        text = "$dateLabel: $startLabel - $endLabel",
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun SeriesRow(value: String) {
  Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text("Sample: $value")
  }
}

@Preview
@Composable
fun HeartRateSeriesPreview() {
  HealthConnectTheme {
    LazyColumn {
      val time1 = Instant.now()
      val time2 = time1.minusSeconds(60)
      val mockSeries = listOf(
        HeartRateRecord(
          startTime = time2,
          endTime = time1,
          samples = listOf(
            HeartRateRecord.Sample(
              beatsPerMinute = 103L, // ★ Long 타입 명시적으로 수정
              time = time1
            ),
            HeartRateRecord.Sample(
              beatsPerMinute = 85L, // ★ Long 타입 명시적으로 수정
              time = time2
            )
          ),
          startZoneOffset = ZoneOffset.UTC,
          endZoneOffset = ZoneOffset.UTC,
          // Metadata 생성자도 string을 요구하므로 안전하게 수정
          metadata = Metadata.manualEntry()
        )
      )

      item {
        Text("심박수 차트 미리보기", style = MaterialTheme.typography.subtitle1)
        HeartRateChart(
          labelId = R.string.hr_series,
          series = mockSeries
        )
      }
    }
  }
}