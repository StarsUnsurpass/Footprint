package com.footprint

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.footprint.ui.components.AddFootprintDialog
import com.footprint.ui.components.AddGoalDialog
import com.footprint.ui.screens.*

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

    Scaffold(
        modifier = Modifier.then(
            if (isBlurActive) {
                Modifier
                    .blur(16.dp)
                    .drawWithContent {
                        drawContent()
                        drawRect(Color.White.copy(alpha = 0.3f)) // Add overlay to improve legibility of dialogs
                    }
            } else Modifier
        ),
        floatingActionButton = {
            if (currentDestination != "map" && currentDestination != "export_trace") {
                ExtendedFloatingActionButton(
                    onClick = { showEntryDialog = true },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("记录足迹") }
                )
            }
        },
        bottomBar = {
            if (currentDestination != "export_trace") {
                // 液态玻璃底部栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        FootprintTab.entries.forEach { tab ->
                            val selected = currentDestination == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()), // 调整 Padding 避免遮挡玻璃栏
            enterTransition = { 
                fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400))
            },
            exitTransition = { 
                fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400))
            }
        ) {
            composable("dashboard") {
                DashboardScreen(
                    state = uiState,
                    onSearch = viewModel::updateSearch,
                    onYearShift = viewModel::shiftYear,
                    onMoodSelected = viewModel::toggleMoodFilter,
                    onCreateGoal = { showGoalDialog = true },
                    onExportTrace = { navController.navigate("export_trace") },
                    onEditEntry = { editingEntry = it }
                )
            }
            composable("export_trace") {
                ExportTraceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("map") { MapScreen() }
            composable("timeline") {
                TimelineScreen(
                    entries = uiState.visibleEntries,
                    filterState = uiState.filterState,
                    onMoodFilterChange = viewModel::toggleMoodFilter,
                    onSearch = viewModel::updateSearch,
                    onEditEntry = { editingEntry = it }
                )
            }
            composable("planner") {
                GoalPlannerScreen(
                    goals = uiState.goals,
                    summary = uiState.summary,
                    onToggleGoal = viewModel::toggleGoal,
                    onAddGoal = { showGoalDialog = true },
                    onEditGoal = { editingGoal = it }
                )
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
                        happenedOn = payload.date
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
                        date = payload.date
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
                        notes = goal.notes
                    ))
                } else {
                    viewModel.addGoal(goal.title, goal.location, goal.date, goal.notes)
                }
                showGoalDialog = false
                editingGoal = null
            }
        )
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