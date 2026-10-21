package com.example.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.api.CatalogApi
import com.example.data.api.EntitlementApi
import com.example.data.api.EpicAuthApi
import com.example.data.api.EpicGraphQLApi
import com.example.data.api.EpicLauncherAssetsApi
import com.example.data.api.EpicLibraryServiceApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class NetworkModule(private val context: Context) {

    private val inMemoryCookieJar = object : CookieJar {
        private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val list = cookieStore.getOrPut(url.host) { mutableListOf() }
            synchronized(list) {
                list.removeAll { old -> cookies.any { new -> new.name == old.name } }
                list.addAll(cookies)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val list = cookieStore[url.host] ?: return emptyList()
            val now = System.currentTimeMillis()
            return synchronized(list) {
                list.filter { it.expiresAt > now }
            }
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .cookieJar(inMemoryCookieJar)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                // Inject Epic Launcher User-Agent if not set
                if (original.header("User-Agent") == null) {
                    requestBuilder.header("User-Agent", "EpicGamesLauncher/15.17.1-23270134+++Portal+Release-Live")
                }

                // Ensure Authorization header has proper Bearer casing
                val authHeader = original.header("Authorization")
                if (authHeader != null && authHeader.startsWith("bearer ", ignoreCase = true)) {
                    val token = authHeader.substring(7).trim()
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val authApi: EpicAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://account-public-service-prod.ol.epicgames.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EpicAuthApi::class.java)
    }

    val graphQLApi: EpicGraphQLApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://launcher-public-service-prod06.ol.epicgames.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EpicGraphQLApi::class.java)
    }

    val libraryServiceApi: EpicLibraryServiceApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://library-service.live.use1a.on.epicgames.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EpicLibraryServiceApi::class.java)
    }

    val launcherAssetsApi: EpicLauncherAssetsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://launcher-public-service-prod06.ol.epicgames.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EpicLauncherAssetsApi::class.java)
    }

    val entitlementApi: EntitlementApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://entitlement-public-service-prod08.ol.epicgames.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EntitlementApi::class.java)
    }

    val catalogApi: CatalogApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://catalog-public-service-prod06.ol.epicgames.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CatalogApi::class.java)
    }

    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(200)
            .build()
    }
}
