package com.example.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AuthWebView
import com.example.ui.components.EpicButton
import com.example.ui.components.EpicButtonStyle
import com.example.ui.components.EpicLoader
import com.example.ui.theme.EpicAnimatedWallpaper
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicError
import com.example.ui.theme.EpicTextMuted
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicTextSecondary
import com.example.ui.theme.EpicVoid
import com.example.ui.theme.InterFontFamily

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        EpicAnimatedWallpaper {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Crest Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_fg),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Hero Title
                Text(
                    text = "SIGN IN TO EPIC GAMES",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.02).em,
                    textAlign = TextAlign.Center,
                    color = EpicTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Connect your account to access your personal game library, entitlements, and catalog data.",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = EpicTextSecondary
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Error Card if any
                AnimatedVisibility(visible = uiState is AuthUiState.Error) {
                    val errMessage = (uiState as? AuthUiState.Error)?.message.orEmpty()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EpicElevated)
                            .border(1.dp, EpicError, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = EpicError,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errMessage,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = EpicTextPrimary
                            )
                        }
                    }
                }

                // Primary Pill Button: SIGN IN WITH EPIC
                EpicButton(
                    text = "SIGN IN WITH EPIC",
                    onClick = { viewModel.openWebView() },
                    style = EpicButtonStyle.PRIMARY,
                    testTag = "sign_in_with_epic_button"
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Fine Print / Legal Disclaimer (Required by Section 10)
                Text(
                    text = "This application is an unofficial client for viewing your Epic Games library. It is not affiliated with, endorsed by, or sponsored by Epic Games, Inc. 'Epic Games' and the Epic Games logo are trademarks of Epic Games, Inc. Use at your own risk.",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    color = EpicTextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Embedded WebView Overlay
        AnimatedVisibility(
            visible = uiState is AuthUiState.InWebView,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AuthWebView(
                clientId = viewModel.getClientId(),
                onCodeReceived = { code ->
                    viewModel.handleAuthCode(code)
                },
                onError = { err ->
                    viewModel.handleAuthError(err)
                },
                onClose = {
                    viewModel.closeWebView()
                }
            )
        }

        // Code exchange loading overlay
        AnimatedVisibility(
            visible = uiState is AuthUiState.ExchangingCode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EpicVoid.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                EpicLoader(statusText = "AUTHENTICATING SESSION...")
            }
        }
    }
}
