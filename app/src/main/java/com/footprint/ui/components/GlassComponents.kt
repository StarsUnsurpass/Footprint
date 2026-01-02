package com.footprint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassMorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadius: Dp = 16.dp, 
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    val surfaceColor = if (isDark) {
        Color(0xFF1C1C1E).copy(alpha = 0.75f) // Telegram Dark surface
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = shape,
                clip = false
            )
            .background(
                color = surfaceColor,
                shape = shape
            )
            .border(
                width = 0.5.dp,
                color = borderColor,
                shape = shape
            )
            .clip(shape)
    ) {
        content()
    }
}

/**
 * 仿 Telegram 风格的列表项容器
 */
@Composable
fun TelegramListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .then(clickableModifier)
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        content()
    }
}
