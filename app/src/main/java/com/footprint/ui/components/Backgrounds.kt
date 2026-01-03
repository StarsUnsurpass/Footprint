package com.footprint.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.sin

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    val baseColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF0F4FF)
    
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidBackground")
    
    // Animate blob positions
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = Math.PI.toFloat() * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        // Layer 1: Liquid Blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Blob 1: Soft Blue
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.3f) else Color(0xFF93C5FD).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = width * (0.3f + 0.1f * sin(time.toDouble()).toFloat()),
                        y = height * (0.2f + 0.05f * sin(time.toDouble() * 0.7).toFloat())
                    ),
                    radius = width * 0.8f
                )
            )
            
            // Blob 2: Soft Purple
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF581C87).copy(alpha = 0.2f) else Color(0xFFDDD6FE).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = width * (0.7f + 0.15f * sin(time.toDouble() * 0.5).toFloat()),
                        y = height * (0.5f + 0.1f * sin(time.toDouble() * 0.8).toFloat())
                    ),
                    radius = width * 0.7f
                )
            )

            // Blob 3: Soft Pink/Orange
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF7C2D12).copy(alpha = 0.15f) else Color(0xFFFED7AA).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(
                        x = width * (0.4f + 0.2f * sin(time.toDouble() * 0.3).toFloat()),
                        y = height * (0.8f + 0.1f * sin(time.toDouble() * 0.6).toFloat())
                    ),
                    radius = width * 0.9f
                )
            )
        }

        // Layer 2: Subtle Noise/Grain Overlay
        // Layer 3: Content
        content()
    }
}
