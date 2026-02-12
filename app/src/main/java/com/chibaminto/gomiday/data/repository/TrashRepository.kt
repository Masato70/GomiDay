package com.chibaminto.gomiday.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.chibaminto.gomiday.data.dao.NotificationSettingsDao
import com.chibaminto.gomiday.data.dao.TrashTypeDao
import com.chibaminto.gomiday.data.model.NotificationSettings
import com.chibaminto.gomiday.data.model.TrashType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TrashRepository(
    private val trashTypeDao: TrashTypeDao,
    private val notificationSettingsDao: NotificationSettingsDao
) {

    // 📖 全てのゴミタイプを取得
    fun getAllTrashTypes(): Flow<List<TrashType>> {
        return trashTypeDao.getAllTrashTypes()
    }

    // 📖 通知が有効なゴミタイプを取得
    fun getNotifyEnabledTrashTypes(): Flow<List<TrashType>> {
        return trashTypeDao.getNotifyEnabledTrashTypes()
    }

    // 📖 今日のゴミを取得
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTodayTrash(): List<TrashType> {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value % 7
        return trashTypeDao.getTrashTypesByDayOfWeek(dayOfWeek)
    }

    // 📖 特定の曜日のゴミを取得
    suspend fun getTrashTypesByDayOfWeek(dayOfWeek: Int): List<TrashType> {
        return trashTypeDao.getTrashTypesByDayOfWeek(dayOfWeek)
    }

    // 📖 IDで取得
    suspend fun getTrashTypeById(id: Int): TrashType? {
        return trashTypeDao.getTrashTypeById(id)
    }

    // ✏️ 追加
    suspend fun insert(trashType: TrashType): Long {
        return trashTypeDao.insert(trashType)
    }

    // 🔄 更新
    suspend fun update(trashType: TrashType) {
        trashTypeDao.update(trashType)
    }

    // 🗑️ 削除
    suspend fun delete(trashType: TrashType) {
        trashTypeDao.delete(trashType)
    }

    // 🗑️ IDで削除
    suspend fun deleteById(id: Int) {
        trashTypeDao.deleteById(id)
    }

    // 📊 件数取得
    suspend fun getCount(): Int {
        return trashTypeDao.getCount()
    }

    // 🔍 今後1週間のゴミスケジュールを取得
    suspend fun getWeekSchedule(): List<TrashType> {
        var result: List<TrashType> = emptyList()
        getAllTrashTypes().collect { result = it }
        return result
    }


    // 通知設定を監視
    fun getNotificationSettings(): Flow<NotificationSettings?> {
        return notificationSettingsDao.getSettings()
    }

    // 通知設定を一度だけ取得
    suspend fun getNotificationSettingsOnce(): NotificationSettings {
        return notificationSettingsDao.getSettingsOnce() ?: NotificationSettings()
    }

    // 通知設定を保存
    suspend fun saveNotificationSettings(settings: NotificationSettings) {
        notificationSettingsDao.saveSettings(settings)
    }

    // 🔄 全件の notifyEnabled を更新
    suspend fun updateAllNotifyEnabled(enabled: Boolean) {
        trashTypeDao.updateAllNotifyEnabled(enabled)
    }
}