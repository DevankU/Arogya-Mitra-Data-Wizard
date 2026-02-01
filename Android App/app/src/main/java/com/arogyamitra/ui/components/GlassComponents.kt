package com.arogyamitra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arogyamitra.ui.theme.BorderDark
import com.arogyamitra.ui.theme.SurfaceGlass

/**
 * Glassmorphism card with blur effect and subtle border
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = SurfaceGlass,
    borderColor: Color = BorderDark,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Circular glassmorphism container
 */
@Composable
fun GlassCircle(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceGlass,
    borderColor: Color = BorderDark,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Ambient glow effect for background decoration
 */
@Composable
fun AmbientGlow(
    modifier: Modifier = Modifier,
    color: Color,
    blurRadius: Dp = 100.dp
) {
    Box(
        modifier = modifier
            .blur(blurRadius)
            .background(color, RoundedCornerShape(percent = 50))
    )
}

/**
 * Gradient background with mesh pattern
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(colors = colors)
        )
    ) {
        content()
    }
}
