package com.arogyamitra.ui.marketplace

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arogyamitra.data.LoraModel

// Colors
val MarketPrimary = Color(0xFF2BEE7C)
val MarketBgDark = Color(0xFF102218)
val MarketSurfaceDark = Color(0xFF1A2E24)
val MarketTextGray = Color(0xFF9CA3AF)

@Composable
fun MarketplaceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = MarketBgDark,
        bottomBar = {
            MarketplaceBottomBar(selectedTab) { selectedTab = it }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> StoreContent(
                    models = uiState.filteredModels,
                    searchQuery = uiState.searchQuery,
                    onSearchChange = viewModel::onSearchQueryChanged,
                    onSelectAdapter = { modelId ->
                        viewModel.activateAndNavigate(modelId) {
                            onNavigateToChat(viewModel.getSystemPromptById(modelId))
                        }
                    },
                    onInfoClick = onNavigateToDetail
                )
                1 -> LibraryContent(
                    models = uiState.models,
                    activeId = uiState.activeModelId,
                    onSelectAdapter = { modelId ->
                        viewModel.activateAndNavigate(modelId) {
                            onNavigateToChat(viewModel.getSystemPromptById(modelId))
                        }
                    },
                    onInfoClick = onNavigateToDetail
                )
                2 -> SettingsContent(onLogout = onLogout)
            }

            // Loading Overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MarketPrimary, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.loadingMessage ?: "Loading...",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreContent(
    models: List<LoraModel>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectAdapter: (String) -> Unit,
    onInfoClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("LoRA Store", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("AROGYA MITRA", color = MarketTextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MarketSurfaceDark, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = MarketPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MarketSurfaceDark, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Search, null, tint = MarketPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(MarketPrimary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search adapters...", color = MarketTextGray, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Clear,
                            null,
                            tint = MarketTextGray,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onSearchChange("") }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Featured Card - Top Rated
        item {
            Text("Featured", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            FeaturedCard(
                modelId = "psych_care",
                onSelect = { onSelectAdapter("psych_care") },
                onInfo = { onInfoClick("psych_care") }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Available Adapters
        item {
            Text("Available Adapters", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Adapter Grid
        items(models.chunked(2)) { rowModels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModels.forEach { model ->
                    Box(modifier = Modifier.weight(1f)) {
                        AdapterCard(
                            model = model,
                            onSelect = { onSelectAdapter(model.id) },
                            onInfo = { onInfoClick(model.id) }
                        )
                    }
                }
                if (rowModels.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (models.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No adapters found", color = MarketTextGray, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun FeaturedCard(modelId: String, onSelect: () -> Unit, onInfo: () -> Unit) {
    val context = LocalContext.current
    val heroImage = remember {
        try {
            val inputStream = context.assets.open("images/mindcomfort_hero.png")
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        } catch (e: Exception) { null }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1E3A5F), Color(0xFF0D2137))))
            .clickable { onSelect() }
    ) {
        // Background image
        if (heroImage != null) {
            Image(
                bitmap = heroImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
        }
        
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(MarketPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⭐ TOP RATED", color = MarketPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("MindComfort AI", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("4.9 rating • Mental Health", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text("Tap to activate", color = MarketPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("ⓘ More info", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp,
                        modifier = Modifier.clickable { onInfo() })
                }
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE91E63).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Face, null, tint = Color(0xFFE91E63), modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun AdapterCard(model: LoraModel, onSelect: () -> Unit, onInfo: () -> Unit) {
    val icon = when (model.iconName) {
        "psychology" -> Icons.Default.Face
        "radiology" -> Icons.Default.RemoveRedEye
        "verified_user" -> Icons.Default.VerifiedUser
        "calendar_month" -> Icons.Default.DateRange
        else -> Icons.Default.Favorite
    }
    val cardColor = Color(model.color.toInt())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(cardColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cardColor, modifier = Modifier.size(28.dp))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(model.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
        Text(model.author, color = MarketTextGray, fontSize = 11.sp, maxLines = 1)
        
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(model.rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Row {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable { onInfo() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Info, null, tint = MarketTextGray, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(MarketPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("USE", color = MarketBgDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LibraryContent(
    models: List<LoraModel>,
    activeId: String?,
    onSelectAdapter: (String) -> Unit,
    onInfoClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text("My Library", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Select an adapter to use", color = MarketTextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
        }

        items(models) { model ->
            LibraryItem(
                model = model,
                isActive = model.id == activeId,
                onSelect = { onSelectAdapter(model.id) },
                onInfo = { onInfoClick(model.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun LibraryItem(model: LoraModel, isActive: Boolean, onSelect: () -> Unit, onInfo: () -> Unit) {
    val icon = when (model.iconName) {
        "psychology" -> Icons.Default.Face
        "radiology" -> Icons.Default.RemoveRedEye
        "verified_user" -> Icons.Default.VerifiedUser
        "calendar_month" -> Icons.Default.DateRange
        else -> Icons.Default.Favorite
    }
    val cardColor = Color(model.color.toInt())
     
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isActive) MarketPrimary else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(20.dp)
            )
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .clickable { onSelect() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(cardColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cardColor, modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(model.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(model.size + " • " + model.tags.firstOrNull().orEmpty(), color = MarketTextGray, fontSize = 12.sp)
        }
        
        // Info button
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable { onInfo() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Info, null, tint = MarketTextGray, modifier = Modifier.size(18.dp))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .background(
                    if (isActive) MarketPrimary else Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isActive) "ACTIVE" else "USE",
                color = if (isActive) MarketBgDark else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SettingsContent(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MarketplaceBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MarketSurfaceDark.copy(alpha = 0.95f), RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(Icons.Outlined.ShoppingCart, "Store", selectedIndex == 0) { onSelect(0) }
            NavBarItem(Icons.Outlined.List, "Library", selectedIndex == 1) { onSelect(1) }
            NavBarItem(Icons.Outlined.Settings, "Settings", selectedIndex == 2) { onSelect(2) }
        }
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, label, tint = if (selected) MarketPrimary else MarketTextGray, modifier = Modifier.size(26.dp))
        Text(label, color = if (selected) MarketPrimary else MarketTextGray, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}
