package com.example.gomiday.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gomiday.core.database.AppDatabase
import com.example.gomiday.data.repository.TrashRepository
import com.example.gomiday.notification.permission.ExactAlarmGate
import com.example.gomiday.notification.scheduler.ExactAlarmScheduler
import com.example.gomiday.notification.notifier.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrashAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TYPE = "type"
        const val TYPE_BEFORE_DAY = "before_day" // 18:00 実行
        const val TYPE_TODAY = "today"           // 06:00 実行
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (!ExactAlarmGate.isExactAlarmAllowed(context)) {
            // 設定がOFFなら通知を出さない
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.Companion.getDatabase(context.applicationContext)
                val repository = TrashRepository(
                    database.trashTypeDao(),
                    database.notificationSettingsDao()
                )
                val settings = repository.getNotificationSettingsOnce()

                when (intent.getStringExtra(EXTRA_TYPE)) {
                    TYPE_BEFORE_DAY -> {
                        if (settings.beforeDayEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                notifyBeforeDay(context, repository)
                            }
                        }
                        // 次回の 18:00 を張り直し
                        ExactAlarmScheduler.scheduleBeforeDay(context)
                    }

                    TYPE_TODAY -> {
                        if (settings.todayEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                notifyToday(context, repository)
                            }
                        }
                        // 次回の 06:00 を張り直し
                        ExactAlarmScheduler.scheduleToday(context)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val type = intent.getStringExtra(EXTRA_TYPE)
                if (type == TYPE_BEFORE_DAY) ExactAlarmScheduler.scheduleBeforeDay(context)
                if (type == TYPE_TODAY) ExactAlarmScheduler.scheduleToday(context)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun notifyBeforeDay(context: Context, repository: TrashRepository) {
        val tomorrow = LocalDate.now().plusDays(1)
        val day = tomorrow.dayOfWeek.value % 7 // 日0 月1 ... 土6

        val trash = repository.getTrashTypesByDayOfWeek(day)
            .filter { it.notifyEnabled }

        if (trash.isNotEmpty()) {
            val names = trash.map { it.name }
            NotificationHelper.showBeforeDayNotification(
                context.applicationContext,
                names,
                notificationId = 1
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun notifyToday(context: Context, repository: TrashRepository) {
        val today = LocalDate.now()
        val day = today.dayOfWeek.value % 7 // 日0 月1 ... 土6

        val trash = repository.getTrashTypesByDayOfWeek(day)
            .filter { it.notifyEnabled }

        if (trash.isNotEmpty()) {
            val names = trash.map { it.name }
            NotificationHelper.showTodayNotification(
                context.applicationContext,
                names,
                notificationId = 2
            )
        }
    }
}