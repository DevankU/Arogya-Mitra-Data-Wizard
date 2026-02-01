package com.arogyamitra.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

// S-curve easing for smooth animations (starts slow, speeds up, ends slow)
val SCurveEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
val SCurveEasingReverse = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

// Animation durations
const val ANIMATION_DURATION_SHORT = 200
const val ANIMATION_DURATION_MEDIUM = 300
const val ANIMATION_DURATION_LONG = 500

// Enter animations
fun slideInFromRight(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing)
) + fadeIn(animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing))

fun slideInFromBottom(): EnterTransition = slideInVertically(
    initialOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing)
) + fadeIn(animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing))

fun fadeInSmooth(): EnterTransition = fadeIn(
    animationSpec = tween(durationMillis = ANIMATION_DURATION_LONG, easing = SCurveEasing)
)

// Exit animations
fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing)
) + fadeOut(animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing))

fun slideOutToBottom(): ExitTransition = slideOutVertically(
    targetOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing)
) + fadeOut(animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing))

fun fadeOutSmooth(): ExitTransition = fadeOut(
    animationSpec = tween(durationMillis = ANIMATION_DURATION_MEDIUM, easing = SCurveEasing)
)
