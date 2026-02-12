package com.chibaminto.gomiday.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.chibaminto.gomiday.feature.home.TrashHomeViewModel
import com.chibaminto.gomiday.feature.settings.TrashSettingsViewModel
import androidx.compose.runtime.getValue
import com.chibaminto.gomiday.feature.home.TrashHomeScreen
import com.chibaminto.gomiday.feature.settings.TrashSettingsScreen

// ナビゲーション管理
enum class Screen {
    Home,
    Settings
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrashAppNavigation(
    homeViewModel: TrashHomeViewModel,
    settingsViewModel: TrashSettingsViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    when (currentScreen) {
        Screen.Home -> {
            TrashHomeScreen(
                viewModel = homeViewModel,
                onSettingsClick = {
                    currentScreen = Screen.Settings
                }
            )
        }
        Screen.Settings -> {
            TrashSettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = {
                    currentScreen = Screen.Home
                }
            )
        }
    }
}