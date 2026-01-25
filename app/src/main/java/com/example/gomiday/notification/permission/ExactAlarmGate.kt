package com.example.gomiday.notification.permission

import android.app.AlarmManager
import android.content.Context
import android.os.Build

object ExactAlarmGate {
    fun isExactAlarmAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }
}