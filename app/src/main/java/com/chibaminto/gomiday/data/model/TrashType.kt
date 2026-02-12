package com.chibaminto.gomiday.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.chibaminto.gomiday.core.database.Converters
import java.time.LocalDate

@Entity(tableName = "trash_types")
@TypeConverters(Converters::class)
data class TrashType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // 自動生成されるID

    val emoji: String,                   // アイコンのリソース名
    val name: String,                    // ゴミの名前 (例: "燃えるゴミ")
    val colorHex: String,                // 色 (例: "#FF6B6B")
    val daysOfWeek: List<Int>,          // 収集曜日 (0=日, 1=月, ..., 6=土)
    val notifyEnabled: Boolean = true,   // 通知ON/OFF
    val notifyTimeBefore: String = "20:00", // 前日通知時刻
    val notifyTimeOn: String = "06:00",     // 当日通知時刻
    val createdAt: Long = System.currentTimeMillis(), // 作成日時
    val sortOrder: Int = 0              // 表示順序
) {
    // 次の収集日を計算するヘルパー関数
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextCollectionDate(): LocalDate {
        val today = LocalDate.now()
        val todayDayOfWeek = today.dayOfWeek.value % 7 // 日曜を0にする

        // 今日以降で最も近い収集日を探す
        val sortedDays = daysOfWeek.sorted()

        for (day in sortedDays) {
            if (day >= todayDayOfWeek) {
                return today.plusDays((day - todayDayOfWeek).toLong())
            }
        }

        // 来週の最初の収集日
        val firstDay = sortedDays.firstOrNull() ?: return today
        return today.plusDays((7 - todayDayOfWeek + firstDay).toLong())
    }

    // 今日が収集日かどうか
    @RequiresApi(Build.VERSION_CODES.O)
    fun isTodayCollectionDay(): Boolean {
        val today = LocalDate.now()
        val todayDayOfWeek = today.dayOfWeek.value % 7
        return daysOfWeek.contains(todayDayOfWeek)
    }
}