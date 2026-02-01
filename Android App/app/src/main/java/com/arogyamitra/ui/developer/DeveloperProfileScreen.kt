package com.arogyamitra.ui.developer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight

import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.MarketSurfaceDark
import androidx.compose.foundation.layout.Arrangement

@Composable
fun DeveloperProfileScreen(
    state: DeveloperUiState,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MarketSurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Alex Developer", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("alex.dev@arogyamitra.ai", color = Color.Gray, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Edit */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MarketSurfaceDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Edit Profile", color = Color.White)
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .background(MarketSurfaceDark, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("12", "Adapters")
                ProfileStat("4.8", "Rating")
                ProfileStat("12.5k", "Downloads")
            }
        }

        // Settings Section
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Settings", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                
                SettingItem(Icons.Default.Notifications, "Notifications", true)
                Spacer(modifier = Modifier.height(16.dp))
                SettingItem(Icons.Default.Security, "Privacy & Security")
                Spacer(modifier = Modifier.height(16.dp))
                SettingItem(Icons.Default.Help, "Help & Support")
                Spacer(modifier = Modifier.height(16.dp))
                SettingItem(Icons.Default.Info, "About Arogya Mitra")
            }
        }

        // Logout
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFEF4444))

                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, hasSwitch: Boolean = false) {
    var isChecked by remember { mutableStateOf(true) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if(hasSwitch) isChecked = !isChecked },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MarketSurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        
        if (hasSwitch) {
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ArogyaPrimary,
                    checkedTrackColor = ArogyaPrimary.copy(alpha = 0.5f)
                )
            )
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)

        }

    }
}
