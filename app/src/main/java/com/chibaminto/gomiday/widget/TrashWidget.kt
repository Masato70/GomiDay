package com.chibaminto.gomiday.widget

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.chibaminto.gomiday.R
import com.chibaminto.gomiday.app.MainActivity
import com.chibaminto.gomiday.core.database.AppDatabase
import com.chibaminto.gomiday.feature.settings.TrashSettingsConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ウィジェット用のデータクラス
 */
data class TrashItemData(
    val name: String,
    val iconResId: Int,
    val colorHex: String
)

/**
 * 更新ボタン用のActionCallback
 */
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        TrashWidget().update(context, glanceId)
    }
}

class TrashWidget : GlanceAppWidget() {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todayTrash = getTrashForDate(context, LocalDate.now())

        provideContent {
            GlanceTheme {
                WidgetContent(todayTrash)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getTrashForDate(context: Context, date: LocalDate): List<TrashItemData> {
        return try {
            val db = AppDatabase.getDatabase(context)
            val allTrash = db.trashTypeDao().getAllTrashTypesOnce()

            val dayOfWeek = date.dayOfWeek.value % 7

            allTrash
                .filter { trashType -> trashType.daysOfWeek.contains(dayOfWeek) }
                .map { trashType ->
                    TrashItemData(
                        name = trashType.name,
                        iconResId = TrashSettingsConstants.getIconResId(context, trashType.emoji),
                        colorHex = trashType.colorHex
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun WidgetContent(todayTrash: List<TrashItemData>) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(16.dp)
                .background(Color.White)
                .clickable(actionStartActivity<MainActivity>())
                .padding(8.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ヘッダー
                WidgetHeader()

                Spacer(modifier = GlanceModifier.height(4.dp))

                // 今日のゴミ表示
                if (todayTrash.isEmpty()) {
                    NoTrashContent()
                } else {
                    TrashContent(todayTrash)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun WidgetHeader() {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 日付
            Text(
                text = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPANESE)
                ),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            // 更新ボタン
            Box(
                modifier = GlanceModifier
                    .size(26.dp)
                    .cornerRadius(13.dp)
                    .background(Color(0xFFF0F0F0))
                    .clickable(actionRunCallback<RefreshAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "更新",
                    modifier = GlanceModifier.size(14.dp)
                )
            }
        }
    }

    @Composable
    private fun NoTrashContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✨",
                style = TextStyle(fontSize = 40.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "ゴミ出しなし",
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.secondary
                )
            )
        }
    }

    @Composable
    private fun TrashContent(todayTrash: List<TrashItemData>) {
        if (todayTrash.size == 1) {
            SingleTrashDisplay(todayTrash[0])
        } else {
            MultipleTrashDisplay(todayTrash)
        }
    }

    @Composable
    private fun SingleTrashDisplay(trashItem: TrashItemData) {
        val color = try {
            Color(android.graphics.Color.parseColor(trashItem.colorHex))
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 大きなアイコン
            Box(
                modifier = GlanceModifier
                    .size(134.dp)
                    .cornerRadius(100.dp)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(trashItem.iconResId),
                    contentDescription = trashItem.name,
                    modifier = GlanceModifier.size(130.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // ゴミの名前
            Text(
                text = trashItem.name,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }

    @Composable
    private fun MultipleTrashDisplay(todayTrash: List<TrashItemData>) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // アイコンを横並び（最大3つ）
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                todayTrash.take(3).forEachIndexed { index, trashItem ->
                    if (index > 0) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                    }
                    TrashIcon(trashItem)
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // ゴミの名前を横並び
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                todayTrash.take(3).forEachIndexed { index, trashItem ->
                    if (index > 0) {
                        Text(
                            text = " / ",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = GlanceTheme.colors.secondary
                            )
                        )
                    }
                    Text(
                        text = trashItem.name,
                        style = TextStyle(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                }
            }

            // 3つ以上ある場合
            if (todayTrash.size > 3) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "他${todayTrash.size - 3}件",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.secondary
                    )
                )
            }
        }
    }

    @Composable
    private fun TrashIcon(trashItem: TrashItemData) {
        val color = try {
            Color(android.graphics.Color.parseColor(trashItem.colorHex))
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }

        Box(
            modifier = GlanceModifier
                .size(52.dp)
                .cornerRadius(26.dp)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(trashItem.iconResId),
                contentDescription = trashItem.name,
                modifier = GlanceModifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}