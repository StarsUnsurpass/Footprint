package com.footprint

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.footprint.ui.components.AddFootprintDialog
import com.footprint.ui.components.AddGoalDialog
import com.footprint.ui.screens.*
import com.footprint.ui.theme.FootprintTheme

@Composable
fun FootprintApp() {
    val navController = rememberNavController()
    val viewModel: FootprintViewModel = viewModel(factory = FootprintViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showEntryDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<com.footprint.data.model.FootprintEntry?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<com.footprint.data.model.TravelGoal?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    val isBlurActive = showEntryDialog || editingEntry != null || showGoalDialog || editingGoal != null
    val isDark = uiState.themeMode == com.footprint.ui.theme.ThemeMode.DARK || 
                (uiState.themeMode == com.footprint.ui.theme.ThemeMode.SYSTEM && isSystemInDarkTheme())

    val tabOrder = listOf("dashboard", "map", "timeline", "planner")

    FootprintTheme(
        themeMode = uiState.themeMode,
        style = uiState.themeStyle,
        dominantMood = uiState.summary.monthly.dominantMood
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.then(
                    if (isBlurActive) {
                        Modifier
                            .blur(16.dp)
                            .drawWithContent {
                                drawContent()
                                drawRect(if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f))
                            }
                    } else Modifier
                ),
                floatingActionButton = {
                    if (currentDestination == "dashboard" || currentDestination == "timeline" || currentDestination == "planner") {
                        FloatingActionButton(
                            onClick = { showEntryDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.padding(bottom = 80.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                        }
                    }
                },
                bottomBar = {
                    val hideBottomBar = currentDestination == "settings" || currentDestination?.startsWith("export_trace") == true
                    if (!hideBottomBar) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                                .height(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                .border(
                                    width = 0.5.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                                            if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                FootprintTab.entries.forEach { tab ->
                                    val selected = currentDestination == tab.route
                                    val iconScale by animateFloatAsState(
                                        targetValue = if (selected) 1.2f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                        label = "IconScale"
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                tab.icon,
                                                contentDescription = tab.label,
                                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                                            )
                                            if (selected) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 4.dp)
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { 
                        val fromRoute = initialState.destination.route
                        val toRoute = targetState.destination.route
                        val fromIndex = tabOrder.indexOf(fromRoute)
                        val toIndex = tabOrder.indexOf(toRoute)
                        
                        if (fromIndex != -1 && toIndex != -1) {
                            if (toIndex > fromIndex) {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()
                            } else {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()
                            }
                        } else {
                            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        }
                    },
                    exitTransition = { 
                        val fromRoute = initialState.destination.route
                        val toRoute = targetState.destination.route
                        val fromIndex = tabOrder.indexOf(fromRoute)
                        val toIndex = tabOrder.indexOf(toRoute)
                        
                        if (fromIndex != -1 && toIndex != -1) {
                            if (toIndex > fromIndex) {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                            } else {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                            }
                        } else {
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            state = uiState,
                            onSearch = viewModel::updateSearch,
                            onYearShift = viewModel::shiftYear,
                            onMoodSelected = viewModel::toggleMoodFilter,
                            onCreateGoal = { showGoalDialog = true },
                            onExportTrace = { year -> 
                                if (year != null) navController.navigate("export_trace/$year")
                                else navController.navigate("export_trace")
                            },
                            onSettings = { navController.navigate("settings") },
                            onEditEntry = { editingEntry = it },
                            onDeleteEntry = viewModel::deleteFootprint,
                            onEditGoal = { editingGoal = it },
                            onDeleteGoal = viewModel::deleteGoal,
                            onMemoryLaneClick = { 
                                navController.navigate("timeline") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable("settings") {
                        val context = LocalContext.current
                        SettingsScreen(
                            currentThemeMode = uiState.themeMode,
                            currentThemeStyle = uiState.themeStyle,
                            currentNickname = uiState.userNickname,
                            currentAvatarId = uiState.userAvatarId,
                            onThemeModeChange = viewModel::setThemeMode,
                            onThemeStyleChange = viewModel::setThemeStyle,
                            onUpdateProfile = viewModel::updateProfile,
                            onUpdateAvatar = viewModel::updateAvatar,
                            onExportData = { uri ->
                                viewModel.exportData(
                                    uri = uri,
                                    onSuccess = { android.widget.Toast.makeText(context, "数据导出成功", android.widget.Toast.LENGTH_SHORT).show() },
                                    onError = { error -> android.widget.Toast.makeText(context, "导出错误: $error", android.widget.Toast.LENGTH_LONG).show() }
                                )
                            },
                            onImportData = { uri ->
                                viewModel.importData(
                                    uri = uri,
                                    onSuccess = { android.widget.Toast.makeText(context, "数据恢复完成", android.widget.Toast.LENGTH_SHORT).show() },
                                    onError = { error -> android.widget.Toast.makeText(context, "导入错误: $error", android.widget.Toast.LENGTH_LONG).show() }
                                )
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("export_trace") {
                        ExportTraceScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("export_trace/{year}") { backStackEntry ->
                        val year = backStackEntry.arguments?.getString("year")?.toIntOrNull()
                        ExportTraceScreen(
                            viewModel = viewModel,
                            initialYear = year,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("map") { 
                        MapScreen(
                            entries = uiState.visibleEntries,
                            contentPadding = innerPadding
                        ) 
                    }
                    composable("timeline") {
                        TimelineScreen(
                            entries = if (uiState.filterState.searchQuery.isBlank()) uiState.entries else uiState.visibleEntries,
                            filterState = uiState.filterState,
                            onMoodFilterChange = viewModel::toggleMoodFilter,
                            onSearch = viewModel::updateSearch,
                            onEditEntry = { editingEntry = it },
                            onDeleteEntry = viewModel::deleteFootprint
                        )
                    }
                    composable("planner") {
                        GoalPlannerScreen(
                            goals = uiState.goals,
                            summary = uiState.summary,
                            onToggleGoal = viewModel::toggleGoal,
                            onAddGoal = { showGoalDialog = true },
                            onEditGoal = { editingGoal = it },
                            onDeleteGoal = viewModel::deleteGoal
                        )
                    }
                }
            }
        }

        // 处理添加/编辑对话框
        if (showEntryDialog || editingEntry != null) {
            AddFootprintDialog(
                initialEntry = editingEntry,
                onDismiss = { 
                    showEntryDialog = false
                    editingEntry = null
                },
                onSave = { payload ->
                    if (editingEntry != null) {
                        viewModel.updateFootprint(editingEntry!!.copy(
                            title = payload.title,
                            location = payload.location,
                            detail = payload.detail,
                            mood = payload.mood,
                            tags = payload.tags,
                            distanceKm = payload.distance,
                            energyLevel = payload.energy,
                            happenedOn = payload.date,
                            latitude = payload.latitude,
                            longitude = payload.longitude,
                            icon = payload.icon
                        ))
                    } else {
                        viewModel.addFootprint(
                            title = payload.title,
                            location = payload.location,
                            detail = payload.detail,
                            mood = payload.mood,
                            tags = payload.tags,
                            distanceKm = payload.distance,
                            photos = emptyList(),
                            energyLevel = payload.energy,
                            date = payload.date,
                            latitude = payload.latitude,
                            longitude = payload.longitude,
                            icon = payload.icon
                        )
                    }
                    showEntryDialog = false
                    editingEntry = null
                }
            )
        }

        if (showGoalDialog || editingGoal != null) {
            AddGoalDialog(
                initialGoal = editingGoal,
                onDismiss = { 
                    showGoalDialog = false
                    editingGoal = null
                },
                onSave = { goal ->
                    if (editingGoal != null) {
                        viewModel.updateGoal(editingGoal!!.copy(
                            title = goal.title,
                            targetLocation = goal.location,
                            targetDate = goal.date,
                            notes = goal.notes,
                            icon = goal.icon
                        ))
                    } else {
                        viewModel.addGoal(goal.title, goal.location, goal.date, goal.notes, goal.icon)
                    }
                    showGoalDialog = false
                    editingGoal = null
                }
            )
        }
    }
}

enum class FootprintTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Dashboard("dashboard", "概览", Icons.Outlined.Dashboard),
    Map("map", "地图", Icons.Outlined.Map),
    Timeline("timeline", "足迹簿", Icons.Outlined.CalendarMonth),
    Planner("planner", "目标", Icons.Outlined.CheckCircle);
    
    companion object {
        val entries = values().toList()
    }
}