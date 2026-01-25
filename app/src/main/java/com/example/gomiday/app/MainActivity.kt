package com.example.gomiday.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.gomiday.core.database.AppDatabase
import com.example.gomiday.data.repository.TrashRepository
import com.example.gomiday.notification.scheduler.ExactAlarmScheduler
import com.example.gomiday.notification.notifier.NotificationHelper
import com.example.gomiday.ui.TrashAppNavigation
import com.example.gomiday.ui.theme.GomiDayTheme
import com.example.gomiday.feature.home.TrashHomeViewModel
import com.example.gomiday.feature.home.TrashHomeViewModelFactory
import com.example.gomiday.feature.settings.TrashSettingsViewModel
import com.example.gomiday.feature.settings.TrashSettingsViewModelFactory

class MainActivity : ComponentActivity() {

    // ViewModelをActivityに持たせる(Activityが破棄されるまで 同じViewModelが使い回される)
    private val homeViewModel: TrashHomeViewModel by viewModels {
        val db = AppDatabase.Companion.getDatabase(applicationContext)
        val repo = TrashRepository(db.trashTypeDao(), db.notificationSettingsDao())
        TrashHomeViewModelFactory(repo)
    }

    private val settingsViewModel: TrashSettingsViewModel by viewModels {
        val db = AppDatabase.Companion.getDatabase(applicationContext)
        val repo = TrashRepository(db.trashTypeDao(), db.notificationSettingsDao())
        TrashSettingsViewModelFactory(
            repository = repo,
        )
    }

    // Android 13+ の通知権限リクエスト
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // 許可されたらスケジュール開始（許可されなければ通知は出ない）
            if (isGranted) {
                ExactAlarmScheduler.scheduleAll(this)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 通知ャンネル作成※1回でOK
        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                ExactAlarmScheduler.scheduleAll(this)
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // 3) Android 12以下は権限いらないので即 scheduleAll
            ExactAlarmScheduler.scheduleAll(this)
        }

        setContent {
            GomiDayTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrashAppNavigation(
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}