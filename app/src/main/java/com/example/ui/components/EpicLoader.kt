package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicBluePressed
import com.example.ui.theme.EpicTextSecondary
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily

/**
 * Centered animated Epic-style mark:
 * Three diagonal blue streaks that draw in sequentially and pulse
 */
@Composable
fun EpicLoader(
    modifier: Modifier = Modifier,
    statusText: String = "LOADING YOUR LIBRARY..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "epic_loader_transition")

    val streak1Progress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_1"
    )

    val streak2Progress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_2"
    )

    val streak3Progress by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_3"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val strokeWidth = 8.dp.toPx()

            // Streak 1 (left diagonal streak)
            val start1 = Offset(size.width * 0.15f, size.height * 0.75f)
            val end1 = Offset(
                size.width * (0.15f + 0.3f * streak1Progress),
                size.height * (0.75f - 0.5f * streak1Progress)
            )
            drawLine(
                color = EpicBlue.copy(alpha = 0.6f + (0.4f * streak1Progress)),
                start = start1,
                end = end1,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Streak 2 (center diagonal streak - longer and brighter)
            val start2 = Offset(size.width * 0.35f, size.height * 0.85f)
            val end2 = Offset(
                size.width * (0.35f + 0.4f * streak2Progress),
                size.height * (0.85f - 0.7f * streak2Progress)
            )
            drawLine(
                color = EpicWhite.copy(alpha = 0.7f + (0.3f * streak2Progress)),
                start = start2,
                end = end2,
                strokeWidth = strokeWidth * 1.1f,
                cap = StrokeCap.Round
            )

            // Streak 3 (right diagonal streak)
            val start3 = Offset(size.width * 0.55f, size.height * 0.65f)
            val end3 = Offset(
                size.width * (0.55f + 0.3f * streak3Progress),
                size.height * (0.65f - 0.45f * streak3Progress)
            )
            drawLine(
                color = EpicBluePressed.copy(alpha = 0.5f + (0.5f * streak3Progress)),
                start = start3,
                end = end3,
                strokeWidth = strokeWidth * 0.9f,
                cap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = statusText.uppercase(),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = EpicTextSecondary,
            letterSpacing = 0.08.em
        )
    }
}
