package com.footprint.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.os.Bundle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.footprint.service.LocationTrackingService
import com.footprint.utils.ApiKeyManager
import com.footprint.ui.components.GlassMorphicCard

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    
    // 管理 MapView 生命周期
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        
        // 关键修复：手动调用 onCreate，防止因生命周期错位导致白屏
        mapView.onCreate(Bundle())
        // 如果当前已经在前台（例如从其他页面返回或首次加载时已 RESUMED），补调 onResume
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onDestroy()
        }
    }
    
    // 扩展权限列表
    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val isTracking by LocationTrackingService.isTracking.collectAsState()
    val currentLocation by LocationTrackingService.currentLocation.collectAsState()
    val trackingPath by LocationTrackingService.trackingPath.collectAsState()
    
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // 监听位置，并确保相机移动是基于有效坐标的
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            if (loc.latitude > 1.0) {
                mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            AndroidView(
                factory = {
                    mapView.apply {
                        map.apply {
                            uiSettings.isMyLocationButtonEnabled = false
                            isMyLocationEnabled = true
                            myLocationStyle = MyLocationStyle().apply {
                                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                                interval(2000)
                                showMyLocation(true)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { mv ->
                if (trackingPath.isNotEmpty()) {
                    mv.map.clear()
                    val points = trackingPath.map { LatLng(it.latitude, it.longitude) }
                    mv.map.addPolyline(PolylineOptions().addAll(points).width(18f).color(android.graphics.Color.parseColor("#00FF9F")))
                }
            }
        } else {
            PermissionDenyOverlay { launcher.launch(permissionsToRequest) }
        }
        
        // API Key 设置按钮
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 20.dp)) {
            GlassMorphicCard(
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(
                    onClick = { showApiKeyDialog = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Default.Settings, "设置 API Key", tint = Color.Black.copy(alpha = 0.8f))
                }
            }
        }

        // 定位回正按钮
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp)) {
            GlassMorphicCard(
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentLocation != null && currentLocation!!.latitude > 1.0) {
                            mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation!!.latitude, currentLocation!!.longitude), 18f))
                        } else {
                            // 强制拉起一次定位
                            LocationTrackingService.startTracking(context)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Rounded.GpsFixed, null, tint = Color.Black.copy(alpha = 0.8f))
                }
            }
        }

        // 底部控制
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 110.dp)
                .fillMaxWidth()
                .height(88.dp)
        ) {
            GlassMorphicCard(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "GPS 状态", 
                            color = Color.Black.copy(alpha = 0.6f), 
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            if (currentLocation == null) "搜索信号..." else "信号良好", 
                            color = if (currentLocation == null) Color(0xFFE6A23C) else Color(0xFF67C23A), 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            if (isTracking) LocationTrackingService.stopTracking(context)
                            else LocationTrackingService.startTracking(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracking) Color(0xFFFF4D4F) else Color(0xFF1890FF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            if (isTracking) "停止" else "开始", 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    if (showApiKeyDialog) {
        ApiKeyDialog(
            initialKey = ApiKeyManager.getApiKey(context) ?: "",
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                ApiKeyManager.setApiKey(context, key)
                showApiKeyDialog = false
                android.widget.Toast.makeText(context, "API Key 已保存，请重启应用生效", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun ApiKeyDialog(initialKey: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var apiKey by remember { mutableStateOf(initialKey) }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassMorphicCard(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "设置 API Key", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "请输入您的高德地图 API Key：", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1890FF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(apiKey) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1890FF))
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDenyOverlay(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Security, null, tint = Color(0xFF00FF9F), modifier = Modifier.size(64.dp))
            Text("需要定位与通知权限", color = Color.White, modifier = Modifier.padding(top = 16.dp))
            Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp)) { Text("立即授权") }
        }
    }
}
