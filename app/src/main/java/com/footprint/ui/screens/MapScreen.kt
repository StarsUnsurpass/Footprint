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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
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
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.AMap
import com.footprint.data.model.FootprintEntry
import com.footprint.service.LocationTrackingService
import com.footprint.utils.ApiKeyManager
import com.footprint.ui.components.GlassMorphicCard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import com.footprint.utils.AppUtils

@Composable
fun MapScreen(
    entries: List<FootprintEntry> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    val bottomPadding = contentPadding.calculateBottomPadding()
    
    var selectedEntry by remember { mutableStateOf<FootprintEntry?>(null) }

    // Persistent Map State
    var lastLat by rememberSaveable { mutableStateOf(39.9042) } // Beijing default
    var lastLng by rememberSaveable { mutableStateOf(116.4074) }
    var lastZoom by rememberSaveable { mutableStateOf(10f) }

    // State to handle auto-centering
    var isPendingCenter by remember { mutableStateOf(true) }

    // 管理 MapView 生命周期
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> {
                    val camera = mapView.map.cameraPosition
                    lastLat = camera.target.latitude
                    lastLng = camera.target.longitude
                    lastZoom = camera.zoom
                    mapView.onPause()
                }
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        
        mapView.onCreate(Bundle())
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            val camera = mapView.map.cameraPosition
            lastLat = camera.target.latitude
            lastLng = camera.target.longitude
            lastZoom = camera.zoom
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onDestroy()
        }
    }

    // 监听主题变化，更新地图样式
    LaunchedEffect(isDark) {
        mapView.map.mapType = if (isDark) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
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

    // Auto-centering logic: handles both switching back and first-time fix
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            if (loc.latitude > 1.0 && isPendingCenter) {
                mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17f))
                isPendingCenter = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(
                if (showApiKeyDialog) {
                    Modifier
                        .blur(16.dp)
                        .drawWithContent {
                            drawContent()
                            drawRect(if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f))
                        }
                } else Modifier
            )
    ) { 
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    mapView.apply {
                        map.apply {
                            uiSettings.isMyLocationButtonEnabled = false
                            isMyLocationEnabled = true
                            mapType = if (isDark) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
                            myLocationStyle = MyLocationStyle().apply {
                                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                                interval(2000)
                                showMyLocation(true)
                            }
                            
                            setOnMarkerClickListener { marker ->
                                val entryId = marker.snippet?.toLongOrNull()
                                selectedEntry = entries.find { it.id == entryId }
                                true
                            }
                            
                            setOnMapClickListener {
                                selectedEntry = null
                            }

                            // Restore last known position immediately
                            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLat, lastLng), lastZoom))
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { mv ->
                mv.map.clear()
                
                // Draw Tracking Path
                if (trackingPath.isNotEmpty()) {
                    val points = trackingPath.map { LatLng(it.latitude, it.longitude) }
                    mv.map.addPolyline(PolylineOptions().addAll(points).width(18f).color(android.graphics.Color.parseColor("#00FF9F")))
                }
                
                // Draw Footprint Markers
                entries.forEach { entry ->
                    if (entry.latitude != null && entry.longitude != null) {
                        mv.map.addMarker(
                            MarkerOptions()
                                .position(LatLng(entry.latitude, entry.longitude))
                                .title(entry.title)
                                .snippet(entry.id.toString())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    }
                }
            }
        } else {
            PermissionDenyOverlay { launcher.launch(permissionsToRequest) }
        }
        
        AnimatedVisibility(
            visible = selectedEntry != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 100.dp, start = 24.dp, end = 24.dp)
        ) {
            selectedEntry?.let { entry ->
                GlassMorphicCard(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(entry.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        IconButton(onClick = { selectedEntry = null }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
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
                    Icon(Icons.Default.Settings, "设置 API Key", tint = MaterialTheme.colorScheme.onSurface)
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
                        isPendingCenter = true // Request auto-center on fix
                        if (currentLocation != null && currentLocation!!.latitude > 1.0) {
                            mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation!!.latitude, currentLocation!!.longitude), 18f))
                            isPendingCenter = false // Already centered
                        } else {
                            android.widget.Toast.makeText(context, "正在请求定位...", android.widget.Toast.LENGTH_SHORT).show()
                            LocationTrackingService.startTracking(context)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Rounded.GpsFixed, null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = bottomPadding + 16.dp)
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), 
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
                            else {
                                isPendingCenter = true // Start tracking and center map on first fix
                                LocationTrackingService.startTracking(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracking) Color(0xFFFF4D4F) else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                try {
                    com.amap.api.maps.MapsInitializer.setApiKey(key)
                    com.amap.api.location.AMapLocationClient.setApiKey(key)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showApiKeyDialog = false
                android.widget.Toast.makeText(context, "API Key 已保存并立即生效", android.widget.Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun ApiKeyDialog(initialKey: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var apiKey by remember { mutableStateOf(initialKey) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sha1 = remember { AppUtils.getAppSignature(context) }
    
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Package: ${context.packageName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "SHA1 (点击复制):", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(sha1))
                            android.widget.Toast.makeText(context, "SHA1 已复制", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                ) {
                    Text(
                        text = sha1, 
                        style = MaterialTheme.typography.labelSmall, 
                        modifier = Modifier.weight(1f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Icon(Icons.Default.ContentCopy, "复制", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "请输入您的高德地图 API Key：", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(apiKey) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Text("需要定位与通知权限", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp))
            Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp)) { Text("立即授权") }
        }
    }
}