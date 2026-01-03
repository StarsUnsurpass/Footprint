package com.footprint.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.footprint.data.model.Mood
import com.footprint.service.LocationTrackingService
import com.footprint.utils.AIStoryGenerator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddFootprintDialog(
    initialEntry: com.footprint.data.model.FootprintEntry? = null,
    onDismiss: () -> Unit,
    onSave: (FootprintDraft) -> Unit
) {
    var title by remember { mutableStateOf(initialEntry?.title ?: "") }
    var location by remember { mutableStateOf(initialEntry?.location ?: "") }
    var detail by remember { mutableStateOf(initialEntry?.detail ?: "") }
    var tags by remember { mutableStateOf(initialEntry?.tags?.joinToString(",") ?: "") }
    var distance by remember { mutableStateOf(initialEntry?.distanceKm?.toString() ?: "5") }
    var energy by remember { mutableStateOf(initialEntry?.energyLevel?.toFloat() ?: 6f) }
    var mood by remember { mutableStateOf(initialEntry?.mood ?: Mood.EXCITED) }
    var selectedIcon by remember { mutableStateOf(initialEntry?.icon ?: "LocationOn") }
    val datePickerState = rememberDatePickerState(
        initialEntry?.happenedOn?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    val availableIcons = listOf(
        "LocationOn", "Restaurant", "LocalCafe", "Park", "Flight", 
        "Train", "DirectionsBike", "ShoppingBag", "CameraAlt", "MusicNote", 
        "Movie", "DirectionsRun", "Pets", "School", "Work"
    )

    val currentLocation by LocationTrackingService.currentLocation.collectAsState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassMorphicCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (initialEntry != null) "编辑足迹" else "添加新的足迹", 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Icon Picker
                Column {
                    Text("选择图标", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availableIcons) { iconName ->
                            val isSelected = selectedIcon == iconName
                            val icon = IconUtils.getIconByName(iconName)
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedIcon = iconName }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        icon, 
                                        null, 
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("故事和感受") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (location.isNotBlank()) {
                                detail = AIStoryGenerator.generateStory(location, mood, selectedDate)
                            }
                        }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Generate", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签，用逗号分隔") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Mood.entries.forEach { option ->
                        FilterChip(
                            selected = mood == option,
                            onClick = { mood = option },
                            label = { Text(option.label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("里程 (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("日期")
                    }
                }
                
                Column {
                    Text(
                        text = "活力指数: ${energy.toInt()}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = energy,
                        onValueChange = { energy = it },
                        steps = 8,
                        valueRange = 1f..10f
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.outline) }
                    Button(
                        onClick = {
                            val payload = FootprintDraft(
                                title = title,
                                location = location,
                                detail = detail,
                                mood = mood,
                                tags = tags.split(',', '，').mapNotNull { it.trim().takeIf(String::isNotEmpty) },
                                distance = distance.toDoubleOrNull() ?: 0.0,
                                energy = energy.toInt().coerceIn(1, 10),
                                date = selectedDate,
                                latitude = initialEntry?.latitude ?: currentLocation?.latitude,
                                longitude = initialEntry?.longitude ?: currentLocation?.longitude,
                                icon = selectedIcon
                            )
                            onSave(payload)
                        },
                        enabled = title.isNotBlank() && location.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

data class FootprintDraft(
    val title: String,
    val location: String,
    val detail: String,
    val mood: Mood,
    val tags: List<String>,
    val distance: Double,
    val energy: Int,
    val date: LocalDate,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val icon: String = "LocationOn"
)
