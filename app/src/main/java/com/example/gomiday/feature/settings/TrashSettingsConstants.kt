package com.example.gomiday.feature.settings

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        return if (resId != 0) resId else R.drawable.gomi_mark12_sonohoka
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

// アイコン選択用のコンポーネント（改善版）
@Composable
fun SelectableCircle(
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (selected) selectedColor.copy(alpha = 0.15f)
                else Color.LightGray.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 3.dp,
                        color = selectedColor,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// 色選択用のコンポーネント（チェックマーク付き）
@Composable
fun ColorSelector(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(color)))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.Black.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // チェックマークを表示
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "選択中",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .padding(4.dp)
            )
        }
    }
}