package com.chibaminto.gomiday.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chibaminto.gomiday.notification.scheduler.ExactAlarmScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            ExactAlarmScheduler.scheduleAll(context.applicationContext)
        }
    }
}