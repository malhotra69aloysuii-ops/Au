package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.EpicBlue
import com.example.ui.theme.EpicCarbon
import com.example.ui.theme.EpicElevated
import com.example.ui.theme.EpicTextPrimary
import com.example.ui.theme.EpicVoid
import com.example.ui.theme.EpicWhite
import com.example.ui.theme.InterFontFamily
import java.net.URLEncoder

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthWebView(
    clientId: String,
    onCodeReceived: (String) -> Unit,
    onError: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var hasDispatchedCode by remember { mutableStateOf(false) }

    val encodedRedirect = URLEncoder.encode(
        "https://www.epicgames.com/id/api/redirect?clientId=$clientId&responseType=code",
        "UTF-8"
    )
    val authUrl = "https://www.epicgames.com/id/login?redirectUrl=$encodedRedirect"

    fun extractAuthCode(content: String): String? {
        // 1. Check for JSON "authorizationCode": "..."
        val authCodeRegex = Regex("""["']?authorizationCode["']?\s*[:=]\s*["']?([a-fA-F0-9]{30,})["']?""")
        authCodeRegex.find(content)?.groupValues?.getOrNull(1)?.let { return it }

        // 2. Check for redirectUrl with code=...
        val redirectCodeRegex = Regex("""[?&"'{\s]code["']?\s*[:=]\s*["']?([a-fA-F0-9]{30,})["']?""")
        redirectCodeRegex.find(content)?.groupValues?.getOrNull(1)?.let { return it }

        return null
    }

    BackHandler {
        if (webViewInstance?.canGoBack() == true && !isAuthenticating) {
            webViewInstance?.goBack()
        } else {
            onClose()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.stopLoading()
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EpicVoid)
            .statusBarsPadding()
    ) {
        // In-App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(EpicCarbon),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("webview_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Login",
                    tint = EpicWhite
                )
            }

            Text(
                text = "SIGN IN TO EPIC GAMES",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = EpicTextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // Thin EpicBlue Linear Progress Bar
        if (progress in 0.01f..0.99f && !isAuthenticating) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = EpicBlue,
                trackColor = EpicElevated
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(EpicElevated)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // 2.1 WebView Configuration
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true

                        // Mobile Chrome UA with EpicGames/Android
                        val defaultUa = settings.userAgentString
                        settings.userAgentString = "$defaultUa EpicGames/Android"

                        // Cookie Configuration
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                                if (newProgress >= 80) {
                                    isInitialLoading = false
                                }
                            }
                        }

                        fun inspectPageContent(view: WebView?) {
                            if (hasDispatchedCode) return
                            view?.evaluateJavascript(
                                "(function() { return (document.body ? (document.body.innerText || document.body.textContent || '') : '') + ' ' + (document.body ? document.body.innerHTML : ''); })();"
                            ) { result ->
                                if (hasDispatchedCode) return@evaluateJavascript
                                if (!result.isNullOrBlank() && result != "null" && result != "\"\"") {
                                    val code = extractAuthCode(result)
                                    if (!code.isNullOrBlank()) {
                                        hasDispatchedCode = true
                                        isAuthenticating = true
                                        view.visibility = android.view.View.INVISIBLE
                                        view.stopLoading()
                                        Log.d("AuthWebView", "Successfully captured authorizationCode from body")
                                        onCodeReceived(code)
                                    }
                                }
                            }
                        }

                        fun checkUrl(view: WebView?, url: String?): Boolean {
                            if (url.isNullOrBlank() || hasDispatchedCode) return false

                            // If URL targets redirect or authorization completion, immediately mask to hide JSON
                            if (url.contains("/id/api/redirect") ||
                                url.contains("/launcher/authorized") ||
                                url.contains("localhost") ||
                                url.contains("authorizationCode") ||
                                url.contains("code=")
                            ) {
                                isAuthenticating = true
                                view?.visibility = android.view.View.INVISIBLE
                            }

                            val directCode = extractAuthCode(url)
                            if (!directCode.isNullOrBlank()) {
                                hasDispatchedCode = true
                                isAuthenticating = true
                                view?.visibility = android.view.View.INVISIBLE
                                view?.stopLoading()
                                Log.d("AuthWebView", "Successfully captured code from URL: $url")
                                onCodeReceived(directCode)
                                return true
                            }

                            if (url.contains("error=")) {
                                val errorRegex = Regex("""error=([^&]+)""")
                                val err = errorRegex.find(url)?.groupValues?.getOrNull(1) ?: "Login error"
                                onError(err)
                                return true
                            }

                            return false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                if (url?.contains("/id/api/redirect") == true ||
                                    url?.contains("/launcher/authorized") == true ||
                                    url?.contains("localhost") == true ||
                                    url?.contains("code=") == true
                                ) {
                                    view?.visibility = android.view.View.INVISIBLE
                                    isAuthenticating = true
                                }
                                checkUrl(view, url)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val requestUrl = request?.url?.toString() ?: return false
                                if (checkUrl(view, requestUrl)) return true

                                if (requestUrl.startsWith("https://localhost") || requestUrl.startsWith("http://localhost")) {
                                    view?.visibility = android.view.View.INVISIBLE
                                    isAuthenticating = true
                                    inspectPageContent(view)
                                    return true
                                }
                                return false
                            }

                            @Deprecated("Deprecated in Java")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                val requestUrl = url ?: return false
                                if (checkUrl(view, requestUrl)) return true

                                if (requestUrl.startsWith("https://localhost") || requestUrl.startsWith("http://localhost")) {
                                    view?.visibility = android.view.View.INVISIBLE
                                    isAuthenticating = true
                                    inspectPageContent(view)
                                    return true
                                }
                                return false
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                checkUrl(view, url)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                val failingUrl = request?.url?.toString()
                                if (checkUrl(view, failingUrl)) {
                                    return
                                }
                                super.onReceivedError(view, request, error)
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                if (checkUrl(view, failingUrl)) {
                                    return
                                }
                                super.onReceivedError(view, errorCode, description, failingUrl)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url?.contains("/id/api/redirect") == true ||
                                    url?.contains("/launcher/authorized") == true ||
                                    url?.contains("localhost") == true ||
                                    url?.contains("code=") == true
                                ) {
                                    view?.visibility = android.view.View.INVISIBLE
                                    isAuthenticating = true
                                } else {
                                    isInitialLoading = false
                                }
                                checkUrl(view, url)
                                inspectPageContent(view)
                            }
                        }

                        loadUrl(authUrl)
                        webViewInstance = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("auth_webview")
            )

            // Epic loading overlay (covers screen during initial load and while authenticating)
            // Ensures the user NEVER sees the raw JSON from the server!
            if (isInitialLoading || isAuthenticating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(EpicVoid),
                    contentAlignment = Alignment.Center
                ) {
                    EpicLoader(
                        statusText = if (isAuthenticating) "AUTHENTICATING & SYNCING LIBRARY..." else "CONNECTING TO EPIC GAMES..."
                    )
                }
            }
        }
    }
}
