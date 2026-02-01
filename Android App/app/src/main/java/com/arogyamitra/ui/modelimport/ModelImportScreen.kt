package com.arogyamitra.ui.modelimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arogyamitra.data.SavedModel
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.BackgroundDark
import com.arogyamitra.ui.theme.BorderGlass
import com.arogyamitra.ui.theme.GlowGreen
import com.arogyamitra.ui.theme.SurfaceGlass
import com.arogyamitra.ui.theme.TextSecondaryDark

import androidx.compose.runtime.LaunchedEffect
import com.arogyamitra.llm.LlmModelHelper

@Composable
fun ModelImportScreen(
    onNavigateBack: () -> Unit,
    onModelLoaded: () -> Unit,
    viewModel: ModelImportViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Auto-navigation when model loads successfully
    LaunchedEffect(uiState.navigateToChat) {
        if (uiState.navigateToChat) {
            onModelLoaded()
            viewModel.onNavigatedToChat()
        }
    }
    
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importModel(it) }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlowGreen.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 500f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Import AI Models",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Import Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ImportModelCard(
                        isImporting = uiState.isImporting,
                        onClick = { filePicker.launch("*/*") }
                    )
                }
                
                // Available Models Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE MODELS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondaryDark,
                            letterSpacing = 1.sp
                        )
                        
                        if (uiState.models.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${uiState.models.size} Loaded",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArogyaPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ArogyaPrimary)
                                )
                            }
                        }
                    }
                }
                
                // Model list
                if (uiState.models.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceGlass)
                                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No models imported yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryDark
                            )
                        }
                    }
                } else {
                    items(uiState.models) { model ->
                        val isLoaded = LlmModelHelper.hasInstance(model.id)
                        ModelCard(
                            model = model,
                            isSelected = model.id == uiState.selectedModelId,
                            isLoaded = isLoaded,
                            isLoading = model.id == uiState.loadingModelId,
                            onSelect = {
                                viewModel.loadModel(model.id)
                            },
                            onPlay = {
                                viewModel.loadModel(model.id)
                            },
                            onUnload = {
                                viewModel.unloadModel(model.id)
                            }
                        )
                    }
                }
                
                // Local Storage Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "LOCAL STORAGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondaryDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StorageOption(
                            icon = Icons.Default.Folder,
                            title = "Browse Files",
                            subtitle = "Select from device",
                            color = Color(0xFFFF9966),
                            onClick = { filePicker.launch("*/*") },
                            modifier = Modifier.weight(1f)
                        )
                        StorageOption(
                            icon = Icons.Default.History,
                            title = "Recent",
                            subtitle = "Last imports",
                            color = Color(0xFF9966FF),
                            onClick = { /* Show recent */ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
        
        // Error snackbar
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    Text(
                        text = "Dismiss",
                        color = ArogyaPrimary,
                        modifier = Modifier.clickable { viewModel.dismissError() }
                    )
                }
            ) {
                Text(uiState.error!!)
            }
        }
    }
}

@Composable
private fun ImportModelCard(
    isImporting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ArogyaPrimary.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        ArogyaPrimary.copy(alpha = 0.5f),
                        ArogyaPrimary.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = !isImporting) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    color = ArogyaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArogyaPrimary.copy(alpha = 0.2f))
                        .border(1.dp, ArogyaPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = ArogyaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                text = if (isImporting) "Importing..." else "Import New Model",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Supports .task, .tflite, .gguf",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun ModelCard(
    model: SavedModel,
    isSelected: Boolean,
    isLoaded: Boolean,
    isLoading: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onUnload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) ArogyaPrimary.copy(alpha = 0.1f) else SurfaceGlass,
        animationSpec = tween(300),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ArogyaPrimary.copy(alpha = 0.5f) else BorderGlass,
        animationSpec = tween(300),
        label = "borderColor"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { 
                if (isLoaded) onUnload() else onSelect() 
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Model icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLoaded) ArogyaPrimary.copy(alpha = 0.2f) else BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = if (isLoaded) ArogyaPrimary else TextSecondaryDark,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Model info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatSize(model.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
                if (isLoaded) {
                    Text(
                        text = "• Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArogyaPrimary
                    )
                }
            }
        }
        
        // Action button
        if (isLoading) {
            CircularProgressIndicator(
                color = ArogyaPrimary,
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp
            )
        } else if (isLoaded) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ArogyaPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Unload",
                    tint = BackgroundDark,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onUnload() }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Load",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StorageOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryDark
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.0f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.0f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
