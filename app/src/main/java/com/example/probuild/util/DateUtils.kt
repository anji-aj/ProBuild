package com.example.probuild.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

object DateUtils {

    fun getStartOfDay(timestamp: Long): Long {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun getEndOfDay(timestamp: Long): Long {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            .toLocalDate()
            .atTime(LocalTime.MAX)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun getTodayRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }

    fun getYesterdayRange(): Pair<Long, Long> {
        val yesterday = LocalDate.now().minusDays(1)
        val start = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = yesterday.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }

    fun getThisWeekRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }

    fun getThisMonthRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.with(TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }
    
    fun isSameDay(t1: Long, t2: Long): Boolean {
        val d1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(t1), ZoneId.systemDefault()).toLocalDate()
        val d2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(t2), ZoneId.systemDefault()).toLocalDate()
        return d1 == d2
    }
}
