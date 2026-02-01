package com.arogyamitra.ui.developer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.MarketBackgroundDark
import com.arogyamitra.ui.theme.MarketSurfaceDark

enum class DeveloperTab {
    MARKET, EXPLORE, ADD, EARNINGS, PROFILE
}

@Composable
fun DeveloperDashboardScreen(
    onLogout: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    var currentTab by remember { mutableStateOf(DeveloperTab.EARNINGS) }
    val uiState by viewModel.uiState.collectAsState()
    val myModels by viewModel.myModels.collectAsState()
    val filteredModels by viewModel.filteredModels.collectAsState()

    Scaffold(
        bottomBar = {
            DeveloperBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        containerColor = MarketBackgroundDark
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width })
                        .togetherWith(fadeOut() + slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width })
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    DeveloperTab.EARNINGS -> DeveloperEarningsScreen(
                        state = uiState, 
                        models = myModels,
                        onDeleteModel = viewModel::deleteModel
                    )
                    DeveloperTab.MARKET -> DeveloperMarketScreen(
                        uiState, 
                        filteredModels,
                        onSearchClick = { currentTab = DeveloperTab.EXPLORE }
                    )
                    DeveloperTab.EXPLORE -> DeveloperExploreScreen(
                        filteredModels, 
                        uiState.searchQuery, 
                        viewModel::onSearchQueryChanged,
                        uiState.selectedCategory,
                        viewModel::onCategorySelected
                    )
                    DeveloperTab.ADD -> DeveloperUploadScreen(
                        isUploading = uiState.isUploading,
                        uploadSuccess = uiState.uploadSuccess,
                        onUpload = viewModel::uploadModel,
                        onReset = viewModel::resetUploadState
                    )
                    DeveloperTab.PROFILE -> DeveloperProfileScreen(uiState, onLogout)
                }
            }
        }
    }
}

@Composable
fun DeveloperBottomBar(
    currentTab: DeveloperTab,
    onTabSelected: (DeveloperTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(80.dp) // Space for floating button
    ) {
        // Glass Background
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, CircleShape, spotColor = ArogyaPrimary.copy(alpha = 0.5f))
                .background(
                    color = Color(0xFF0F0F11).copy(alpha = 0.9f),
                    shape = CircleShape
                )
        )
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Market
            TabItem(
                icon = Icons.Default.Storefront,
                label = "Market",
                isSelected = currentTab == DeveloperTab.MARKET,
                onClick = { onTabSelected(DeveloperTab.MARKET) }
            )

            // Explore
            TabItem(
                icon = Icons.Default.Search,
                label = "Explore",
                isSelected = currentTab == DeveloperTab.EXPLORE,
                onClick = { onTabSelected(DeveloperTab.EXPLORE) }
            )
            
            // FAB Placeholder (Center)
            Box(modifier = Modifier.size(48.dp))

            // Earnings
            TabItem(
                icon = Icons.Default.ShowChart,
                label = "Earnings",
                isSelected = currentTab == DeveloperTab.EARNINGS,
                onClick = { onTabSelected(DeveloperTab.EARNINGS) }
            )
            
            // Profile
            TabItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentTab == DeveloperTab.PROFILE,
                onClick = { onTabSelected(DeveloperTab.PROFILE) }
            )
        }

        // Floating Action Button (Center)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp)
                .shadow(16.dp, CircleShape, spotColor = ArogyaPrimary)
                .background(ArogyaPrimary, CircleShape)
                .clickable { onTabSelected(DeveloperTab.ADD) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Upload",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun TabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}
