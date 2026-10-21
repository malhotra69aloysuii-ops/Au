package com.example.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Epic Games Signature Animated Wallpaper:
 * Base: deep vertical gradient from EpicVoid (#0A0A0A) to #141414
 * Overlay: drifting diagonal aurora wave in EpicBlue (6-10% opacity)
 */
@Composable
fun EpicAnimatedWallpaper(
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_transition")
    val driftAnim by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_drift"
    )

    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = -scrollOffset * 0.15f
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Base vertical gradient
            val baseGradient = Brush.verticalGradient(
                colors = listOf(
                    EpicVoid,
                    Color(0xFF101010),
                    Color(0xFF141414),
                    EpicVoid
                )
            )
            drawRect(brush = baseGradient, size = size)

            // 2. Slow-drifting diagonal aurora streak in EpicBlue at 6-10% opacity
            val auroraCenterY = height * driftAnim
            val auroraPath = Path().apply {
                moveTo(-width * 0.2f, auroraCenterY - height * 0.25f)
                cubicTo(
                    width * 0.3f, auroraCenterY - height * 0.15f,
                    width * 0.7f, auroraCenterY + height * 0.25f,
                    width * 1.3f, auroraCenterY + height * 0.1f
                )
                lineTo(width * 1.3f, auroraCenterY + height * 0.55f)
                cubicTo(
                    width * 0.7f, auroraCenterY + height * 0.7f,
                    width * 0.3f, auroraCenterY + height * 0.3f,
                    -width * 0.2f, auroraCenterY + height * 0.2f
                )
                close()
            }

            val auroraBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    EpicBlue.copy(alpha = pulseAnim * 0.5f),
                    EpicBlue.copy(alpha = pulseAnim),
                    EpicBluePressed.copy(alpha = pulseAnim * 0.6f),
                    Color.Transparent
                ),
                start = Offset(0f, auroraCenterY - height * 0.2f),
                end = Offset(width, auroraCenterY + height * 0.4f)
            )

            drawPath(path = auroraPath, brush = auroraBrush)

            // Secondary subtle glow streak
            val secondPath = Path().apply {
                moveTo(width * 1.2f, auroraCenterY * 0.6f - height * 0.1f)
                cubicTo(
                    width * 0.8f, auroraCenterY * 0.6f + height * 0.2f,
                    width * 0.2f, auroraCenterY * 0.6f - height * 0.1f,
                    -width * 0.2f, auroraCenterY * 0.6f + height * 0.15f
                )
                lineTo(-width * 0.2f, auroraCenterY * 0.6f + height * 0.3f)
                cubicTo(
                    width * 0.2f, auroraCenterY * 0.6f + height * 0.05f,
                    width * 0.8f, auroraCenterY * 0.6f + height * 0.35f,
                    width * 1.2f, auroraCenterY * 0.6f + height * 0.2f
                )
                close()
            }

            val secondBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    EpicBlue.copy(alpha = pulseAnim * 0.4f),
                    Color.Transparent
                ),
                start = Offset(width, auroraCenterY * 0.6f),
                end = Offset(0f, auroraCenterY * 0.6f + height * 0.2f)
            )

            drawPath(path = secondPath, brush = secondBrush)
        }

        content()
    }
}
