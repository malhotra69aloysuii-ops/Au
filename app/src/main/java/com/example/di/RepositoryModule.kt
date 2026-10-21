package com.example.di

import android.content.Context
import com.example.data.local.TokenStore
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.LibraryRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LibraryRepository

class RepositoryModule(
    context: Context,
    networkModule: NetworkModule,
    databaseModule: DatabaseModule,
    val tokenStore: TokenStore,
    val networkMonitor: com.example.data.util.NetworkMonitor
) {

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            context = context,
            authApi = networkModule.authApi,
            tokenStore = tokenStore,
            gameDao = databaseModule.gameDao,
            imageLoader = networkModule.imageLoader
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(
            graphQLApi = networkModule.graphQLApi,
            libraryServiceApi = networkModule.libraryServiceApi,
            launcherAssetsApi = networkModule.launcherAssetsApi,
            entitlementApi = networkModule.entitlementApi,
            catalogApi = networkModule.catalogApi,
            gameDao = databaseModule.gameDao,
            tokenStore = tokenStore,
            authRepository = authRepository,
            networkMonitor = networkMonitor
        )
    }

    val credentialsUpdater: com.example.data.repository.CredentialsUpdater by lazy {
        com.example.data.repository.CredentialsUpdater(
            okHttpClient = networkModule.okHttpClient,
            tokenStore = tokenStore
        )
    }
}
