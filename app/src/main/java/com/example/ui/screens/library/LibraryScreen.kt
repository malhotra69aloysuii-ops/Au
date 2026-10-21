package com.example.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EpicButton
import com.example.ui.components.EpicButtonStyle
import com.example.ui.components.EpicTopBar
import com.example.ui.components.GameCard
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.EpicAnimatedWallpaper
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicBorderGray
import com.example.ui.theme.EpicCardShape
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicError
import com.example.ui.theme.EpicTextMuted
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicTextSecondary
import com.example.ui.theme.EpicVoid
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onGameClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val showOfflineBanner by viewModel.showOfflineBanner.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState()
    val scrollOffset by remember {
        derivedStateOf {
            gridState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    var isSearchFocused by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EpicTopBar(
                title = "LIBRARY",
                displayName = viewModel.displayName,
                isRefreshing = isRefreshing,
                onSyncClick = { viewModel.refreshLibrary(force = true) },
                onSettingsClick = onSettingsClick
            )
        },
        containerColor = EpicVoid,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        EpicAnimatedWallpaper(
            scrollOffset = scrollOffset,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Animated Offline Bar: slides in from the left, stays 5 seconds, then slides out to the right
                AnimatedVisibility(
                    visible = showOfflineBanner,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF281518))
                            .border(BorderStroke(1.dp, Color(0xFFE53935)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("offline_bar")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "Offline icon",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "You're Offline",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EpicWhite
                                )
                                Text(
                                    text = "Browsing cached library",
                                    fontFamily = InterFontFamily,
                                    fontSize = 11.sp,
                                    color = EpicTextSecondary
                                )
                            }
                        }
                    }
                }

                // Epic Games Styled Search Bar with EpicBlue Focus Glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EpicElevated)
                            .then(
                                if (isSearchFocused) {
                                    Modifier
                                        .border(
                                            BorderStroke(3.dp, EpicBlue.copy(alpha = 0.25f)),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            BorderStroke(1.5.dp, EpicBlue),
                                            RoundedCornerShape(12.dp)
                                        )
                                } else {
                                    Modifier.border(
                                        BorderStroke(1.dp, EpicBorderGray),
                                        RoundedCornerShape(12.dp)
                                    )
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchFocused) EpicBlue else EpicTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search library by game name...",
                                        fontFamily = InterFontFamily,
                                        fontSize = 14.sp,
                                        color = EpicTextMuted
                                    )
                                }

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = InterFontFamily,
                                        fontSize = 14.sp,
                                        color = EpicTextPrimary
                                    ),
                                    cursorBrush = SolidColor(EpicBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { isSearchFocused = it.isFocused }
                                        .testTag("search_text_field")
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChanged("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = EpicTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter / Sort Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = sortOption == LibrarySortOption.ALPHABETICAL_ASC,
                        onClick = { viewModel.onSortOptionChanged(LibrarySortOption.ALPHABETICAL_ASC) },
                        label = { Text("A - Z", fontFamily = InterFontFamily, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EpicBlue,
                            selectedLabelColor = EpicWhite,
                            containerColor = EpicElevated,
                            labelColor = EpicTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sortOption == LibrarySortOption.ALPHABETICAL_ASC,
                            borderColor = EpicBorderGray,
                            selectedBorderColor = EpicBlue
                        )
                    )

                    FilterChip(
                        selected = sortOption == LibrarySortOption.ALPHABETICAL_DESC,
                        onClick = { viewModel.onSortOptionChanged(LibrarySortOption.ALPHABETICAL_DESC) },
                        label = { Text("Z - A", fontFamily = InterFontFamily, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EpicBlue,
                            selectedLabelColor = EpicWhite,
                            containerColor = EpicElevated,
                            labelColor = EpicTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sortOption == LibrarySortOption.ALPHABETICAL_DESC,
                            borderColor = EpicBorderGray,
                            selectedBorderColor = EpicBlue
                        )
                    )

                    FilterChip(
                        selected = sortOption == LibrarySortOption.RECENTLY_ADDED,
                        onClick = { viewModel.onSortOptionChanged(LibrarySortOption.RECENTLY_ADDED) },
                        label = { Text("Recent", fontFamily = InterFontFamily, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EpicBlue,
                            selectedLabelColor = EpicWhite,
                            containerColor = EpicElevated,
                            labelColor = EpicTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sortOption == LibrarySortOption.RECENTLY_ADDED,
                            borderColor = EpicBorderGray,
                            selectedBorderColor = EpicBlue
                        )
                    )
                }

                // Error Card Alert if sync failed
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { error ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(EpicCardShape)
                                .background(EpicElevated)
                                .border(1.dp, EpicError, EpicCardShape)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = EpicError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = error,
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = EpicTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RETRY",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = EpicBlue,
                                    modifier = Modifier
                                        .clickable { viewModel.refreshLibrary(force = true) }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // Content: Skeleton Loading, Games Grid, or Empty State
                if (isRefreshing && games.isEmpty()) {
                    // Shimmer Skeleton Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(6) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(EpicCardShape)
                                    .background(EpicElevated)
                                    .border(1.dp, EpicBorderGray, EpicCardShape)
                            ) {
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(3f / 4f),
                                    shape = RoundedCornerShape(0.dp)
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    ShimmerBox(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(14.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ShimmerBox(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f)
                                            .height(11.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (games.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(EpicElevated)
                                .border(1.dp, EpicBorderGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOff,
                                contentDescription = null,
                                tint = EpicTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No games found matching \"$searchQuery\""
                            } else if (isOffline) {
                                "You're Offline"
                            } else {
                                "Your library is empty."
                            },
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = EpicTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Try checking the spelling or clear your search to view all games."
                            } else if (isOffline) {
                                "No cached games available offline yet. Reconnect to the internet to sync your library."
                            } else {
                                "Tap the sync button above to fetch owned titles from your Epic Games account."
                            },
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            color = EpicTextMuted,
                            textAlign = TextAlign.Center
                        )

                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(18.dp))
                            EpicButton(
                                text = "CLEAR SEARCH",
                                onClick = { viewModel.onSearchQueryChanged("") },
                                style = EpicButtonStyle.SECONDARY,
                                modifier = Modifier.width(180.dp),
                                testTag = "clear_search_button"
                            )
                        } else if (!isOffline) {
                            Spacer(modifier = Modifier.height(24.dp))
                            EpicButton(
                                text = "SYNC NOW",
                                onClick = { viewModel.refreshLibrary(force = true) },
                                style = EpicButtonStyle.PRIMARY,
                                modifier = Modifier.width(180.dp),
                                testTag = "empty_sync_button"
                            )
                        }
                    }
                } else {
                    // Games Grid (2-column on mobile, adaptive on tablets)
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 10.dp,
                            bottom = 24.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("games_grid")
                    ) {
                        items(
                            items = games,
                            key = { it.id }
                        ) { game ->
                            GameCard(
                                game = game,
                                onClick = { onGameClick(game.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
