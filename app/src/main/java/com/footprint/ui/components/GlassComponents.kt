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
    shape: Shape = RoundedCornerShape(24.dp), // Slightly more rounded for "liquid" look
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    val surfaceColor = if (isDark) {
        Color(0xFF1C1C1E).copy(alpha = 0.65f)
    } else {
        Color.White.copy(alpha = 0.75f)
    }

    // Refraction border gradient
    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                Color.White.copy(alpha = 0.2f),
                Color.White.copy(alpha = 0.6f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 12.dp else 8.dp,
                shape = shape,
                spotColor = if (isDark) Color.Black else Color.Gray.copy(alpha = 0.3f),
                ambientColor = if (isDark) Color.Black else Color.Gray.copy(alpha = 0.2f)
            )
            .background(
                color = surfaceColor,
                shape = shape
            )
            .border(
                width = 1.dp, // Thicker border for refraction effect
                brush = borderBrush,
                shape = shape
            )
            .clip(shape)
    ) {
        // Inner highlight for extra "liquid" depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 0.5.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f),
                    shape = shape
                )
        )
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