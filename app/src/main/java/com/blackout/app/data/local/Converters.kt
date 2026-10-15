package com.blackout.app.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromDate(d: LocalDate?): String? = d?.toString()
    @TypeConverter fun toDate(s: String?): LocalDate? = s?.let(LocalDate::parse)

    @TypeConverter fun fromDays(s: Set<DayOfWeek>): String = s.joinToString(",") { it.value.toString() }
    @TypeConverter fun toDays(s: String): Set<DayOfWeek> =
        if (s.isBlank()) emptySet() else s.split(",").map { DayOfWeek.of(it.toInt()) }.toSet()

    @TypeConverter fun fromInts(s: Set<Int>): String = s.joinToString(",")
    @TypeConverter fun toInts(s: String): Set<Int> =
        if (s.isBlank()) emptySet() else s.split(",").map { it.toInt() }.toSet()
}
