package com.chibaminto.gomiday.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chibaminto.gomiday.data.model.NotificationSettings
import com.chibaminto.gomiday.data.dao.NotificationSettingsDao
import com.chibaminto.gomiday.data.model.TrashType
import com.chibaminto.gomiday.data.dao.TrashTypeDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TrashType::class, NotificationSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trashTypeDao(): TrashTypeDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // シングルトンでデータベースインスタンスを取得
        fun getDatabase(context: Context): AppDatabase {
            //インスタンスが既に存在する場合、それを返し、存在しない場合は新規作成
            //synchronized(this)は、このブロックが同時に実行されることを制御(1スレッドだけ)
            return INSTANCE ?: synchronized(this) {
                // データベースインスタンスを作成
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trash_app_database"
                )
                    // データベースが更新されたときに既存のデータを破棄(アップデートでカラムを追加したり削除したり型を変えてしまうと...)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 初回起動時にサンプルデータを挿入
        private class DatabaseCallback : Callback() {
            //「SupportSQLiteDatabase」RoomのDBファイルが“初めて”作られた瞬間だけ呼ばれる
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(
                            database.trashTypeDao(),
                            database.notificationSettingsDao()
                        )
                    }
                }
            }
        }

        // サンプルデータを挿入する関数
        private suspend fun populateDatabase(
            dao: TrashTypeDao,
            notificationDao: NotificationSettingsDao,
        ) {
            // 初回起動時のサンプルデータ
            val sampleData = listOf(
                TrashType(
                    emoji = "gomi_mark01_moeru",
                    name = "燃えるゴミ",
                    colorHex = "#FF6B6B",
                    daysOfWeek = listOf(0, 6),
                    sortOrder = 1
                ),
                TrashType(
                    emoji = "gomi_mark02_moenai",
                    name = "燃えないゴミ",
                    colorHex = "#95E1D3",
                    daysOfWeek = listOf(6),
                    sortOrder = 3
                ),
                TrashType(
                    emoji = "gomi_mark05_petbottle",
                    name = "ペット,ビン, 缶",
                    colorHex = "#FECA57",
                    daysOfWeek = listOf(0),
                    sortOrder = 4
                )
            )

            dao.insertAll(sampleData)
            notificationDao.insertDefaultSettings()
        }
    }
}