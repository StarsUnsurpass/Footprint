package com.footprint.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import com.footprint.ui.components.SwipeableItem
import com.footprint.data.model.Mood
import com.footprint.data.model.FootprintEntry
import com.footprint.ui.state.FootprintUiState
import com.footprint.ui.components.AppBackground
import com.footprint.ui.components.GlassMorphicCard
import com.footprint.ui.components.AboutDialog
import com.footprint.ui.components.IconUtils
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.LocalDate

enum class StatType(val label: String, val icon: ImageVector) {
    TRACK_POINTS("年度足迹点数", Icons.Default.Route),
    MILEAGE("年度总里程", Icons.Default.Map),
    PLACES("探索地点总计", Icons.Default.Place),
    RECORDS("年度记录明细", Icons.Default.Description),
    ENERGY("年度活力指数", Icons.Default.Bolt),
    MOOD("年度情绪分布", Icons.Default.EmojiEmotions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    state: FootprintUiState,
    onSearch: (String) -> Unit,
    onYearShift: (Int) -> Unit,
    onMoodSelected: (Mood?) -> Unit,
    onCreateGoal: () -> Unit,
    onExportTrace: (Int?) -> Unit,
    onSettings: () -> Unit,
    onEditEntry: (com.footprint.data.model.FootprintEntry) -> Unit,
    onDeleteEntry: (com.footprint.data.model.FootprintEntry) -> Unit,
    onEditGoal: (com.footprint.data.model.TravelGoal) -> Unit,
    onDeleteGoal: (com.footprint.data.model.TravelGoal) -> Unit,
    onMemoryLaneClick: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf(state.filterState.searchQuery) }
    val df = remember { DecimalFormat("0.0") }
    val focusManager = LocalFocusManager.current

    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    var selectedStatType by remember { mutableStateOf<StatType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    
    var isSearchFocused by remember { mutableStateOf(false) }

    val openStatDetail = { type: StatType ->
        selectedStatType = type
        showBottomSheet = true
    }

    AppBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1: Content (Scrollable & Blurred on focus)
            LazyColumn(
                contentPadding = PaddingValues(top = 190.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (isSearchFocused) 20.dp else 0.dp)
            ) {
                // Year Navigator
                item {
                    YearNavigator(
                        year = state.filterState.year,
                        onBack = { onYearShift(-1) },
                        onForward = { onYearShift(1) }
                    )
                }

                // Statistics Grid
                item {
                    StatisticsSection(
                        state = state,
                        onStatClick = { type ->
                            when (type) {
                                StatType.TRACK_POINTS -> onExportTrace(state.filterState.year)
                                else -> openStatDetail(type)
                            }
                        },
                        df = df
                    )
                }

                // Memory Lane (Personalized)
                item {
                    MemoryLaneSection(
                        memory = state.randomMemory,
                        quote = state.memoryQuote,
                        onClick = onMemoryLaneClick
                    )
                }

                // History Trace Action
                item {
                    TelegramActionCard(
                        title = "时光足迹回放",
                        subtitle = "查看历史移动轨迹与时空分布",
                        icon = Icons.Default.History,
                        onClick = { onExportTrace(state.filterState.year) }
                    )
                }

                // Sections (Footprints)
                recentFootprintsSection(
                    entries = state.visibleEntries, 
                    onCreateGoal = onCreateGoal, 
                    onEditEntry = onEditEntry,
                    onDeleteEntry = onDeleteEntry
                )
                
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // Sections (Goals)
                goalsListSection(
                    goals = state.goals, 
                    onEditGoal = onEditGoal,
                    onDeleteGoal = onDeleteGoal
                )
            }

            // Layer 2: Transparent Dismiss Overlay (only when searching)
            if (isSearchFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { 
                            focusManager.clearFocus()
                        }
                )
            }

