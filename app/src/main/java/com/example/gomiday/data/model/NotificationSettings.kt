package com.example.gomiday.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettings(
    @PrimaryKey
    val id: Int = 1,

    val beforeDayEnabled: Boolean = true,
    val beforeDayHour: Int = 20,
    val beforeDayMinute: Int = 0,

    val todayEnabled: Boolean = true,
    val todayHour: Int = 7,
    val todayMinute: Int = 0
) {
    fun getBeforeDayTimeString(): String {
        return String.format("%02d:%02d", beforeDayHour, beforeDayMinute)
    }

    fun getTodayTimeString(): String {
        return String.format("%02d:%02d", todayHour, todayMinute)
    }
}