package com.example.gomiday.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.gomiday.data.model.TrashType
import com.example.gomiday.notification.permission.ExactAlarmGate
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.gomiday.feature.settings.TrashSettingsConstants.colorList
import com.example.gomiday.feature.settings.TrashSettingsConstants.daysList
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.delay


// 設定画面メイン
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashSettingsScreen(
    viewModel: TrashSettingsViewModel,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTrash by remember { mutableStateOf<TrashType?>(null) }
    var showDeleteDialog by remember { mutableStateOf<TrashType?>(null) }

    BackHandler {
        onBackClick()
    }

    // 成功メッセージ表示
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(2000)
            viewModel.clearMessages()
        }
    }

    // onResume で権限を同期
    LifecycleResumeEffect(Unit) {
        viewModel.syncNotifyEnabled(context)
        onPauseOrDispose { }
    }

// イベントを受け取って設定画面へ遷移
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                TrashSettingsViewModelFactory.TrashSettingsEvent.NavigateToNotificationSettings -> {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
                TrashSettingsViewModelFactory.TrashSettingsEvent.NavigateToExactAlarmSettings -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ゴミの設定") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "追加")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ローディング表示
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // エラー表示
            if (uiState.errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("閉じる")
                        }
                    }
                ) {
                    Text(uiState.errorMessage ?: "")
                }
            }

            // 成功メッセージ表示
            if (uiState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text(uiState.successMessage ?: "", color = Color.White)
                }
            }

            // メインコンテンツ
            if (uiState.trashTypes.isEmpty() && !uiState.isLoading) {
                // 空の状態
                EmptyTrashListView { showAddDialog = true }
            } else {
                // ゴミリスト + テストボタン
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trashTypes) { trash ->
                        TrashSettingsCard(
                            trashType = trash,
                            onEdit = { editingTrash = it },
                            onDelete = { showDeleteDialog = it }
                        )
                    }
                }
            }
        }
    }

    // 追加ダイアログ
    if (showAddDialog) {
        TrashEditDialog(
            trashType = null,
            onDismiss = { showAddDialog = false },
            onSave = { newTrash ->
                viewModel.addTrashType(newTrash, context)
                showAddDialog = false
            }
        )
    }

    // 編集ダイアログ
    if (editingTrash != null) {
        TrashEditDialog(
            trashType = editingTrash,
            onDismiss = { editingTrash = null },
            onSave = { updatedTrash ->
                viewModel.updateTrashType(updatedTrash, context)
                editingTrash = null
            }
        )
    }

    // 削除確認ダイアログ
    if (showDeleteDialog != null) {
        DeleteConfirmDialog(
            trashType = showDeleteDialog!!,
            onConfirm = {
                viewModel.deleteTrashType(showDeleteDialog!!)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}


// ゴミ設定カード
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrashSettingsCard(
    trashType: TrashType,
    onEdit: (TrashType) -> Unit,
    onDelete: (TrashType) -> Unit,
) {
    val color = Color(android.graphics.Color.parseColor(trashType.colorHex))
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(trashType) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アイコン
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = TrashSettingsConstants.getIconResId(context, trashType.emoji)
                    ),
                    contentDescription = trashType.name,
                    modifier = Modifier.size(48.dp)
                )
            }

            // 情報
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    trashType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    getDaysOfWeekText(trashType.daysOfWeek),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 削除ボタン
            IconButton(onClick = { onDelete(trashType) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// 空の状態表示
@Composable
fun EmptyTrashListView(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "🗑️",
                fontSize = 64.sp
            )
            Text(
                "ゴミの種類が登録されていません",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "右下のボタンから追加してください",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ゴミを追加")
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashEditDialog(
    trashType: TrashType?,
    onDismiss: () -> Unit,
    onSave: (TrashType) -> Unit,
) {
    var name by remember { mutableStateOf(trashType?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(trashType?.emoji ?: TrashSettingsConstants.defaultIcon) }
    var selectedColor by remember { mutableStateOf(trashType?.colorHex ?: TrashSettingsConstants.defaultColor) }
    var selectedDays by remember { mutableStateOf(trashType?.daysOfWeek ?: emptyList()) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // タイトル
                Text(
                    text = if (trashType == null) "新しいゴミを追加" else "ゴミを編集",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // 名前入力
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ゴミの名前") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // アイコン選択（グリッド表示・大きめサイズ）
                Column {
                    Text(
                        "アイコンを選ぶ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val iconChunks = TrashSettingsConstants.iconList.chunked(4)
                    iconChunks.forEach { rowIcons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowIcons.forEach { iconName ->
                                SelectableCircle(
                                    selected = selectedIcon == iconName,
                                    onClick = { selectedIcon = iconName },
                                    selectedColor = Color(
                                        android.graphics.Color.parseColor(selectedColor)
                                    ),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = TrashSettingsConstants.getIconResId(
                                                context,
                                                iconName
                                            )
                                        ),
                                        contentDescription = iconName,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }
                            repeat(4 - rowIcons.size) {
                                Spacer(modifier = Modifier.size(68.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // 色選択（チェックマーク付き）
                Column {
                    Text(
                        "色を選ぶ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorList.take(4).forEach { (hex, _) ->
                            ColorSelector(
                                color = hex,
                                isSelected = selectedColor == hex,
                                onClick = { selectedColor = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorList.drop(4).forEach { (hex, _) ->
                            ColorSelector(
                                color = hex,
                                isSelected = selectedColor == hex,
                                onClick = { selectedColor = hex }
                            )
                        }
                    }
                }

                // 曜日選択（2列表示）
                Column {
                    Text(
                        "収集日を選ぶ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1行目: 日〜水（4つ）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        daysList.take(4).forEach { (dayNum, dayName) ->
                            val isSelected = selectedDays.contains(dayNum)
                            SelectableCircle(
                                selected = isSelected,
                                onClick = {
                                    selectedDays =
                                        if (isSelected) selectedDays - dayNum else selectedDays + dayNum
                                },
                                selectedColor = Color(
                                    android.graphics.Color.parseColor(
                                        selectedColor
                                    )
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text(
                                    dayName,
                                    color = if (isSelected) Color(
                                        android.graphics.Color.parseColor(selectedColor)
                                    ) else Color.Black.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2行目: 木〜土（3つ）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        daysList.drop(4).forEach { (dayNum, dayName) ->
                            val isSelected = selectedDays.contains(dayNum)
                            SelectableCircle(
                                selected = isSelected,
                                onClick = {
                                    selectedDays =
                                        if (isSelected) selectedDays - dayNum else selectedDays + dayNum
                                },
                                selectedColor = Color(
                                    android.graphics.Color.parseColor(
                                        selectedColor
                                    )
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text(
                                    dayName,
                                    color = if (isSelected) Color(
                                        android.graphics.Color.parseColor(selectedColor)
                                    ) else Color.Black.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        // 3つしかないので1つ分スペーサーを追加してバランスを取る
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("キャンセル", maxLines = 1)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && selectedDays.isNotEmpty()) {

                                val postNotificationsGranted =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                    } else {
                                        true
                                    }

                                val exactAlarmAllowed = ExactAlarmGate.isExactAlarmAllowed(context)

                                val canNotify = postNotificationsGranted && exactAlarmAllowed

                                onSave(
                                    TrashType(
                                        id = trashType?.id ?: 0,
                                        emoji = selectedIcon,
                                        name = name,
                                        colorHex = selectedColor,
                                        daysOfWeek = selectedDays.sorted(),
                                        notifyEnabled = canNotify,
                                        sortOrder = trashType?.sortOrder ?: 0
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && selectedDays.isNotEmpty()
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

// 削除確認ダイアログ
@Composable
fun DeleteConfirmDialog(
    trashType: TrashType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("削除の確認") },
        text = { Text("「${trashType.name}」を削除してもよろしいですか?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("削除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// ヘルパー関数

fun getDaysOfWeekText(daysOfWeek: List<Int>): String {
    val dayMap = daysList.toMap()
    return daysOfWeek.sorted().joinToString("・") { dayMap[it] ?: "" } + "曜日"
}