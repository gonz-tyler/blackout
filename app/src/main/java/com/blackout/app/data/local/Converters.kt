package com.blackout.app.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromDate(d: LocalDate?): String? = d?.toString()
    @TypeConverter fun toDate(s: String?): LocalDate? = s?.let(LocalDate::parse)
}