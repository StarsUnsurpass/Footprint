package com.footprint.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.footprint.data.model.FootprintSummary
import com.footprint.data.model.TravelGoal
import com.footprint.ui.components.AppBackground
import com.footprint.ui.components.GlassMorphicCard
import java.time.format.DateTimeFormatter

@Composable
fun GoalPlannerScreen(
    modifier: Modifier = Modifier,
    goals: List<TravelGoal>,
    summary: FootprintSummary,
    onToggleGoal: (TravelGoal) -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (TravelGoal) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    
    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "目标驾驶舱", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PlannerCard(modifier = Modifier.weight(1f), title = "年度打卡", value = "${summary.yearly.totalEntries} 次")
                PlannerCard(modifier = Modifier.weight(1f), title = "活跃天数", value = summary.daysActiveThisYear.toString())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PlannerCard(modifier = Modifier.weight(1f), title = "连续记录", value = "${summary.streakDays} 天")
                PlannerCard(modifier = Modifier.weight(1f), title = "本月能量", value = summary.monthly.energyAverage.takeIf { it > 0 }?.toInt()?.toString() ?: "-" )
            }
    
            Button(onClick = onAddGoal) { Text("添加目标") }
    
            goals.forEach { goal ->
                GlassMorphicCard(
                    modifier = Modifier.fillMaxWidth().clickable { onEditGoal(goal) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
                            Checkbox(checked = goal.isCompleted, onCheckedChange = { onToggleGoal(goal) })
                        }
                        Text(text = "目的地：${goal.targetLocation}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "预计：${goal.targetDate.format(formatter)}", style = MaterialTheme.typography.labelMedium)
                        Text(text = goal.notes, style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(
                            progress = { goal.progress.coerceIn(0, 100) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerCard(modifier: Modifier = Modifier, title: String, value: String) {
    GlassMorphicCard(
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = value, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
