package com.example.gomiday.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gomiday.data.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

//通知の設定を1か所に保存して、取り出して、最初だけ初期値を入れるようなところ
@Dao
interface NotificationSettingsDao {

    @Query("SELECT * FROM notification_settings WHERE id = 1")
    fun getSettings(): Flow<NotificationSettings?>

    @Query("SELECT * FROM notification_settings WHERE id = 1")
    suspend fun getSettingsOnce(): NotificationSettings?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveSettings(settings: NotificationSettings)

    @Query("""
        INSERT OR IGNORE INTO notification_settings 
        (id, beforeDayEnabled, beforeDayHour, beforeDayMinute, todayEnabled, todayHour, todayMinute)
        VALUES (1, 1, 18, 0, 1, 6, 0)
    """)
    suspend fun insertDefaultSettings()
}