package com.footprint.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.footprint.utils.AIStoryGenerator

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
    val datePickerState = rememberDatePickerState(
        initialEntry?.happenedOn?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    // Get current location for new entries
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
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
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
                        modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                    ) {
                        Text("日期")
                    }
                }
                
                Column {
                    Text(
                        text = "活力指数: ${energy.toInt()}",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = energy,
                        onValueChange = { energy = it },
                        steps = 8,
                        valueRange = 1f..10f
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", color = androidx.compose.material3.MaterialTheme.colorScheme.outline) }
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
                                longitude = initialEntry?.longitude ?: currentLocation?.longitude
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
    val longitude: Double? = null
)