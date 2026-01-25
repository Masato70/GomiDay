package com.example.gomiday.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.content.Context
import com.example.gomiday.R

object TrashSettingsConstants {


    val iconList = listOf(
        "gomi_mark01_moeru",
        "gomi_mark02_moenai",
        "gomi_mark04_can",
        "gomi_mark05_petbottle",
        "gomi_mark06_plastic",
        "gomi_mark07_shigen",
        "gomi_mark11_kami",
        "gomi_mark12_sonohoka"
    )

    val defaultIcon = "gomi_mark01_moeru"


    fun getIconResId(context: Context, iconName: String): Int {
        val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.gomi_mark12_sonohoka // フォールバック
    }


    val colorList = listOf(
        "#FF6B6B" to "赤",
        "#4ECDC4" to "青緑",
        "#95E1D3" to "緑",
        "#FECA57" to "黄",
        "#9B59B6" to "紫",
        "#3498DB" to "青",
        "#E67E22" to "橙",
        "#95A5A6" to "灰"
    )

    val daysList = listOf(
        0 to "日",
        1 to "月",
        2 to "火",
        3 to "水",
        4 to "木",
        5 to "金",
        6 to "土"
    )

    val defaultColor = "#FF6B6B"
}

@Composable
fun SelectableCircle(
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) selectedColor else Color.LightGray.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

