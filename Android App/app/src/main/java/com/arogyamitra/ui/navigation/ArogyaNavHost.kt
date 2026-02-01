package com.arogyamitra.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.arogyamitra.data.ModelRepository
import com.arogyamitra.ui.chat.ChatScreen
import com.arogyamitra.ui.chat.ChatViewModel
import com.arogyamitra.ui.chat.components.PpgData
import com.arogyamitra.ui.modelimport.ModelImportScreen
import com.arogyamitra.ui.onboarding.WelcomeWizard
import com.arogyamitra.ui.ppg.PpgScreen
import com.arogyamitra.ui.splash.SplashScreen
import com.arogyamitra.ui.theme.SCurveEasing
import com.arogyamitra.ui.login.LoginScreen
import com.arogyamitra.ui.marketplace.MarketplaceScreen
import com.arogyamitra.ui.developer.DeveloperDashboardScreen
import com.arogyamitra.ui.marketplace.LoraDetailScreen
import com.arogyamitra.data.LoraRepository

private const val ANIMATION_DURATION = 400

@Composable
fun ArogyaNavHost(
    modelRepository: ModelRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val isFirstRun = modelRepository.isFirstRun()
    val hasModels = modelRepository.hasModels()
    
    // Determine start destination based on first run and model status
    val startDestination: Any = SplashRoute
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(ANIMATION_DURATION, easing = SCurveEasing)
            ) + fadeIn(
                animationSpec = tween(ANIMATION_DURATION / 2)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(ANIMATION_DURATION, easing = SCurveEasing)
            ) + fadeOut(
                animationSpec = tween(ANIMATION_DURATION / 2)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(ANIMATION_DURATION, easing = SCurveEasing)
            ) + fadeIn(
                animationSpec = tween(ANIMATION_DURATION / 2)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(ANIMATION_DURATION, easing = SCurveEasing)
            ) + fadeOut(
                animationSpec = tween(ANIMATION_DURATION / 2)
            )
        }
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onSplashComplete = {
                    if (isFirstRun) {
                        navController.navigate(WelcomeRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    } else if (!hasModels) {
                        navController.navigate(ModelImportRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ChatRoute()) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable<WelcomeRoute> {
            WelcomeWizard(
                onComplete = {
                    modelRepository.setFirstRunComplete()
                    navController.navigate(ModelImportRoute) {
                        popUpTo(WelcomeRoute) { inclusive = true }
                    }
                },
                onSkip = {
                    modelRepository.setFirstRunComplete()
                    navController.navigate(ModelImportRoute) {
                        popUpTo(WelcomeRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<ModelImportRoute> {
            ModelImportScreen(
                onNavigateBack = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                },
                onModelLoaded = {
                    navController.navigate(ChatRoute()) {
                        popUpTo(ModelImportRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<ChatRoute> { entry ->
            val viewModel: ChatViewModel = hiltViewModel()
            val args = entry.toRoute<ChatRoute>()
            
            // Handle PPG Data
            val ppgResult = entry.savedStateHandle.get<PpgData>("ppg_data")
            LaunchedEffect(ppgResult) {
                if (ppgResult != null) {
                    viewModel.handlePpgResult(ppgResult)
                    entry.savedStateHandle.remove<PpgData>("ppg_data")
                }
            }
            
            // Handle System Prompt (Context Switch)
            LaunchedEffect(args.systemPrompt) {
                if (!args.systemPrompt.isNullOrEmpty()) {
                    viewModel.resetWithPersona(args.systemPrompt)
                }
            }

            ChatScreen(
                onNavigateToPpg = {
                    navController.navigate(PpgRoute)
                },
                onNavigateToModelImport = {
                    navController.navigate(ModelImportRoute)
                },
                onNavigateToWelcome = {
                    navController.navigate(WelcomeRoute)
                },
                onNavigateToMarketplace = {
                    navController.navigate(LoginRoute)
                },
                viewModel = viewModel
            )
        }
        
        composable<PpgRoute> {
            PpgScreen(
                onNavigateBack = { result ->
                     if (result != null) {
                         navController.previousBackStackEntry
                             ?.savedStateHandle
                             ?.set("ppg_data", result)
                     }
                    navController.popBackStack()
                }
            )
        }
        
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { isDev ->
                    if (isDev) {
                        navController.navigate(DeveloperRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    } else {
                        navController.navigate(MarketplaceRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable<MarketplaceRoute> {
            MarketplaceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { systemPrompt ->
                    navController.navigate(ChatRoute(systemPrompt = systemPrompt)) {
                        popUpTo(ChatRoute()) { 
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToDetail = { modelId ->
                    navController.navigate(LoraDetailRoute(modelId = modelId))
                },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(MarketplaceRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<DeveloperRoute> {
            DeveloperDashboardScreen(
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(DeveloperRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<LoraDetailRoute> { entry ->
            val args = entry.toRoute<LoraDetailRoute>()
            val model = LoraRepository.models.find { it.id == args.modelId }
            
            if (model != null) {
                LoraDetailScreen(
                    model = model,
                    onBack = { navController.popBackStack() },
                    onActivate = {
                        navController.navigate(ChatRoute(systemPrompt = model.systemPrompt)) {
                            popUpTo(ChatRoute()) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