            // Layer 3: Search Results (Real-time List)
            if (isSearchFocused && query.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 180.dp, start = 16.dp, end = 16.dp)
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp
                ) {
                    if (state.visibleEntries.isEmpty()) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("未找到相关记录", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn {
                            items(state.visibleEntries) { entry ->
                                TelegramEntryItem(
                                    entry = entry,
                                    dateFormatter = DateTimeFormatter.ofPattern("MM-dd"),
                                    onClick = {
                                        onEditEntry(entry)
                                        focusManager.clearFocus()
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }

            // Layer 4: Top Bar & Search Bar (Fixed & Clear)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSettings() }
                            .padding(4.dp)
                    ) {
                        // Avatar
                        val avatarId = state.userAvatarId
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (File(avatarId).exists()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(File(avatarId))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Avatar",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val icon = when(avatarId) {
                                    "avatar_2" -> Icons.Default.AccountCircle
                                    "avatar_3" -> Icons.Default.SmartToy
                                    "avatar_4" -> Icons.Default.Fingerprint
                                    else -> Icons.Default.Face
                                }
                                Icon(
                                    icon, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column {
                            val greeting = remember {
                                val hour = java.time.LocalTime.now().hour
                                when {
                                    hour < 6 -> "凌晨好"
                                    hour < 12 -> "早安"
                                    hour < 18 -> "午后时光"
                                    else -> "晚安"
                                }
                            }
                            Text(
                                text = "$greeting, ${state.userNickname}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val levelInfo = remember(state.summary.yearly.totalDistance) {
                                val dist = state.summary.yearly.totalDistance
                                when {
                                    dist < 10 -> "新手旅行者" to Icons.Default.DirectionsWalk
                                    dist < 50 -> "进阶探索者" to Icons.Default.Explore
                                    dist < 200 -> "里程达人" to Icons.Default.MilitaryTech
                                    else -> "传奇旅行家" to Icons.Default.Public
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(levelInfo.second, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = levelInfo.first,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("设置") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = {
                                    showMenu = false
                                    onSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("关于") },
                                leadingIcon = { Icon(Icons.Default.Info, null) },
                                onClick = {
                                    showMenu = false
                                    showAboutDialog = true
                                }
                            )
                        }
                    }
                }

                // Search Bar
                SearchBar(
                    query = query,
                    onQueryChange = {
                        query = it
                        onSearch(it)
                    },
                    onFocusChange = { isSearchFocused = it },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
            }
        }

        // Bottom Sheet
        if (showBottomSheet && selectedStatType != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                StatDetailContent(
                    type = selectedStatType!!,
                    state = state,
                    onEditEntry = { 
                        onEditEntry(it)
                        showBottomSheet = false
                    }
                )
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        "搜索地点、标签...", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChange(it.isFocused) }
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatisticsSection(
    state: FootprintUiState,
    onStatClick: (StatType) -> Unit,
    df: DecimalFormat
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).animateContentSize()) {
        Text(
            "年度数据总览", 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                label = "足迹",
                value = "${state.summary.yearly.totalTrackPoints}",
                modifier = Modifier.weight(1f),
                onClick = { onStatClick(StatType.TRACK_POINTS) }
            )
            StatItem(
                label = "里程",
                value = "${df.format(state.summary.yearly.totalDistance)}",
                unit = "km",
                modifier = Modifier.weight(1f),
                onClick = { onStatClick(StatType.MILEAGE) }
            )
            StatItem(
                label = "地点",
                value = "${state.summary.yearly.totalEntries}",
                modifier = Modifier.weight(1f),
                onClick = { onStatClick(StatType.PLACES) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                label = "记录",
                value = "${state.summary.yearly.totalEntries}",
                modifier = Modifier.weight(1f),
                onClick = { onStatClick(StatType.RECORDS) }
            )
            StatItem(
                label = "活力",
                value = "${state.summary.yearly.vitalityIndex}",
                unit = "指数",
                modifier = Modifier.weight(1f),
                onClick = { onStatClick(StatType.ENERGY) }
            )
            StatItem(
                label = "主情绪",
                value = state.summary.yearly.dominantMood?.label ?: "待发现",
                modifier = Modifier.weight(1f),
                onClick = { onStatClick(StatType.MOOD) }
            )
        }
    }
}

@Composable
fun MemoryLaneSection(
    memory: FootprintEntry?, 
    quote: String?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "那年今日 / 时光碎片", 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().clickable { onClick() }
        ) {
            if (memory != null) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(memory.mood.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            IconUtils.getIconByName(memory.icon),
                            contentDescription = null,
                            tint = memory.mood.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val yearsAgo = LocalDate.now().year - memory.happenedOn.year
                        Text(
                            text = "${yearsAgo}年前的今天 · ${memory.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = quote ?: "记录当下的每一步，让未来有迹可循。",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    unit: String = "",
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Scale"
    )

    GlassMorphicCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> isPressed = true
                            PointerEventType.Release, PointerEventType.Exit -> isPressed = false
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            if (unit.isNotEmpty()) {
                Text(text = unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TelegramActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    GlassMorphicCard(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun MoodRadarSection(currentMood: Mood?, onMoodSelected: (Mood?) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("年度心情偏好", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Mood.entries.forEach { mood ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (currentMood == mood) mood.color else mood.color.copy(alpha = 0.2f))
                        .clickable { onMoodSelected(mood) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mood.label.take(1),
                        color = if (currentMood == mood) Color.White else mood.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatDetailContent(
    type: StatType,
    state: FootprintUiState,
    onEditEntry: (FootprintEntry) -> Unit
) {
    val df = remember { DecimalFormat("0.0") }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    
    val entries = state.entries.filter { it.happenedOn.year == state.filterState.year }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Icon(
                type.icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = type.label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        when (type) {
            StatType.PLACES -> {
                val places = entries.groupBy { it.location }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(places) { (location, count) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                    Text(location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text("${count} 次", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            StatType.MOOD -> {
                val moodStats = entries.groupBy { it.mood }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(moodStats) { (mood, count) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = mood.color.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(mood.color))
                                    Text(mood.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text("${count} 次", style = MaterialTheme.typography.labelSmall, color = mood.color)
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sortedEntries = if (type == StatType.ENERGY) entries.sortedByDescending { it.energyLevel } else entries
                    
                    items(sortedEntries) { entry ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditEntry(entry) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(entry.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(entry.happenedOn.format(formatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                if (type == StatType.MILEAGE) {
                                    Text(
                                        "${df.format(entry.distanceKm)} km",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (type == StatType.ENERGY) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { entry.energyLevel / 10f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}


@Composable
private fun YearNavigator(year: Int, onBack: () -> Unit, onForward: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "年份筛选", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Outlined.ArrowBackIosNew, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = year.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = onForward, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.KeyboardArrowRight, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ExpandableMonthHeader(
    month: Int,
    isExpanded: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${month}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = color.copy(alpha = 0.6f)
        )
    }
}

private fun LazyListScope.goalsListSection(
    goals: List<com.footprint.data.model.TravelGoal>,
    onEditGoal: (com.footprint.data.model.TravelGoal) -> Unit,
    onDeleteGoal: (com.footprint.data.model.TravelGoal) -> Unit
) {
    val grouped = goals.groupBy { it.targetDate.monthValue }
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            Text(
                text = "年度旅行目标", 
                style = MaterialTheme.typography.labelMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }

    if (goals.isEmpty()) return

    grouped.forEach { (month, monthGoals) ->
        item {
            var isExpanded by remember { mutableStateOf(true) }
            Column(modifier = Modifier.animateContentSize()) {
                ExpandableMonthHeader(
                    month = month,
                    isExpanded = isExpanded,
                    color = MaterialTheme.colorScheme.secondary,
                    onToggle = { isExpanded = !isExpanded }
                )
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        monthGoals.forEach { goal ->
                            SwipeableItem(
                                onEdit = { onEditGoal(goal) },
                                onDelete = { onDeleteGoal(goal) }
                            ) {
                                GlassMorphicCard(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clickable { onEditGoal(goal) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (goal.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (goal.isCompleted) Icons.Default.Check else IconUtils.getIconByName(goal.icon),
                                                null,
                                                tint = if (goal.isCompleted) Color.White else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${goal.targetLocation} · ${goal.targetDate.format(formatter)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.recentFootprintsSection(
    entries: List<com.footprint.data.model.FootprintEntry>, 
    onCreateGoal: () -> Unit,
    onEditEntry: (com.footprint.data.model.FootprintEntry) -> Unit,
    onDeleteEntry: (com.footprint.data.model.FootprintEntry) -> Unit
) {
    val grouped = entries.groupBy { it.happenedOn.monthValue }
    val formatter = DateTimeFormatter.ofPattern("MM-dd")
    
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Route, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(text = "年度足迹轨迹", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "新建 +", 
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onCreateGoal() }
            )
        }
    }

    if (entries.isEmpty()) return

    grouped.forEach { (month, monthEntries) ->
        item {
            var isExpanded by remember { mutableStateOf(true) }
            Column(modifier = Modifier.animateContentSize()) {
                ExpandableMonthHeader(
                    month = month,
                    isExpanded = isExpanded,
                    color = MaterialTheme.colorScheme.primary,
                    onToggle = { isExpanded = !isExpanded }
                )
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        monthEntries.forEach { entry ->
                            SwipeableItem(
                                onEdit = { onEditEntry(entry) },
                                onDelete = { onDeleteEntry(entry) }
                            ) {
                                TelegramEntryItem(entry, formatter, { onEditEntry(entry) })
                            }
                        }
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
    onClick: () -> Unit
) {
    GlassMorphicCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(entry.mood.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    IconUtils.getIconByName(entry.icon),
                    contentDescription = null,
                    tint = entry.mood.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
