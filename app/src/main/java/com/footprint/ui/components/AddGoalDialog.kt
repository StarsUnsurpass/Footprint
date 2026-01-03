package com.footprint.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    initialGoal: com.footprint.data.model.TravelGoal? = null,
    onDismiss: () -> Unit,
    onSave: (GoalDraft) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var location by remember { mutableStateOf(initialGoal?.targetLocation ?: "") }
    var notes by remember { mutableStateOf(initialGoal?.notes ?: "想要达成的体验…") }
    var selectedIcon by remember { mutableStateOf(initialGoal?.icon ?: "Flag") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialGoal?.targetDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
    )

    val availableIcons = listOf(
        "Flag", "Star", "Favorite", "Explore", "Flight", 
        "Train", "DirectionsBike", "CameraAlt", "Map", "Landscape", 
        "Hotel", "Restaurant", "LocalActivity", "Event", "BeachAccess"
    )

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
    } ?: (initialGoal?.targetDate ?: LocalDate.now().plusWeeks(2))

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassMorphicCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (initialGoal != null) "编辑旅程目标" else "创建旅程目标",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("目标名称") },
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
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                    label = { Text("目的地") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("灵感/资源") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("目标日期：$selectedDate")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.outline) }
                    Button(
                        onClick = {
                            onSave(
                                GoalDraft(
                                    title = title,
                                    location = location,
                                    date = selectedDate,
                                    notes = notes,
                                    icon = selectedIcon
                                )
                            )
                        },
                        enabled = title.isNotBlank() && location.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) { Text("保存目标") }
                }
            }
        }
    }
}

data class GoalDraft(
    val title: String,
    val location: String,
    val date: LocalDate,
    val notes: String,
    val icon: String = "Flag"
)