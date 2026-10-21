package com.example.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Game
import com.example.domain.repository.LibraryRepository
import com.example.ui.theme.EpicAnimatedWallpaper
import com.example.ui.theme.EpicBorderGray
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicSuccess
import com.example.ui.theme.EpicSurfaceGray
import com.example.ui.theme.EpicTextMuted
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicTextSecondary
import com.example.ui.theme.EpicVoid
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GameDetailScreen(
    gameId: String,
    libraryRepository: LibraryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val game by libraryRepository.getGameById(gameId).collectAsState(initial = null)
    val scrollState = rememberScrollState()

    EpicAnimatedWallpaper(
        scrollOffset = scrollState.value.toFloat(),
        modifier = modifier.fillMaxSize()
    ) {
        if (game == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading game details...",
                    fontFamily = InterFontFamily,
                    color = EpicTextSecondary
                )
            }
        } else {
            val currentGame = game!!
            val formattedDate = remember(currentGame.ownedSince) {
                if (currentGame.ownedSince > 0) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(currentGame.ownedSince))
                } else {
                    "In Library"
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                ) {
                    // Full-bleed hero banner with parallax
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 11f)
                            .graphicsLayer {
                                translationY = scrollState.value * 0.4f
                            }
                    ) {
                        if (!currentGame.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentGame.imageUrl,
                                contentDescription = currentGame.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(EpicSurfaceGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentGame.name.firstOrNull()?.toString() ?: "E",
                                    fontSize = 72.sp,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EpicBorderGray
                                )
                            }
                        }

                        // Gradient fade to black at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            EpicVoid.copy(alpha = 0.8f),
                                            EpicVoid
                                        )
                                    )
                                )
                        )
                    }

                    // Content Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        // "OWNED" Badge in EpicSuccess
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EpicSuccess.copy(alpha = 0.15f))
                                .border(1.dp, EpicSuccess, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EpicSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OWNED IN LIBRARY",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.08.em,
                                    color = EpicSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Game Title
                        Text(
                            text = currentGame.name,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
                            color = EpicTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Developer info
                        if (!currentGame.developer.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CorporateFare,
                                    contentDescription = null,
                                    tint = EpicTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentGame.developer,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = EpicTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Metadata Cards Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Namespace info
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EpicElevated)
                                    .border(1.dp, EpicBorderGray, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = EpicTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NAMESPACE",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = EpicTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentGame.namespace,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = EpicTextPrimary
                                )
                            }

                            // Added Date
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EpicElevated)
                                    .border(1.dp, EpicBorderGray, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = EpicTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACQUIRED",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = EpicTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedDate,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = EpicTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Description Section
                        Text(
                            text = "ABOUT THIS GAME",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.08.em,
                            color = EpicTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentGame.description ?: "No description provided for this product in Epic catalog.",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = EpicTextPrimary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Product ID
                        Text(
                            text = "CATALOG ITEM ID",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.08.em,
                            color = EpicTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentGame.id,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = EpicTextMuted
                        )
                    }
                }

                // Floating Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, EpicBorderGray, CircleShape)
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Library",
                        tint = EpicWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
