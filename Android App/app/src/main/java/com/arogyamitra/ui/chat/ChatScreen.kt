package com.arogyamitra.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arogyamitra.ui.chat.components.ChatMessage
import com.arogyamitra.ui.chat.components.DateDivider
import com.arogyamitra.ui.chat.components.MessageBubble
import com.arogyamitra.ui.chat.components.MessageSide
import com.arogyamitra.ui.chat.components.PpgResultCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.BackgroundDark
import com.arogyamitra.ui.theme.BorderGlass
import com.arogyamitra.ui.theme.GlowGreen
import com.arogyamitra.ui.theme.SurfaceGlass
import com.arogyamitra.ui.theme.TextSecondaryDark

import androidx.compose.material.icons.filled.Stop
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import com.arogyamitra.data.LoraModel
import com.arogyamitra.data.LoraRepository
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToPpg: () -> Unit,
    onNavigateToModelImport: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToMarketplace: () -> Unit, // Added
    viewModel: ChatViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var showMenu by remember { mutableStateOf(false) }
    var showAdapterSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val adapters = remember { LoraRepository.models }
    var activeAdapterId by remember { mutableStateOf<String?>(null) }
    
    // Scroll to bottom
    LaunchedEffect(uiState.messages.size, uiState.streamingMessage) {
        if (uiState.messages.isNotEmpty() || uiState.streamingMessage.isNotBlank()) {
            listState.animateScrollToItem(
                index = uiState.messages.size + if (uiState.streamingMessage.isNotBlank()) 1 else 0
            )
        }
    }
    
    // Handle navigation triggers
    LaunchedEffect(uiState.navigateToPpg) {
        if (uiState.navigateToPpg) {
            onNavigateToPpg()
            viewModel.onPpgNavigationConsumed()
        }
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
                            GlowGreen.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 500f
                    )
                )
                .blur(100.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                )
        ) {
            // Top Bar
            TopBar(
                modelName = uiState.currentModel?.name ?: "No Model",
                isModelLoaded = uiState.isModelLoaded,
                isLoading = uiState.isLoading,
                showMenu = showMenu,
                onMenuClick = { showMenu = !showMenu },
                onModelClick = onNavigateToModelImport,
                onMarketplaceClick = onNavigateToMarketplace,
                onAdaptersClick = {
                    showMenu = false
                    showAdapterSheet = true
                },
                onDismissMenu = { showMenu = false },
                onViewWelcome = {
                    showMenu = false
                    onNavigateToWelcome()
                },
                onClearChat = {
                    showMenu = false
                    viewModel.clearChat()
                }
            )
            
            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.messages.isNotEmpty()) {
                    item {
                        DateDivider(date = "Today")
                    }
                }
                
                items(uiState.messages, key = { it.id }) { message ->
                    if (message.ppgData != null) {
                        val timeString = remember(message.timestamp) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
                        }
                        
                        PpgResultCard(
                            bpm = message.ppgData.bpm,
                            bpmHistory = message.ppgData.history,
                            timestamp = timeString,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                        
                        if (message.content.isNotBlank() && message.content != "Analyzing your vitals...") {
                            MessageBubble(message = message)
                        }
                    } else {
                        MessageBubble(message = message)
                    }
                }
                
                if (uiState.streamingMessage.isNotBlank()) {
                    item {
                        MessageBubble(
                            message = ChatMessage(
                                id = "streaming",
                                content = uiState.streamingMessage,
                                side = MessageSide.AI,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                
                if (uiState.isGenerating && uiState.streamingMessage.isBlank()) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            
            // Input Bar
            InputBar(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.text.isNotBlank()) {
                        viewModel.sendMessage(inputText.text)
                        inputText = TextFieldValue("")
                    }
                },
                onStop = {
                    viewModel.stopGeneration()
                },
                onPpgClick = onNavigateToPpg,
                isGenerating = uiState.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
            )
        }
        
        // Adapter Bottom Sheet
        if (showAdapterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAdapterSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF1A2E24)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "LoRA Adapters",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Select an adapter to customize AI responses",
                        color = TextSecondaryDark,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    adapters.forEach { adapter ->
                        AdapterItem(
                            adapter = adapter,
                            isActive = activeAdapterId == adapter.id,
                            onActivate = {
                                if (activeAdapterId == adapter.id) {
                                    // Deactivate
                                    activeAdapterId = null
                                    viewModel.resetWithPersona("")
                                } else {
                                    // Activate
                                    activeAdapterId = adapter.id
                                    viewModel.resetWithPersona(adapter.systemPrompt)
                                }
                                showAdapterSheet = false
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    modelName: String,
    isModelLoaded: Boolean,
    isLoading: Boolean,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onModelClick: () -> Unit,
    onMarketplaceClick: () -> Unit,
    onAdaptersClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onViewWelcome: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShrunk by remember { mutableStateOf(false) }
    
    LaunchedEffect(isModelLoaded) {
        if (isModelLoaded) {
            delay(3000)
            isShrunk = true
        } else {
            isShrunk = false
        }
    }
    
    val textMaxWidth by animateDpAsState(
        targetValue = if (isShrunk) 150.dp else 300.dp,
        animationSpec = tween(1000),
        label = "textWidth"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Model selector
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceGlass)
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                .clickable { onModelClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ArogyaPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = ArogyaPrimary,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = ArogyaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.width(textMaxWidth)) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isModelLoaded) "Active" else if (isLoading) "Loading..." else "Tap to import",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isModelLoaded) ArogyaPrimary else TextSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Marketplace Button
        IconButton(onClick = onMarketplaceClick) {
            Icon(
                imageVector = Icons.Default.Add, // Standard add icon for "Get More"
                contentDescription = "Marketplace",
                tint = ArogyaPrimary
            )
        }
        
        // Menu button
        Box {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(SurfaceGlass)
            ) {
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = null,
                                tint = ArogyaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("View Welcome", color = Color.White)
                        }
                    },
                    onClick = onViewWelcome
                )
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = ArogyaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Change Model", color = Color.White)
                        }
                    },
                    onClick = { 
                        onDismissMenu()
                        onModelClick() 
                    }
                )
                DropdownMenuItem(
                    text = { 
                        Text("Clear Chat", color = Color.White)
                    },
                    onClick = onClearChat
                )
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                            Icon(Icons.Default.Extension, null, tint = ArogyaPrimary, modifier = Modifier.size(20.dp))
                            Text("Manage Adapters", color = Color.White) 
                        } 
                    },
                    onClick = onAdaptersClick
                )
                 DropdownMenuItem(
                    text = { 
                         Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                             Icon(Icons.Default.Add, null, tint = ArogyaPrimary, modifier = Modifier.size(20.dp)); 
                             Text("LoRA Marketplace", color = Color.White) 
                         } 
                    },
                    onClick = { onDismissMenu(); onMarketplaceClick() }
                )
            }
        }
    }
}

