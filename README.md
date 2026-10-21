# EpicLibrary (Vault Client)

A native Android application built with Jetpack Compose that allows users to sign in to their Epic Games account via an in-app OAuth WebView flow, browse their owned game library with catalog metadata, cache content offline with Room, and perform a full-wipe sign-out from within the app.

---

## Legal Notice & Disclaimer

> **IMPORTANT DISCLAIMER**:
> This application is an **unofficial client** for viewing your Epic Games library. It is **not affiliated with, endorsed by, or sponsored by Epic Games, Inc.**
> "Epic Games" and the Epic Games logo are registered trademarks of Epic Games, Inc.
> This software is intended solely for personal use and educational research into native Android client development. Use at your own risk. Do not redistribute without complying with Epic Games' Terms of Service.

---

## Features

- **Native In-App OAuth Flow**:
  - Entire authentication process handled inside the app using an embedded Android WebView.
  - Zero external browser or custom tab redirects.
  - Full support for third-party cross-domain cookies required by Epic SSO.
  - Mobile Chrome User-Agent with `EpicGames/Android` identifier.
  - Silent redirect interception for authorization code extraction.
- **Encrypted Token Persistence & Silent Refresh**:
  - Tokens (`access_token`, `refresh_token`, `expires_in`, `account_id`) encrypted on-device via `EncryptedSharedPreferences` with `AES256_GCM`.
  - Silent token refresh before expiration with 60-second buffer.
- **Full-Wipe Logout**:
  - Revokes session server-side via `DELETE /account/api/oauth/sessions/kill/{token}`.
  - Clears all WebView cookies via `CookieManager`.
  - Wipes encrypted token store.
  - Purges Room SQLite database cache.
  - Clears Coil image memory and disk caches.
  - Resets navigation stack back to the login screen.
- **Library & Catalog Caching**:
  - Fetches owned entitlements from Epic's entitlement endpoint with pagination.
  - Resolves display titles, box art tall covers, developers, and descriptions from the Catalog API.
  - Room SQLite database enables instant launch and offline access.
- **Epic Games Design Language**:
  - Dark theme palette (`#0A0A0A` EpicVoid background, `#0074E4` electric blue accent).
  - Signature animated drifting aurora wallpaper rendered with Compose Canvas.
  - Shimmer skeleton loading cards (no content spinners).
  - Inter typography scale.
  - 3:4 aspect ratio game tiles with 90ms spring press feedback and long-press context menus.
---

## OAuth Client Configuration

The app defaults to the community launcher client ID used across open-source tools:
- **Client ID**: `34a02cf8f4414e29b15921876da36f9a`
- **Client Secret**: `daafbccc737745039dffe53d94fc76cf`

### Using Your Own Client Credentials:
1. Register an OAuth application in the [Epic Games Developer Portal](https://dev.epicgames.com/).
2. Set your redirect URI to:
   ```
   https://www.epicgames.com/id/api/redirect
   ```
3. In EpicLibrary, navigate to **Settings** -> **OAuth Client Configuration**.
4. Enter your custom Client ID and Client Secret and tap **SAVE**.

---

## Architecture & Tech Stack

- **Language**: Kotlin 2.2
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture (`data` / `domain` / `ui`)
- **Local Database**: Room 2.7 (`GameEntity`, `GameDao`, `EpicDatabase`)
- **Secure Storage**: `EncryptedSharedPreferences` (androidx.security:security-crypto)
- **Networking**: Retrofit 2.12 + OkHttp 4.10 + Moshi
- **Image Loading**: Coil 2.7 with custom memory & disk caching
- **Fonts**: Bundled Google Fonts (Inter)
