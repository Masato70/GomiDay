package com.example.gomiday.notification.notifier

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gomiday.R
import com.example.gomiday.app.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "trash_notification_channel"
    private const val CHANNEL_NAME = "ゴミ出し通知"
    private const val CHANNEL_DESCRIPTION = "ゴミ出しの日を通知します"

    /**
     * 通知チャンネルを作成
     * Android 8.0 (API 26) 以降で必要
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 前日通知を表示
     * 例: "明日は燃えるゴミの日です"
     */
    fun showBeforeDayNotification(
        context: Context,
        trashNames: List<String>,
        notificationId: Int = 1
    ) {
        val title = if (trashNames.size == 1) {
            "明日は${trashNames[0]}の日です"
        } else {
            "明日は${trashNames.joinToString("、")}の日です"
        }

        showNotification(context, title, notificationId)
    }

    /**
     * 当日通知を表示
     * 例: "今日は燃えるゴミの日です"
     */
    fun showTodayNotification(
        context: Context,
        trashNames: List<String>,
        notificationId: Int = 2
    ) {
        val title = if (trashNames.size == 1) {
            "今日は${trashNames[0]}の日です"
        } else {
            "今日は${trashNames.joinToString("、")}の日です"
        }

        showNotification(context, title, notificationId)
    }

    /**
     * 通知を表示
     */
    private fun showNotification(
        context: Context,
        title: String,
        notificationId: Int
    ) {
        // 通知権限をチェック
        if (!hasNotificationPermission(context)) {
            return
        }

        // アプリを開くためのIntent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 通知を構築（テキストなし、タイトルのみ）
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 通知を表示 (権限チェック済み)
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // 権限がない場合は何もしない
            e.printStackTrace()
        }
    }

    /**
     * 通知権限があるかチェック
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}