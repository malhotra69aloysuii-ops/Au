package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.domain.model.Game
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicBorderGray
import com.example.ui.theme.EpicCardShape
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicSurfaceGray
import com.example.ui.theme.EpicTextMuted
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showContextMenu by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 900f, dampingRatio = 0.8f),
        label = "card_press_spring"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(EpicCardShape)
            .background(EpicElevated)
            .border(BorderStroke(1.dp, EpicBorderGray), EpicCardShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
            .testTag("game_card_${game.id}")
    ) {
        // 3:4 Aspect Ratio Cover Image Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .background(EpicSurfaceGray)
        ) {
            if (!game.imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        ShimmerBox(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp)
                        )
                    },
                    error = {
                        ErrorCoverFallback(title = game.name)
                    }
                )
            } else {
                ErrorCoverFallback(title = game.name)
            }

            // Bottom-left mini badge icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, EpicBlue.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = EpicBlue,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Context Menu Dropdown
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                modifier = Modifier.background(EpicElevated)
            ) {
                DropdownMenuItem(
                    text = { Text("View Details", color = EpicTextPrimary) },
                    leadingIcon = {
                        Icon(Icons.Default.Info, contentDescription = null, tint = EpicBlue)
                    },
                    onClick = {
                        showContextMenu = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Copy Name", color = EpicTextPrimary) },
                    leadingIcon = {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = EpicTextMuted)
                    },
                    onClick = {
                        showContextMenu = false
                        clipboardManager.setText(AnnotatedString(game.name))
                        Toast.makeText(context, "Copied: ${game.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Title and Developer info below image
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = game.name,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                color = EpicTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!game.developer.isNullOrBlank()) {
                Text(
                    text = game.developer,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = EpicTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorCoverFallback(title: String) {
    val initial = title.firstOrNull()?.uppercaseChar()?.toString() ?: "E"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EpicSurfaceGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 44.sp,
            color = EpicBlue
        )
    }
}