// ... InputBar and TypingIndicator remain the same
@Composable
private fun InputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onPpgClick: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onPpgClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceGlass)
                .border(1.dp, BorderGlass, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Vitals",
                tint = ArogyaPrimary
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceGlass)
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                cursorBrush = SolidColor(ArogyaPrimary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.text.isEmpty()) {
                            Text(
                                text = "Ask me anything about health...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondaryDark
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        
        IconButton(
            onClick = if (isGenerating) onStop else onSend,
            enabled = isGenerating || value.text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isGenerating || value.text.isNotBlank()) 
                        ArogyaPrimary 
                    else 
                        SurfaceGlass
                )
        ) {
            Icon(
                imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                contentDescription = if (isGenerating) "Stop" else "Send",
                tint = if (isGenerating || value.text.isNotBlank()) 
                    BackgroundDark 
                else 
                    TextSecondaryDark
            )
        }
    }
}

@Composable
private fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { index ->
            val alpha by animateFloatAsState(
                targetValue = 1f,
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ArogyaPrimary.copy(alpha = alpha * 0.7f))
            )
        }
    }
}

@Composable
private fun AdapterItem(
    adapter: LoraModel,
    isActive: Boolean,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (adapter.iconName) {
        "psychology" -> Icons.Default.Face
        "radiology" -> Icons.Default.RemoveRedEye
        "verified_user" -> Icons.Default.VerifiedUser
        "calendar_month" -> Icons.Default.DateRange
        else -> Icons.Default.Favorite
    }
    val cardColor = Color(adapter.color.toInt())
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) cardColor.copy(alpha = 0.15f) else SurfaceGlass)
            .border(
                1.dp,
                if (isActive) cardColor else BorderGlass,
                RoundedCornerShape(16.dp)
            )
            .clickable { onActivate() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cardColor, modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = adapter.title,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = adapter.description,
                color = TextSecondaryDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        if (isActive) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Active",
                tint = ArogyaPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
