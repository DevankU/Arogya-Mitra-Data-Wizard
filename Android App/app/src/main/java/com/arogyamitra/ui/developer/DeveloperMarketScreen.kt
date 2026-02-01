package com.arogyamitra.ui.developer

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arogyamitra.data.LoraModel
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.MarketSurfaceDark

@Composable
fun DeveloperMarketScreen(
    state: DeveloperUiState,
    models: List<LoraModel>,
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("LoRA Store", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Arogya Mitra", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MarketSurfaceDark)
                        .border(1.dp, Color.White.copy(alpha=0.1f), CircleShape)
                        .clickable { /* Profile */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountCircle, null, tint = ArogyaPrimary)
                }
            }
        }

        // Search Bar - Clickable to navigate to Explore
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MarketSurfaceDark)
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = ArogyaPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Tap to search...", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Tune, null, tint = Color.Gray)
                }
            }
        }

        // Filters
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ArogyaPrimary)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("All", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                listOf("Cardiology", "Neurology", "Radiology", "Pediatrics").forEach { category ->
                    item {
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MarketSurfaceDark)
                                .border(1.dp, Color.White.copy(alpha=0.1f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(category, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Featured Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Featured", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("See All", color = ArogyaPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Rated Card
                    item {
                        FeaturedAdapterCard(
                            title = "MindComfort AI",
                            subtitle = "4.9 rating • Mental Health",
                            badge = "⭐ TOP RATED",
                            badgeColor = ArogyaPrimary,
                            bgGradient = listOf(Color(0xFF1E3A5F), Color(0xFF0D2137)),
                            icon = Icons.Default.Face,
                            iconColor = Color(0xFFE91E63),
                            imagePath = "images/mindcomfort_hero.png"
                        )
                    }
                    // Rising Card
                    item {
                        FeaturedAdapterCard(
                            title = "X-Ray Vision Pro",
                            subtitle = "4.8 rating • Radiology",
                            badge = "🚀 RISING",
                            badgeColor = Color(0xFF3B82F6),
                            bgGradient = listOf(Color(0xFF1A2F4A), Color(0xFF0D1A2A)),
                            icon = Icons.Default.RemoveRedEye,
                            iconColor = Color(0xFF4CAF50),
                            imagePath = "images/xray_vision_hero.png"
                        )
                    }
                }
            }
        }

        // Grid Title
        item {
            Text(
                "Specialized Adapters",
                color = Color.White, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Grid Items
        items(models.chunked(2)) { rowModels ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (model in rowModels) {
                    Box(modifier = Modifier.weight(1f)) {
                        SpecializedCard(model)
                    }
                }
                if (rowModels.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FeaturedAdapterCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    bgGradient: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    imagePath: String
) {
    val context = LocalContext.current
    val heroImage = remember(imagePath) {
        try {
            val inputStream = context.assets.open(imagePath)
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        } catch (e: Exception) { null }
    }
    
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(bgGradient))
    ) {
        // Background image
        if (heroImage != null) {
            Image(
                bitmap = heroImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.35f
            )
        }
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(bgGradient[0].copy(alpha = 0.9f), Color.Transparent),
                        startX = 0f,
                        endX = 400f
                    )
                )
        )
        
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(badge, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun SpecializedCard(model: LoraModel) {
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with proper color
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(cardColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cardColor, modifier = Modifier.size(28.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(model.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(model.author, color = Color.Gray, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))

        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                Text(" ${model.rating}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ArogyaPrimary)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(model.price, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}
