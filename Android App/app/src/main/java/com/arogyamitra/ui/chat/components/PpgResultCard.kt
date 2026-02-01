package com.arogyamitra.ui.chat.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.SurfaceGlass
import com.arogyamitra.ui.theme.TextSecondaryDark
import kotlin.random.Random

/**
 * Glassmorphic card displaying PPG analysis results with a dynamic waveform graph.
 */
@Composable
fun PpgResultCard(
    bpm: Int,
    bpmHistory: List<Double>, // The changing values of BPM
    timestamp: String,
    modifier: Modifier = Modifier,
    onDetailsClick: () -> Unit = {}
) {
    // Pulse animation for the "Live" badge and icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A2C23)) // Surface Dark matching HTML
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        // Background Gradient Decoration (top-right blur)
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = ArogyaPrimary.copy(alpha = 0.05f),
                radius = size.width * 0.4f,
                center = Offset(size.width, 0f)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = ArogyaPrimary.copy(alpha = alpha),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PPG Analysis",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live Badge
                Box(
                    modifier = Modifier
                        .background(ArogyaPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, ArogyaPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ANALYZED", // Changed to ANALYZED since it's a past result
                        color = ArogyaPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Graph + BPM
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Waveform Graph
                // We use the bpmHistory to draw relative bar heights
                WaveformGraph(
                    data = bpmHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                // Large BPM Display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = bpm.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interpretation text based on BPM
                val statusText = when {
                     bpm < 60 -> "Resting heart rate is low."
                     bpm in 60..100 -> "Heart rate is normal."
                     else -> "Heart rate is slightly elevated."
                }
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.clickable { onDetailsClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Details",
                        color = ArogyaPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ArogyaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WaveformGraph(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    // If data is empty, generate some fake visual data for aesthetics
    val displayData = if (data.isEmpty()) {
        List(20) { Random.nextDouble(0.3, 1.0) }
    } else {
        // Normalize data to 0.1..1.0 range
        val min = data.minOrNull() ?: 0.0
        val max = data.maxOrNull() ?: 100.0
        val range = (max - min).coerceAtLeast(1.0)
        data.takeLast(25).map { 
             0.2 + ((it - min) / range) * 0.8
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween, // Distribute evenly
        verticalAlignment = Alignment.Bottom
    ) {
        displayData.forEach { heightRatio ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 1.dp) // Gap
                    .height((48 * heightRatio).dp) // Scaled height
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ArogyaPrimary,
                                ArogyaPrimary.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}
