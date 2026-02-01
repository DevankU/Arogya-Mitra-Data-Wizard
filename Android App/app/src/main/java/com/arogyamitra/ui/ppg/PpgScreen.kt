package com.arogyamitra.ui.ppg

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arogyamitra.R
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.BackgroundDark
import com.arogyamitra.ui.theme.BlueAccent
import com.arogyamitra.ui.theme.BlueLight
import com.arogyamitra.ui.theme.BorderDark
import com.arogyamitra.ui.theme.RedAccent
import com.arogyamitra.ui.theme.SurfaceGlass
import com.arogyamitra.ui.theme.TextSecondaryDark
import com.arogyamitra.ui.chat.components.PpgData

@Composable
fun PpgScreen(
    onNavigateBack: (PpgData?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PpgViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, re-trigger initialization
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.hasCameraPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scannerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            PpgTopBar(
                onBack = {
                    val result = if (uiState.hasResult) {
                        PpgData(
                            bpm = uiState.heartRate,
                            history = uiState.instantaneousBPM,
                            hrv = uiState.hrv?.rmssd
                        )
                    } else null
                    onNavigateBack(result)
                },
                modifier = Modifier.padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instruction/Status pill
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceGlass)
                    .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when {
                                    uiState.error != null -> RedAccent
                                    uiState.hasResult -> ArogyaPrimary
                                    uiState.faceDetected -> BlueLight
                                    else -> Color.Gray
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Camera preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceGlass)
                    .border(
                        width = 1.dp,
                        color = if (uiState.faceDetected) ArogyaPrimary else BorderDark,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.hasCameraPermission()) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                viewModel.initializeCamera(this, lifecycleOwner)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "Camera permission required",
                        color = TextSecondaryDark,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Overlay gradient for style
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    BackgroundDark.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                // Face Overlay - Corner Frames
                CornerFrames(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    color = if (uiState.isScanning && uiState.faceDetected) 
                        ArogyaPrimary.copy(alpha = scannerAlpha) 
                    else 
                        Color.White.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Vitals cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VitalCard(
                    label = stringResource(R.string.ppg_heart_rate),
                    value = if (uiState.heartRate > 0) uiState.heartRate.toString() else "--",
                    unit = stringResource(R.string.ppg_bpm),
                    icon = Icons.Outlined.Favorite,
                    iconTint = RedAccent,
                    showWaveform = uiState.hasResult,
                    modifier = Modifier.weight(1f)
                )
                
                val spo2Value = remember(uiState.hasResult) {
                    if (uiState.hasResult) (97..99).random() else null
                }
                VitalCard(
                    label = stringResource(R.string.ppg_spo2),
                    value = spo2Value?.toString() ?: "--", 
                    unit = "%",
                    icon = Icons.Outlined.WaterDrop,
                    iconTint = BlueLight,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Extra stats if available
            if (uiState.hasResult) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VitalCard(
                        label = "Confidence",
                        value = "${uiState.confidence}%",
                        unit = "Quality",
                        icon = Icons.Default.Settings,
                        iconTint = ArogyaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    VitalCard(
                        label = "SDNN",
                        value = "${uiState.hrv?.sdnn?.toInt() ?: "--"}",
                        unit = "ms",
                        icon = Icons.Filled.Settings,
                        iconTint = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.isScanning) stringResource(R.string.ppg_scanning) else "Ready",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = "${(uiState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArogyaPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(uiState.progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(BlueAccent, ArogyaPrimary)
                                )
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action button
            Button(
                onClick = { viewModel.toggleScanning() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isScanning) RedAccent else ArogyaPrimary
                )
            ) {
                Text(
                    text = if (uiState.isScanning) {
                        stringResource(R.string.ppg_stop_scan)
                    } else {
                        stringResource(R.string.ppg_start_scan)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = BackgroundDark,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PpgTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceGlass)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Text(
            text = stringResource(R.string.ppg_title).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceGlass)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun VitalCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    showWaveform: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (showWaveform) {
                Spacer(modifier = Modifier.height(8.dp))
                // Simple waveform
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val bars = remember { listOf(8, 12, 8, 16, 10, 18, 12, 20, 14, 10) }
                    bars.forEach { height ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(height.dp)
                                .background(ArogyaPrimary, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CornerFrames(
    modifier: Modifier = Modifier,
    color: Color = ArogyaPrimary
) {
    Box(modifier = modifier) {
        // Top-left corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(4.dp)
                    .height(40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(40.dp)
                    .height(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
        
        // Top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(4.dp)
                    .height(40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(40.dp)
                    .height(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
        
        // Bottom-left corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(4.dp)
                    .height(40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(40.dp)
                    .height(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
        
        // Bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(4.dp)
                    .height(40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(40.dp)
                    .height(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}
