package com.arogyamitra.ui.marketplace

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun LoraDetailScreen(
    model: LoraModel,
    onBack: () -> Unit,
    onActivate: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Get hero image based on model ID
    val heroImageName = when (model.id) {
        "psych_care" -> "images/mindcomfort_hero.png"
        "radiology_pro" -> "images/xray_vision_hero.png"
        "insurance_assist" -> "images/insureguard_hero.png"
        "symbiosis_schedule" -> "images/symbiosis_hero.png"
        else -> null
    }
    
    val heroImage = remember(heroImageName) {
        heroImageName?.let {
            try {
                val inputStream = context.assets.open(it)
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    val modelColor = Color(model.color.toInt())

    Box(modifier = Modifier.fillMaxSize().background(MarketBgDark)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MarketSurfaceDark, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text("LoRA Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MarketSurfaceDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Share, null, tint = Color.White)
                }
            }
            
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(modelColor.copy(alpha = 0.2f))
            ) {
                if (heroImage != null) {
                    Image(
                        bitmap = heroImage,
                        contentDescription = model.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(modelColor.copy(alpha = 0.3f), modelColor.copy(alpha = 0.1f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (model.iconName) {
                            "psychology" -> Icons.Default.Face
                            "radiology" -> Icons.Default.RemoveRedEye
                            "verified_user" -> Icons.Default.VerifiedUser
                            "calendar_month" -> Icons.Default.DateRange
                            else -> Icons.Default.Favorite
                        }
                        Icon(icon, null, tint = modelColor, modifier = Modifier.size(80.dp))
                    }
                }
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MarketBgDark.copy(alpha = 0.8f)),
                                startY = 150f
                            )
                        )
                )
                
                // Rating badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(model.rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(" (120)", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Title & Author
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(model.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("By ${model.author}", color = MarketTextGray, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chips
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip(icon = Icons.Default.Verified, text = "Verified", color = MarketPrimary)
                DetailChip(text = "v1.0")
                model.tags.forEach { tag ->
                    DetailChip(text = tag)
                }
                DetailChip(text = model.size)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Description", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    model.description,
                    color = MarketTextGray,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Performance Metrics
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Performance", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        icon = Icons.Default.CheckCircle,
                        label = "ACCURACY",
                        value = "98.5%",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Default.Speed,
                        label = "SPEED",
                        value = "25ms",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        icon = Icons.Default.Analytics,
                        label = "F1 SCORE",
                        value = "0.97",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Default.Storage,
                        label = "PARAMS",
                        value = "7B",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Compatibility
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Compatibility", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                CompatibilityRow(
                    icon = Icons.Default.Code,
                    title = "Base Model",
                    subtitle = "Recommended foundation",
                    value = "Gemma-2B"
                )
                Spacer(modifier = Modifier.height(12.dp))
                CompatibilityRow(
                    icon = Icons.Default.Extension,
                    title = "Format",
                    subtitle = "Download types",
                    value = ".safetensors"
                )
            }
        }
        
        // Bottom Action Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            // Gradient fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MarketBgDark)
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MarketBgDark.copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PRICE", color = MarketTextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(model.price, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        if (model.price != "Free") {
                            Text(" / mo", color = MarketPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Button(
                    onClick = onActivate,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MarketPrimary),
                    shape = RoundedCornerShape(28.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    Text("Activate", color = MarketBgDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Download, null, tint = MarketBgDark)
                }
            }
        }
    }
}

@Composable
fun DetailChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = Color.White
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .background(MarketSurfaceDark, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MarketSurfaceDark, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MarketPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = MarketTextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CompatibilityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MarketSurfaceDark, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MarketBgDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MarketTextGray, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MarketTextGray, fontSize = 11.sp)
            }
        }
        Text(value, color = MarketPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
