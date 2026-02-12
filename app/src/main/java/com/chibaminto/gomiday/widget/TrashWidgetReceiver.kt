package com.chibaminto.gomiday.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class TrashWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TrashWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // カスタムアクションでウィジェット更新
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TrashWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.chibaminto.gomiday.UPDATE_WIDGET"

        /**
         * アプリ内からウィジェットを更新するためのヘルパー
         * 設定変更時などに呼び出す
         */
        fun updateWidget(context: Context) {
            val intent = Intent(context, TrashWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}