package com.arogyamitra.ui.developer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arogyamitra.data.LoraModel
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.MarketSurfaceDark
import androidx.compose.foundation.clickable

@Composable
fun DeveloperEarningsScreen(
    state: DeveloperUiState,
    models: List<LoraModel>,
    onDeleteModel: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
    ) {
        // Hero Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Earnings",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "₹${state.earnings}",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Withdraw */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ArogyaPrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Withdraw Funds", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Chart Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF181A1B).copy(alpha = 0.5f)) // Surface Dark
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "SALES TREND",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = ArogyaPrimary, modifier = Modifier.size(16.dp))

                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+12.5%", color = ArogyaPrimary, fontWeight = FontWeight.Bold)
                                Text(" vs last 7 days", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Chart
                    EarningsChart(data = state.salesTrend)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach {
                            Text(text = it, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Stats
        item {
            LazyRow(
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { StatCard(Icons.Default.Download, "${state.downloads / 1000.0}k", "Total Downloads", Color(0xFF3B82F6)) }
                item { StatCard(Icons.Default.Group, "${state.activeUsers}", "Active Users", Color(0xFFA855F7)) }
                item { StatCard(Icons.Default.Star, "${state.rating}", "Avg Rating", Color(0xFFEAB308)) }
            }
        }

        // Adapters Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Adapters",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See All",
                    color = ArogyaPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { /* See All */ }
                )
            }
        }

        // Adapters List
        items(models) { model ->
            AdapterListItem(
                model = model,
                onDelete = { onDeleteModel(model.id) }
            )
        }
    }
}

@Composable
fun EarningsChart(data: List<Float>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val path = Path()
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)
        
        // Move to start
        path.moveTo(0f, height - (data.first() * height)) // Invert Y (0 is top)
        
        // Draw Quadratic Bezier curve
        for (i in 0 until data.size - 1) {
            val p1 = Offset(i * spacing, height - (data[i] * height))
            val p2 = Offset((i + 1) * spacing, height - (data[i + 1] * height))
            
            val controlPoint1 = Offset(p1.x + spacing / 2, p1.y)
            val controlPoint2 = Offset(p2.x - spacing / 2, p2.y)
            
            path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
        }
        
        // Gradient Fill
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(ArogyaPrimary.copy(alpha = 0.2f), Color.Transparent)
            )
        )
        
        // Stroke
        drawPath(
            path = path,
            color = ArogyaPrimary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatCard(icon: ImageVector, value: String, label: String, color: Color) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF181A1B).copy(alpha = 0.5f)) // Glassy
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun AdapterListItem(model: LoraModel, onDelete: () -> Unit = {}) {
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF181A1B).copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with color
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cardColor, modifier = Modifier.size(28.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = model.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = model.price,
                        color = if (model.price == "Free") Color(0xFF90CAF9) else ArogyaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text(" ${model.size}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Text(" ${model.rating}", color = Color.Gray, fontSize = 12.sp)
            }
        }
        
        // Delete button for user uploads
        if (model.isUserUploaded) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
