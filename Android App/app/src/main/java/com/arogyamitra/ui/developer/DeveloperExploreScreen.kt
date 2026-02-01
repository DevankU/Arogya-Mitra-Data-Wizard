package com.arogyamitra.ui.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arogyamitra.data.LoraModel
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.MarketSurfaceDark
import androidx.compose.foundation.border
import androidx.compose.material3.IconButton

import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VerifiedUser

@Composable
fun DeveloperExploreScreen(
    models: List<LoraModel>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Sticky Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F11)) // Background Dark
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MarketSurfaceDark)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search Adapters...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, null, tint = Color.Gray)
                    }
                }
            }
        }

        // Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                listOf("All", "Cardiology", "Radiology", "Neurology", "Oncology", "Pediatrics").forEach { cat ->
                    item {
                        FilterChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { onCategorySelected(cat) }
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("Trending Adapters", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("View All", color = ArogyaPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // List
        items(models) { model ->
            ExploreItem(model)
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) ArogyaPrimary else MarketSurfaceDark.copy(alpha = 0.5f))
            .border(
                1.dp, 
                if (isSelected) ArogyaPrimary else Color.White.copy(alpha = 0.1f), 
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExploreItem(model: LoraModel) {
    val icon = when (model.iconName) {
        "psychology" -> Icons.Default.Face
        "radiology" -> Icons.Default.RemoveRedEye
        "verified_user" -> Icons.Default.VerifiedUser
        "calendar_month" -> Icons.Default.DateRange
        else -> Icons.Default.Favorite
    }
    val cardColor = Color(model.color.toInt())
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MarketSurfaceDark.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Icon with color
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = cardColor, modifier = Modifier.size(36.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.tags.firstOrNull() ?: "General",
                            color = ArogyaPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(ArogyaPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                        Text(" ${model.rating}", color = Color.Gray, fontSize = 10.sp)
                    }
                    
                    Text(
                        text = model.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = model.description,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Price", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(model.price, color = if(model.price == "Free") ArogyaPrimary else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { /* Download */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Download", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
