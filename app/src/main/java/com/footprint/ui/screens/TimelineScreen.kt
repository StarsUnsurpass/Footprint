package com.footprint.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.footprint.data.model.FootprintEntry
import com.footprint.data.model.Mood
import com.footprint.ui.state.FilterState
import com.footprint.ui.components.AppBackground
import com.footprint.ui.components.GlassMorphicCard
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    entries: List<com.footprint.data.model.FootprintEntry>,
    filterState: com.footprint.ui.state.FilterState,
    onMoodFilterChange: (Mood?) -> Unit,
    onSearch: (String) -> Unit,
    onEditEntry: (com.footprint.data.model.FootprintEntry) -> Unit
) {
    val grouped = entries.groupBy { it.happenedOn.withDayOfMonth(1) }
        .toSortedMap(compareByDescending { it })
    val formatter = DateTimeFormatter.ofPattern("M月d日")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val headerFormatter = DateTimeFormatter.ofPattern("yyyy年 MM月")

    AppBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 仿 Telegram 沉浸式标题
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp).statusBarsPadding()) {
                    Text(
                        text = "足迹流",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${entries.size} 条记录已同步",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                grouped.forEach { (month, items) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = month.format(headerFormatter),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    items(items) { entry ->
                        TelegramEntryItem(
                            entry = entry, 
                            dateFormatter = formatter, 
                            timeFormatter = timeFormatter,
                            onClick = { onEditEntry(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelegramEntryItem(
    entry: FootprintEntry, 
    dateFormatter: DateTimeFormatter, 
    timeFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 仿 Telegram 圆形头像/图标
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(entry.mood.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.title.take(1),
                style = MaterialTheme.typography.titleLarge,
                color = entry.mood.color,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.happenedOn.format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Text(
                text = entry.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = entry.detail,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 底部元数据
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", entry.distanceKm)} km",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (entry.tags.isNotEmpty()) {
                    Text(
                        text = entry.tags.joinToString(" ") { "#$it" },
                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}