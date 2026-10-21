package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.local.TokenStore
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LibraryRepository
import com.example.ui.components.EpicButton
import com.example.ui.components.EpicButtonStyle
import com.example.ui.theme.EpicAnimatedWallpaper
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicBorderGray
import com.example.ui.theme.EpicCardShape
import com.example.ui.theme.EpicDialogShape
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicError
import com.example.ui.theme.EpicSurfaceGray
import com.example.ui.theme.EpicTextMuted
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicTextSecondary
import com.example.ui.theme.EpicVoid
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    libraryRepository: LibraryRepository,
    tokenStore: TokenStore,
    onBack: () -> Unit,
    onLogoutCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    var showClientDialog by remember { mutableStateOf(false) }
    var customClientId by remember { mutableStateOf(tokenStore.getCustomClientId()) }
    var customClientSecret by remember { mutableStateOf(tokenStore.getCustomClientSecret()) }

    val displayName = remember { tokenStore.getDisplayName() }
    val accountId = remember { tokenStore.getAccountId() }
    val truncatedAccountId = remember(accountId) {
        if (accountId.length > 12) {
            "${accountId.take(6)}...${accountId.takeLast(6)}"
        } else {
            accountId.ifEmpty { "demo_account" }
        }
    }

    EpicAnimatedWallpaper(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = EpicWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "SETTINGS & ACCOUNT",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.04.em,
                    color = EpicTextPrimary
                )
            }

            // Scrollable Settings Sections
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Account Section Card
                SectionHeader("ACCOUNT")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EpicCardShape)
                        .background(EpicElevated)
                        .border(1.dp, EpicBorderGray, EpicCardShape)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EpicSurfaceGray)
                                .border(2.dp, EpicBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "E",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = EpicWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = displayName,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = EpicTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Account ID: $truncatedAccountId",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = EpicTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    EpicButton(
                        text = "SIGN OUT",
                        onClick = { showLogoutDialog = true },
                        style = EpicButtonStyle.DANGER,
                        leadingIcon = {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = EpicWhite, modifier = Modifier.size(18.dp))
                        },
                        enabled = !isLoggingOut,
                        testTag = "settings_logout_button"
                    )
                }

                // Data & Cache Management
                SectionHeader("DATA & STORAGE")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EpicCardShape)
                        .background(EpicElevated)
                        .border(1.dp, EpicBorderGray, EpicCardShape)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    libraryRepository.clearCache()
                                    Toast.makeText(context, "Local library cache cleared", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = EpicBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Clear Library Cache",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EpicTextPrimary
                            )
                            Text(
                                text = "Clears Room database cache and re-downloads fresh metadata on next sync",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = EpicTextMuted
                            )
                        }
                    }
                }

                // OAuth Client Configuration
                SectionHeader("OAUTH CLIENT CONFIGURATION")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EpicCardShape)
                        .background(EpicElevated)
                        .border(1.dp, EpicBorderGray, EpicCardShape)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClientDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = EpicBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Client ID / Secret Credentials",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EpicTextPrimary
                            )
                            Text(
                                text = "Currently using Community Launcher Client (${tokenStore.getCustomClientId().take(8)}...)",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = EpicTextMuted
                            )
                        }
                    }
                }

                // About & Disclaimer Section
                SectionHeader("LEGAL & DISCLAIMER")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EpicCardShape)
                        .background(EpicElevated)
                        .border(1.dp, EpicBorderGray, EpicCardShape)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = EpicTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "EpicLibrary v1.0.0",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EpicTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This application is an unofficial client for viewing your Epic Games library. It is not affiliated with, endorsed by, or sponsored by Epic Games, Inc. 'Epic Games' and the Epic Games logo are trademarks of Epic Games, Inc. Use at your own risk.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = EpicTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Section 3: Sign Out Confirmation AlertDialog in Epic Style
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { if (!isLoggingOut) showLogoutDialog = false },
                shape = EpicDialogShape,
                containerColor = EpicElevated,
                title = {
                    Text(
                        text = "Sign out of Epic Games?",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = EpicTextPrimary
                    )
                },
                text = {
                    Text(
                        text = "Your session will be revoked on Epic's servers and all local data will be cleared.",
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = EpicTextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isLoggingOut = true
                            scope.launch {
                                authRepository.logout()
                                showLogoutDialog = false
                                isLoggingOut = false
                                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                                onLogoutCompleted()
                            }
                        },
                        modifier = Modifier.testTag("confirm_signout_button")
                    ) {
                        Text(
                            text = "SIGN OUT",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = EpicError
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        enabled = !isLoggingOut
                    ) {
                        Text(
                            text = "CANCEL",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = EpicTextSecondary
                        )
                    }
                }
            )
        }

        // Custom Client ID / Secret Dialog
        if (showClientDialog) {
            AlertDialog(
                onDismissRequest = { showClientDialog = false },
                shape = EpicDialogShape,
                containerColor = EpicElevated,
                title = {
                    Text(
                        text = "OAuth Client Credentials",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EpicTextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Configure custom OAuth client ID and secret if registered with Epic Games developer portal:",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = EpicTextSecondary
                        )
                        OutlinedTextField(
                            value = customClientId,
                            onValueChange = { customClientId = it },
                            label = { Text("Client ID", color = EpicTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = EpicTextPrimary,
                                unfocusedTextColor = EpicTextPrimary,
                                focusedBorderColor = EpicBlue,
                                unfocusedBorderColor = EpicBorderGray
                            )
                        )
                        OutlinedTextField(
                            value = customClientSecret,
                            onValueChange = { customClientSecret = it },
                            label = { Text("Client Secret", color = EpicTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = EpicTextPrimary,
                                unfocusedTextColor = EpicTextPrimary,
                                focusedBorderColor = EpicBlue,
                                unfocusedBorderColor = EpicBorderGray
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tokenStore.setCustomClientId(customClientId)
                            tokenStore.setCustomClientSecret(customClientSecret)
                            showClientDialog = false
                            Toast.makeText(context, "Credentials updated", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("SAVE", color = EpicBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClientDialog = false }) {
                        Text("CANCEL", color = EpicTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.08.em,
        color = EpicTextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}
