package com.footprint.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import com.footprint.ui.theme.ThemeMode
import com.footprint.ui.components.AppBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentThemeStyle: com.footprint.ui.theme.AppThemeStyle,
    currentNickname: String,
    currentAvatarId: String,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemeStyleChange: (com.footprint.ui.theme.AppThemeStyle) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onUpdateAvatar: (Uri) -> Unit,
    onExportData: (Uri) -> Unit,
    onImportData: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // File Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri -> uri?.let { onExportData(it) } }
    )
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { onImportData(it) } }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onUpdateAvatar(it) } }
    )

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("设置", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 个人资料
                item {
                    SettingsSectionTitle("数字身份")
                }
                item {
                    ProfileEditor(
                        nickname = currentNickname,
                        avatarId = currentAvatarId,
                        onUpdate = onUpdateProfile,
                        onPickImage = {
                             imagePickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                // 外观
                item {
                    SettingsSectionTitle("外观定制")
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("系统模式", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        ThemeModeSelector(
                            selectedMode = currentThemeMode,
                            onModeSelected = onThemeModeChange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("视觉风格", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        ThemeStyleSelector(
                            selectedStyle = currentThemeStyle,
                            onStyleSelected = onThemeStyleChange
                        )
                    }
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                // 数据管理 (Backup & Restore)
                item {
                    SettingsSectionTitle("数据管理")
                }
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Column {
                            SettingsActionItem(
                                title = "导出足迹备份",
                                subtitle = "将所有数据保存为 JSON 文件",
                                icon = Icons.Default.CloudUpload,
                                onClick = { exportLauncher.launch("footprint_backup_${System.currentTimeMillis()}.json") }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            SettingsActionItem(
                                title = "导入历史记录",
                                subtitle = "从备份文件恢复足迹和目标",
                                icon = Icons.Default.CloudDownload,
                                onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) }
                            )
                        }
                    }
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

                // 关于
                item {
                    SettingsSectionTitle("关于应用")
                }
                item {
                    ListItem(
                        headlineContent = { Text("版本信息") },
                        supportingContent = { Text("v1.4.0") },
                        leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            ThemeOption(
                title = "跟随系统",
                icon = Icons.Default.BrightnessAuto,
                selected = selectedMode == ThemeMode.SYSTEM,
                onClick = { onModeSelected(ThemeMode.SYSTEM) }
            )
            ThemeOption(
                title = "日间模式",
                icon = Icons.Default.BrightnessLow,
                selected = selectedMode == ThemeMode.LIGHT,
                onClick = { onModeSelected(ThemeMode.LIGHT) }
            )
            ThemeOption(
                title = "夜间模式",
                icon = Icons.Default.Brightness4,
                selected = selectedMode == ThemeMode.DARK,
                onClick = { onModeSelected(ThemeMode.DARK) }
            )
        }
    }
}

@Composable
fun ThemeStyleSelector(
    selectedStyle: com.footprint.ui.theme.AppThemeStyle,
    onStyleSelected: (com.footprint.ui.theme.AppThemeStyle) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            ThemeStyleOption(
                title = "智能自适应 (随主情绪变化)",
                icon = Icons.Default.AutoAwesome,
                selected = selectedStyle == com.footprint.ui.theme.AppThemeStyle.AUTO,
                onClick = { onStyleSelected(com.footprint.ui.theme.AppThemeStyle.AUTO) }
            )
            ThemeStyleOption(
                title = "经典蓝调",
                icon = Icons.Default.Palette,
                selected = selectedStyle == com.footprint.ui.theme.AppThemeStyle.CLASSIC,
                onClick = { onStyleSelected(com.footprint.ui.theme.AppThemeStyle.CLASSIC) }
            )
            ThemeStyleOption(
                title = "赛博朋克",
                icon = Icons.Default.ElectricBolt,
                selected = selectedStyle == com.footprint.ui.theme.AppThemeStyle.CYBERPUNK,
                onClick = { onStyleSelected(com.footprint.ui.theme.AppThemeStyle.CYBERPUNK) }
            )
            ThemeStyleOption(
                title = "森林氧吧",
                icon = Icons.Default.Forest,
                selected = selectedStyle == com.footprint.ui.theme.AppThemeStyle.FOREST,
                onClick = { onStyleSelected(com.footprint.ui.theme.AppThemeStyle.FOREST) }
            )
            ThemeStyleOption(
                title = "撒哈拉之光",
                icon = Icons.Default.WbSunny,
                selected = selectedStyle == com.footprint.ui.theme.AppThemeStyle.SAHARA,
                onClick = { onStyleSelected(com.footprint.ui.theme.AppThemeStyle.SAHARA) }
            )
        }
    }
}

@Composable
fun ThemeStyleOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier.size(20.dp),
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ProfileEditor(
    nickname: String, 
    avatarId: String, 
    onUpdate: (String, String) -> Unit,
    onPickImage: () -> Unit
) {
    var name by remember { mutableStateOf(nickname) }
    
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    onUpdate(it, avatarId)
                },
                label = { Text("代号 (Nickname)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("头像接入点", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onPickImage) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("上传图片")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Custom Avatar Preview (if avatarId is a file path)
            if (File(avatarId).exists()) {
                 Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onPickImage() },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(File(avatarId))
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("预设头像", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val avatars = listOf("avatar_1" to Icons.Default.Face, "avatar_2" to Icons.Default.AccountCircle, "avatar_3" to Icons.Default.SmartToy, "avatar_4" to Icons.Default.Fingerprint)
                avatars.forEach { (id, icon) ->
                    val selected = id == avatarId
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onUpdate(name, id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon, 
                            contentDescription = null, 
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}