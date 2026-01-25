package com.example.gomiday.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gomiday.data.model.TrashType
import com.example.gomiday.data.repository.TrashRepository
import com.example.gomiday.notification.permission.ExactAlarmGate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// UI State (設定画面)
data class TrashSettingsUiState(
    val trashTypes: List<TrashType> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TrashSettingsViewModel(
    private val repository: TrashRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashSettingsUiState())
    val uiState: StateFlow<TrashSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TrashSettingsViewModelFactory.TrashSettingsEvent>()
    val events: SharedFlow<TrashSettingsViewModelFactory.TrashSettingsEvent> = _events.asSharedFlow()

    init {
        loadTrashTypes()
    }

    // データ読み込み
    private fun loadTrashTypes() {
        viewModelScope.launch {
            repository.getAllTrashTypes()
                .catch { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "データの読み込みに失敗しました"
                    )}
                }
                .collect { trashTypes ->
                    _uiState.update { it.copy(
                        trashTypes = trashTypes,
                        isLoading = false
                    )}
                }
        }
    }

    fun syncNotifyEnabled(context: Context) {
        viewModelScope.launch {
            val canNotify = checkCanNotify(context)
            repository.updateAllNotifyEnabled(canNotify)
        }
    }

    //
    private suspend fun maybeNavigateToSettings(context: Context) {
        val postNotificationsGranted = checkPostNotificationsGranted(context)
        val exactAlarmAllowed = ExactAlarmGate.isExactAlarmAllowed(context)

        when {
            !postNotificationsGranted -> {
                _events.emit(TrashSettingsViewModelFactory.TrashSettingsEvent.NavigateToNotificationSettings)
            }
            !exactAlarmAllowed -> {
                _events.emit(TrashSettingsViewModelFactory.TrashSettingsEvent.NavigateToExactAlarmSettings)
            }
            // 両方 OK → 何もしない
        }
    }

    private fun checkCanNotify(context: Context): Boolean {
        val postNotificationsGranted = checkPostNotificationsGranted(context)
        val exactAlarmAllowed = ExactAlarmGate.isExactAlarmAllowed(context)
        return postNotificationsGranted && exactAlarmAllowed
    }

    private fun checkPostNotificationsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }


    // ============================================
    // CRUD操作
    // ============================================

    // 新しいゴミを追加
    fun addTrashType(trashType: TrashType, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                repository.insert(trashType)
                _uiState.update { it.copy(
                    isSaving = false,
                    successMessage = "${trashType.name}を追加しました"
                )}
                maybeNavigateToSettings(context)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSaving = false,
                    errorMessage = "追加に失敗しました"
                )}
            }
        }
    }

    // ゴミを更新
    fun updateTrashType(trashType: TrashType, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                repository.update(trashType)
                _uiState.update { it.copy(
                    isSaving = false,
                    successMessage = "${trashType.name}を更新しました"
                )}
                maybeNavigateToSettings(context)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSaving = false,
                    errorMessage = "更新に失敗しました"
                )}
            }
        }
    }

    // ゴミを削除
    fun deleteTrashType(trashType: TrashType) {
        viewModelScope.launch {
            try {
                repository.delete(trashType)
                _uiState.update { it.copy(
                    successMessage = "${trashType.name}を削除しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "削除に失敗しました"
                )}
            }
        }
    }


    fun clearMessages() {
        _uiState.update { it.copy(
            errorMessage = null,
            successMessage = null
        )}
    }
}

// ViewModelFactory (設定画面用)
class TrashSettingsViewModelFactory(
    private val repository: TrashRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrashSettingsViewModel::class.java)) {
            return TrashSettingsViewModel(repository) as T  // useCase 削除
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    sealed interface TrashSettingsEvent {
        object NavigateToNotificationSettings : TrashSettingsEvent
        object NavigateToExactAlarmSettings : TrashSettingsEvent
    }
}