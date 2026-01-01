package com.footprint.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.footprint.data.model.Mood
import com.footprint.ui.state.FootprintUiState
import com.footprint.ui.components.AppBackground
import com.footprint.ui.components.GlassMorphicCard
import com.footprint.ui.components.AboutDialog
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    state: FootprintUiState,
    onSearch: (String) -> Unit,
    onYearShift: (Int) -> Unit,
    onMoodSelected: (Mood?) -> Unit,
    onCreateGoal: () -> Unit,
    onExportTrace: () -> Unit,
    onEditEntry: (com.footprint.data.model.FootprintEntry) -> Unit
) {
    val scrollState = rememberScrollState()
    var query by rememberSaveable { mutableStateOf(state.filterState.searchQuery) }
    val df = remember { DecimalFormat("0.0") }

    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "年度·月度足迹雷达",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Box {
                    androidx.compose.material3.IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("反馈") },
                            onClick = {
                                showMenu = false
                                uriHandler.openUri("https://github.com/StarsUnsurpass/Footprint/issues")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("关于") },
                            onClick = {
                                showMenu = false
                                showAboutDialog = true
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onSearch(it)
                },
                label = { Text("搜索地点/标签/故事") },
                modifier = Modifier.fillMaxWidth()
            )

            // History Trace Card
            GlassMorphicCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExportTrace)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "历史足迹",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "回放你的时空轨迹",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            YearNavigator(
                year = state.filterState.year,
                onBack = { onYearShift(-1) },
                onForward = { onYearShift(1) }
            )

            SummaryRow(
                title = "年度足迹",
                stats = listOf(
                    "总记录" to "${state.summary.yearly.totalEntries} 次",
                    "里程" to "${df.format(state.summary.yearly.totalDistance)} km",
                    "独特地点" to "${state.summary.yearly.uniquePlaces}"
                )
            )
            SummaryRow(
                title = "本月亮点",
                stats = listOf(
                    "记录" to "${state.summary.monthly.totalEntries}",
                    "活力" to (state.summary.monthly.energyAverage.takeIf { it > 0 }?.let { df.format(it) } ?: "-"),
                    "主情绪" to (state.summary.monthly.dominantMood?.label ?: "待发现")
                )
            )

            MoodDistributionSection(mood = state.summary.yearly.dominantMood, onMoodSelected = onMoodSelected)

            RecentFootprintsSection(entries = state.entries.take(3), onCreateGoal = onCreateGoal, onEditEntry = onEditEntry)
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for nav bar
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun YearNavigator(year: Int, onBack: () -> Unit, onForward: () -> Unit) {
    GlassMorphicCard(shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "聚焦年份", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(icon = Icons.Outlined.ArrowBackIosNew, onClick = onBack)
                Text(text = year.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 12.dp))
                IconButton(icon = Icons.AutoMirrored.Outlined.ArrowForwardIos, onClick = onForward)
            }
        }
    }
}

@Composable
private fun IconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
private fun SummaryRow(title: String, stats: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stats.forEachIndexed { index, pair ->
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = pair.first,
                    value = pair.second,
                    accent = when (index) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, label: String, value: String, accent: Color) {
    GlassMorphicCard(
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun MoodDistributionSection(mood: Mood?, onMoodSelected: (Mood?) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "心情雷达", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Mood.entries.forEach { option ->
                FilterChip(
                    selected = mood == option,
                    onClick = { onMoodSelected(option) },
                    label = { Text(option.label) },
                    leadingIcon = {
                        Spacer(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(option.color)
                        )
                    }
                )
            }
            FilterChip(
                selected = mood == null,
                onClick = { onMoodSelected(null) },
                label = { Text("全部") }
            )
        }
    }
}

@Composable
private fun RecentFootprintsSection(
    entries: List<com.footprint.data.model.FootprintEntry>, 
    onCreateGoal: () -> Unit,
    onEditEntry: (com.footprint.data.model.FootprintEntry) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MM-dd") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "最近的灵感", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onCreateGoal) { Text("新的计划") }
        }
        entries.forEach { entry ->
            GlassMorphicCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.clickable { onEditEntry(entry) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = entry.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${entry.location} · ${entry.happenedOn.format(formatter)}", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = entry.detail, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
