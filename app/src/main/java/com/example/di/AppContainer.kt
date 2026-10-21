package com.example.di

import android.content.Context
import coil.ImageLoader
import com.example.data.local.TokenStore
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LibraryRepository

class AppContainer(context: Context) {

    val tokenStore: TokenStore by lazy {
        TokenStore(context)
    }

    val networkModule: NetworkModule by lazy {
        NetworkModule(context)
    }

    val networkMonitor: com.example.data.util.NetworkMonitor by lazy {
        com.example.data.util.NetworkMonitor(context)
    }

    val databaseModule: DatabaseModule by lazy {
        DatabaseModule(context)
    }

    val repositoryModule: RepositoryModule by lazy {
        RepositoryModule(
            context = context,
            networkModule = networkModule,
            databaseModule = databaseModule,
            tokenStore = tokenStore,
            networkMonitor = networkMonitor
        )
    }

    val authRepository: AuthRepository
        get() = repositoryModule.authRepository

    val libraryRepository: LibraryRepository
        get() = repositoryModule.libraryRepository

    val credentialsUpdater: com.example.data.repository.CredentialsUpdater
        get() = repositoryModule.credentialsUpdater

    val imageLoader: ImageLoader
        get() = networkModule.imageLoader
}
