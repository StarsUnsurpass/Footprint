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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.clickable
import com.footprint.utils.AppUtils

@Composable
fun MapScreen() {
    // ... (MapScreen content remains the same until ApiKeyDialog call)
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    // ...
    // ...
    
    if (showApiKeyDialog) {
        ApiKeyDialog(
            initialKey = ApiKeyManager.getApiKey(context) ?: "",
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                ApiKeyManager.setApiKey(context, key)
                // 立即生效，无需重启
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
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // SHA1 显示区域
                Text(
                    text = "Package: ${context.packageName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "SHA1 (点击复制):", style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(alpha = 0.7f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
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
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Icon(Icons.Default.ContentCopy, "复制", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "请输入您的高德地图 API Key：", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
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
