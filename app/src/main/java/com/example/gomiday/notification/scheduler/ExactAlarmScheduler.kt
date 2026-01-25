package com.example.gomiday.notification.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.gomiday.notification.receiver.TrashAlarmReceiver
import java.util.Calendar
import kotlin.math.max

object ExactAlarmScheduler {

    private const val REQ_BEFORE_DAY = 1001
    private const val REQ_TODAY = 1002

    // 18:00 と 06:00 をまとめて開始
    fun scheduleAll(context: Context) {
        scheduleBeforeDay(context)
        scheduleToday(context)
    }

    fun cancelAll(context: Context) {
        cancelBeforeDay(context)
        cancelToday(context)
    }

    fun scheduleBeforeDay(context: Context) {
        scheduleExact(context, hour = 18, minute = 0, requestCode = REQ_BEFORE_DAY, type = TrashAlarmReceiver.Companion.TYPE_BEFORE_DAY)
    }

    fun scheduleToday(context: Context) {
        scheduleExact(context, hour = 6, minute = 0, requestCode = REQ_TODAY, type = TrashAlarmReceiver.Companion.TYPE_TODAY)
    }

    fun cancelBeforeDay(context: Context) {
        cancel(context, REQ_BEFORE_DAY, TrashAlarmReceiver.Companion.TYPE_BEFORE_DAY)
    }

    fun cancelToday(context: Context) {
        cancel(context, REQ_TODAY, TrashAlarmReceiver.Companion.TYPE_TODAY)
    }

    private fun scheduleExact(context: Context, hour: Int, minute: Int, requestCode: Int, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ は「正確なアラーム」が許可されてないと exact が拒否され得る
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val triggerAtMillis = nextTriggerTimeMillis(hour, minute)

        val pendingIntent = buildPendingIntent(context, requestCode, type)

        // 既存があっても上書き（PendingIntent同一なら置き換わる）
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun cancel(context: Context, requestCode: Int, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, requestCode, type)
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun buildPendingIntent(context: Context, requestCode: Int, type: String): PendingIntent {
        val intent = Intent(context, TrashAlarmReceiver::class.java).apply {
            putExtra(TrashAlarmReceiver.Companion.EXTRA_TYPE, type)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE


        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun nextTriggerTimeMillis(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_MONTH, 1)
        }
        // まれに now と同一ミリ秒になって即発火するのを避ける
        return max(cal.timeInMillis, now + 1000L)
    }
}