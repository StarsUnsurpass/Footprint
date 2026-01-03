package com.footprint.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SwipeState {
    Settled, Revealed
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableItem(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val actionWidth = 160.dp // width for two larger buttons
    val actionWidthPx = with(density) { actionWidth.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = SwipeState.Settled,
            anchors = DraggableAnchors {
                SwipeState.Settled at 0f
                SwipeState.Revealed at -actionWidthPx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Match height of content
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
    ) {
        // Background Actions: This Row fills the entire Box area
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionWidth)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit Action (Green Block)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF43A047)) // Rich Green
                    .clickable { 
                        scope.launch { state.animateTo(SwipeState.Settled) }
                        onEdit()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    Text("编辑", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
            
            // Delete Action (Red Block)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFE53935)) // Rich Red
                    .clickable { 
                        scope.launch { state.animateTo(SwipeState.Settled) }
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    Text("删除", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        // Foreground Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = state.requireOffset().roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(state, Orientation.Horizontal)
                .background(MaterialTheme.colorScheme.surface) // Covers the colored blocks when settled
        ) {
            content()
        }
    }
}