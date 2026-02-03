package com.example.gomiday.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gomiday.data.model.TrashType
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashTypeDao {

    // 📖 全てのゴミタイプを取得 (sortOrder順)
    // Flow: データベースが変更されると自動的に新しいデータを流す
    @Query("SELECT * FROM trash_types ORDER BY sortOrder ASC, id ASC")
    fun getAllTrashTypes(): Flow<List<TrashType>>

    // 📖 IDで1つのゴミタイプを取得
    @Query("SELECT * FROM trash_types WHERE id = :id")
    suspend fun getTrashTypeById(id: Int): TrashType?

    // 📖 全てのゴミタイプを一度だけ取得（ウィジェット用）
    @Query("SELECT * FROM trash_types ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllTrashTypesOnce(): List<TrashType>


    // 📖 通知が有効なゴミタイプのみ取得
    @Query("SELECT * FROM trash_types WHERE notifyEnabled = 1 ORDER BY sortOrder ASC")
    fun getNotifyEnabledTrashTypes(): Flow<List<TrashType>>

    // 📖 特定の曜日に収集されるゴミを取得
    // 注: daysOfWeekはJSON配列なので、LIKE検索を使用
    @Query("""
        SELECT * FROM trash_types 
        WHERE daysOfWeek LIKE '%[' || :dayOfWeek || ',%'
           OR daysOfWeek LIKE '%,' || :dayOfWeek || ']%'
           OR daysOfWeek LIKE '%,' || :dayOfWeek || ',%'
           OR daysOfWeek = '[' || :dayOfWeek || ']'
    """)
    suspend fun getTrashTypesByDayOfWeek(dayOfWeek: Int): List<TrashType>

    // ✏️ 新しいゴミタイプを追加
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(trashType: TrashType): Long

    // ✏️ 複数のゴミタイプを一度に追加
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(trashTypes: List<TrashType>)

    // 🔄 ゴミタイプを更新
    @Update
    suspend fun update(trashType: TrashType)

    // 🗑️ ゴミタイプを削除
    @Delete
    suspend fun delete(trashType: TrashType)

    // 🗑️ IDで削除
    @Query("DELETE FROM trash_types WHERE id = :id")
    suspend fun deleteById(id: Int)

    // 🗑️ 全て削除 (テスト用)
    @Query("DELETE FROM trash_types")
    suspend fun deleteAll()

    // 📊 件数を取得
    @Query("SELECT COUNT(*) FROM trash_types")
    suspend fun getCount(): Int

    // 🔄 全件の notifyEnabled を更新
    @Query("UPDATE trash_types SET notifyEnabled = :enabled")
    suspend fun updateAllNotifyEnabled(enabled: Boolean)
}