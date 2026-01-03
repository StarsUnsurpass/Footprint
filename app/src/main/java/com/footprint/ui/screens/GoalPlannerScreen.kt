package com.footprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footprint.data.model.FootprintSummary
import com.footprint.data.model.TravelGoal
import com.footprint.ui.components.AppBackground
import com.footprint.ui.components.GlassMorphicCard
import java.time.format.DateTimeFormatter

import com.footprint.ui.components.SwipeableItem

@Composable
fun GoalPlannerScreen(
    modifier: Modifier = Modifier,
    goals: List<TravelGoal>,
    summary: FootprintSummary,
    onToggleGoal: (TravelGoal) -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (TravelGoal) -> Unit,
    onDeleteGoal: (TravelGoal) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    
    AppBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 仿 Telegram 沉浸式顶部
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "计划与目标",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "完成进度: ${goals.count { it.isCompleted }}/${goals.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    SmallFloatingActionButton(
                        onClick = onAddGoal,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 概览摘要卡片
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            PlannerStatItem("年度记录", "${summary.yearly.totalEntries}", Icons.Default.QueryStats)
                            PlannerStatItem("活跃天数", "${summary.daysActiveThisYear}", Icons.Default.Flag)
                            PlannerStatItem("连续天数", "${summary.streakDays}", Icons.Default.Check)
                        }
                    }
                }

                item {
                    Text(
                        "进行中",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                items(goals) { goal ->
                    SwipeableItem(
                        onEdit = { onEditGoal(goal) },
                        onDelete = { onDeleteGoal(goal) }
                    ) {
                        TelegramGoalItem(
                            goal = goal,
                            formatter = formatter,
                            onToggle = { onToggleGoal(goal) },
                            onEdit = { onEditGoal(goal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TelegramGoalItem(
    goal: TravelGoal,
    formatter: DateTimeFormatter,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 自定义 Checkbox 效果
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = if (goal.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = if (goal.isCompleted) null else androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(24.dp)
            ) {
                if (goal.isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "目的地: ${goal.targetLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "预计: ${goal.targetDate.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (goal.notes.isNotBlank()) {
                    Text(
                        text = goal.notes,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // 进度条
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { goal.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
