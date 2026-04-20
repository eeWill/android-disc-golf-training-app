package com.eewill.discgolftraining.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDiscType(value: DiscType?): String? = value?.name

    @TypeConverter
    fun toDiscType(value: String?): DiscType? =
        value?.let { runCatching { DiscType.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromDiscDataMode(value: DiscDataMode?): String? = value?.name

    @TypeConverter
    fun toDiscDataMode(value: String?): DiscDataMode =
        value?.let { runCatching { DiscDataMode.valueOf(it) }.getOrNull() } ?: DiscDataMode.NONE

    @TypeConverter
    fun fromPuttingResult(value: PuttingResult?): String? = value?.name

    @TypeConverter
    fun toPuttingResult(value: String?): PuttingResult? =
        value?.let { runCatching { PuttingResult.valueOf(it) }.getOrNull() }
}
