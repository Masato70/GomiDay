package com.chibaminto.gomiday.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chibaminto.gomiday.data.model.TrashType
import com.chibaminto.gomiday.data.repository.TrashRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// UI State (画面の状態を表すデータクラス)
data class TrashHomeUiState(
    val todayTrash: List<TrashType> = emptyList(),      // 今日のゴミ
    val upcomingTrash: List<TrashScheduleItem> = emptyList(), // 今後のゴミ
    val isLoading: Boolean = true,                       // ローディング中
    val errorMessage: String? = null                     // エラーメッセージ
)

// 今後のゴミスケジュール用データクラス
data class TrashScheduleItem(
    val trashType: TrashType,
    val date: LocalDate,
    val daysUntil: Long
)


@RequiresApi(Build.VERSION_CODES.O)
class TrashHomeViewModel(
    private val repository: TrashRepository
) : ViewModel() {

    // UI Stateを管理 (Composeから監視される)
    private val _uiState = MutableStateFlow(TrashHomeUiState())
    val uiState: StateFlow<TrashHomeUiState> = _uiState.asStateFlow()

    init {
        loadTrashSchedule()
    }

    // データ読み込み
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTrashSchedule() {
        viewModelScope.launch {
            try {
                // データベースの変更を監視
                repository.getAllTrashTypes()
                    .catch { exception ->
                        // エラーハンドリング
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = "データの読み込みに失敗しました: ${exception.message}"
                        )}
                    }
                    .collect { allTrash ->
                        // データが更新されるたびにUIStateを更新
                        updateUiState(allTrash)
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "予期しないエラーが発生しました"
                )}
            }
        }
    }

    // UIStateを更新
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUiState(allTrash: List<TrashType>) {
        val today = LocalDate.now()
        val todayDayOfWeek = today.dayOfWeek.value % 7

        // 今日のゴミをフィルタリング
        val todayTrash = allTrash.filter { trashType ->
            trashType.daysOfWeek.contains(todayDayOfWeek)
        }

        // 今後7日間のスケジュールを作成
        val upcomingSchedule = mutableListOf<TrashScheduleItem>()

        for (daysAhead in 1..7) {
            val targetDate = today.plusDays(daysAhead.toLong())
            val targetDayOfWeek = targetDate.dayOfWeek.value % 7

            allTrash.forEach { trashType ->
                if (trashType.daysOfWeek.contains(targetDayOfWeek)) {
                    upcomingSchedule.add(
                        TrashScheduleItem(
                            trashType = trashType,
                            date = targetDate,
                            daysUntil = daysAhead.toLong()
                        )
                    )
                }
            }
        }

        // UIStateを更新
        _uiState.update {
            TrashHomeUiState(
                todayTrash = todayTrash,
                upcomingTrash = upcomingSchedule.sortedBy { it.daysUntil },
                isLoading = false,
                errorMessage = null
            )
        }
    }

    // 公開メソッド (UIから呼ばれる)
    // データを再読み込み (Pull to Refresh用)
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadTrashSchedule()
    }

    // エラーをクリア
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}


// - Repositoryを引数として受け取り、ViewModelに渡す
class TrashHomeViewModelFactory(
    private val repository: TrashRepository
) : ViewModelProvider.Factory {

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrashHomeViewModel::class.java)) {
            return TrashHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}