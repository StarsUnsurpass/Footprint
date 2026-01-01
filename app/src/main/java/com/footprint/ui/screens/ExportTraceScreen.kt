package com.footprint.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.PolylineOptions
import com.footprint.FootprintViewModel
import com.footprint.ui.components.GlassMorphicCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportTraceScreen(
    viewModel: FootprintViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    
    // Default to today 00:00 to now
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var startHour by remember { mutableStateOf(0) }
    var startMinute by remember { mutableStateOf(0) }
    
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var endHour by remember { mutableStateOf(LocalTime.now().hour) }
    var endMinute by remember { mutableStateOf(LocalTime.now().minute) }
    
    // Points state
    var points by remember { mutableStateOf<List<com.footprint.data.local.TrackPointEntity>>(emptyList()) }
    
    // Map lifecycle
    DisposableEffect(mapView) {
        mapView.onCreate(null)
        mapView.onResume()
        onDispose {
            mapView.onDestroy()
        }
    }

    LaunchedEffect(points) {
        mapView.map.clear()
        if (points.isNotEmpty()) {
            val latLngs = points.map { LatLng(it.latitude, it.longitude) }
            mapView.map.addPolyline(
                PolylineOptions()
                    .addAll(latLngs)
                    .width(18f)
                    .color(android.graphics.Color.parseColor("#00FF9F"))
            )
            
            // Zoom to bounds
            val builder = LatLngBounds.builder()
            latLngs.forEach { builder.include(it) }
            try {
                mapView.map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
            } catch (e: Exception) {
                // Bounds might be invalid if points are too close
                 if (latLngs.isNotEmpty()) {
                    mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 15f))
                 }
            }
        }
    }

    // Better approach: State driven.
    val startTimestamp = remember(startDate, startHour, startMinute) {
        LocalDateTime.of(startDate, LocalTime.of(startHour, startMinute))
            .toInstant(ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())).toEpochMilli()
    }
    val endTimestamp = remember(endDate, endHour, endMinute) {
         LocalDateTime.of(endDate, LocalTime.of(endHour, endMinute))
            .toInstant(ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())).toEpochMilli()
    }
    
    val tracePoints by viewModel.getTrackPoints(startTimestamp, endTimestamp).collectAsStateWithLifecycle(initialValue = emptyList())

    // Update local points when flow emits
    LaunchedEffect(tracePoints) {
        points = tracePoints
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar
        Row(
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            SmallFloatingActionButton(
                onClick = onBack,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        // Controls
        GlassMorphicCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "足迹回放 (精确到分钟)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeSelector(
                        label = "开始",
                        date = startDate,
                        hour = startHour,
                        minute = startMinute,
                        onDateChange = { startDate = it },
                        onTimeChange = { h, m -> 
                            startHour = h
                            startMinute = m 
                        }
                    )
                    
                    Text("to", color = MaterialTheme.colorScheme.outline)
                    
                    TimeSelector(
                        label = "结束",
                        date = endDate,
                        hour = endHour,
                        minute = endMinute,
                        onDateChange = { endDate = it },
                        onTimeChange = { h, m -> 
                            endHour = h
                            endMinute = m
                        }
                    )
                }
                
                Button(
                    onClick = { /* Auto updates via Flow */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = points.isNotEmpty()
                ) {
                    Text("当前显示 ${points.size} 个轨迹点")
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    label: String,
    date: LocalDate,
    hour: Int,
    minute: Int,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, y, m, d -> onDateChange(LocalDate.of(y, m + 1, d)) },
                        date.year,
                        date.monthValue - 1,
                        date.dayOfMonth
                    ).show()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = date.format(dateFormatter),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
             verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable {
                     android.app.TimePickerDialog(
                        context,
                        { _, h, m -> onTimeChange(h, m) },
                        hour,
                        minute,
                        true
                    ).show()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
             Text(
                text = String.format("%02d:%02d", hour, minute),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
