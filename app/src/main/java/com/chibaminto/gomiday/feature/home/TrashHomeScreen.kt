package com.chibaminto.gomiday.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chibaminto.gomiday.data.model.TrashType
import com.chibaminto.gomiday.feature.settings.TrashSettingsConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

// メイン画面
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashHomeScreen(
    viewModel: TrashHomeViewModel,
    onSettingsClick: () -> Unit = {}
) {
    // ViewModelからUIStateを取得
    val uiState by viewModel.uiState.collectAsState()

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ゴミ出しリマインダー",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "設定")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        // ローディング表示
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // エラー表示
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "エラーが発生しました",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("再読み込み")
                    }
                }
            }
            return@Scaffold
        }

        // メインコンテンツ
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 今日のゴミ (大きく表示)
            if (uiState.todayTrash.isNotEmpty()) {
                items(uiState.todayTrash) { trashType ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    animationSpec = tween(600, easing = EaseOutCubic),
                                    initialOffsetY = { -40 }
                                )
                    ) {
                        TodayTrashCard(trashType)
                    }
                }
            } else {
                // 今日のゴミがない場合
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600))
                    ) {
                        NoTrashTodayCard()
                    }
                }
            }

            // セクションヘッダー
            if (uiState.upcomingTrash.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(800, delayMillis = 200))
                    ) {
                        Text(
                            "今週の予定",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }

                // 今後のゴミ (コンパクト表示)
                items(uiState.upcomingTrash.withIndex().toList()) { (index, scheduleItem) ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = tween(600, delayMillis = 300 + index * 100)
                        ) + slideInVertically(
                            animationSpec = tween(600, delayMillis = 300 + index * 100, easing = EaseOutCubic),
                            initialOffsetY = { 40 }
                        )
                    ) {
                        UpcomingTrashCard(
                            trashType = scheduleItem.trashType,
                            date = scheduleItem.date,
                            daysUntil = scheduleItem.daysUntil
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// 今日のゴミカード
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayTrashCard(trashType: TrashType) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val color = Color(android.graphics.Color.parseColor(trashType.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.12f),
                            color.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ラベル
                Text(
                    "今日のゴミ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // アイコン（中央に大きく）
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = TrashSettingsConstants.getIconResId(context, trashType.emoji)
                        ),
                        contentDescription = trashType.name,
                        modifier = Modifier.size(190.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ゴミの名前
                Text(
                    trashType.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 日付とバッジ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        LocalDate.now().format(
                            DateTimeFormatter.ofPattern("M月d日 (E)", Locale.JAPANESE)
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color
                    ) {
                        Text(
                            "今日",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


// 今日のゴミがない場合のカード
@Composable
fun NoTrashTodayCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF95E1D3).copy(alpha = 0.15f),
                            Color(0xFF95E1D3).copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "✨",
                    fontSize = 48.sp
                )
                Text(
                    "今日はゴミ出しの日ではありません",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "ゆっくり過ごしましょう",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 今後のゴミカード
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingTrashCard(
    trashType: TrashType,
    date: LocalDate,
    daysUntil: Long
) {
    val context = LocalContext.current
    val color = Color(android.graphics.Color.parseColor(trashType.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                // 画像として表示
                Image(
                    painter = painterResource(
                        id = TrashSettingsConstants.getIconResId(context, trashType.emoji)
                    ),
                    contentDescription = trashType.name,
                    modifier = Modifier.size(59.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    trashType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    date.format(
                        DateTimeFormatter.ofPattern("M月d日 (E)", Locale.JAPANESE)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    "${daysUntil}日後",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = color,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}