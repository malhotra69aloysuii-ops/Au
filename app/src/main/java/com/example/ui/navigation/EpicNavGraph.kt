package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.screens.detail.GameDetailScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.login.AuthViewModel
import com.example.ui.screens.login.LoginScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen

object EpicDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val LIBRARY = "library"
    const val DETAIL = "detail/{gameId}"
    const val SETTINGS = "settings"

    fun detailRoute(gameId: String) = "detail/$gameId"
}

@Composable
fun EpicNavGraph(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = EpicDestinations.SPLASH,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffset = { 36 }
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffset = { 36 }
                )
        }
    ) {
        // Splash Screen
        composable(EpicDestinations.SPLASH) {
            SplashScreen(
                isLoggedIn = container.tokenStore.isLoggedIn(),
                onNavigateToHome = {
                    navController.navigate(EpicDestinations.LIBRARY) {
                        popUpTo(EpicDestinations.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(EpicDestinations.LOGIN) {
                        popUpTo(EpicDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(EpicDestinations.LOGIN) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.provideFactory(
                    authRepository = container.authRepository,
                    tokenStore = container.tokenStore,
                    credentialsUpdater = container.credentialsUpdater
                )
            )

            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(EpicDestinations.LIBRARY) {
                        popUpTo(EpicDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Library Screen (Main)
        composable(EpicDestinations.LIBRARY) {
            val libraryViewModel: LibraryViewModel = viewModel(
                factory = LibraryViewModel.provideFactory(
                    libraryRepository = container.libraryRepository,
                    authRepository = container.authRepository,
                    tokenStore = container.tokenStore,
                    networkMonitor = container.networkMonitor
                )
            )

            LibraryScreen(
                viewModel = libraryViewModel,
                onGameClick = { gameId ->
                    navController.navigate(EpicDestinations.detailRoute(gameId))
                },
                onSettingsClick = {
                    navController.navigate(EpicDestinations.SETTINGS)
                }
            )
        }

        // Game Detail Screen
        composable(
            route = EpicDestinations.DETAIL,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameDetailScreen(
                gameId = gameId,
                libraryRepository = container.libraryRepository,
                onBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(EpicDestinations.SETTINGS) {
            SettingsScreen(
                authRepository = container.authRepository,
                libraryRepository = container.libraryRepository,
                tokenStore = container.tokenStore,
                onBack = { navController.popBackStack() },
                onLogoutCompleted = {
                    navController.navigate(EpicDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
