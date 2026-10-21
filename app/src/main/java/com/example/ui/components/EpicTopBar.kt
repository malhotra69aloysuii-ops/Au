package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicCarbon
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily

@Composable
fun EpicTopBar(
    title: String = "LIBRARY",
    displayName: String = "Epic Gamer",
    isRefreshing: Boolean = false,
    onSyncClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rotation by if (isRefreshing) {
        val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing)
            ),
            label = "spin_angle"
        )
    } else {
        rememberInfiniteTransition(label = "idle").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(animation = tween(1000)),
            label = "idle_angle"
        )
    }

    val initialLetter = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "E"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with EpicBlue ring
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EpicCarbon)
                    .border(2.dp, EpicBlue, CircleShape)
                    .clickable { onSettingsClick() }
                    .testTag("topbar_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialLetter,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = EpicWhite
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main Title
            Text(
                text = title.uppercase(),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = 0.04.em,
                color = EpicTextPrimary,
                modifier = Modifier.weight(1f)
            )

            // Sync Button with spin animation
            IconButton(
                onClick = onSyncClick,
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("sync_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Library",
                    tint = if (isRefreshing) EpicBlue else EpicWhite,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = EpicWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
