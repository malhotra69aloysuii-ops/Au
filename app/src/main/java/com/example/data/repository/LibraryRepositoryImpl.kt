package com.example.data.repository

import android.util.Log
import com.example.data.api.CatalogApi
import com.example.data.api.EntitlementApi
import com.example.data.api.EpicGraphQLApi
import com.example.data.api.EpicLauncherAssetsApi
import com.example.data.api.EpicLibraryServiceApi
import com.example.data.api.EpicQueries
import com.example.data.api.GraphQLRequest
import com.example.data.local.GameDao
import com.example.data.local.GameEntity
import com.example.data.local.TokenStore
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.util.NetworkMonitor
import com.example.domain.model.Game
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class RawGameRecord(
    val catalogItemId: String,
    val namespace: String,
    val productId: String? = null,
    val appName: String? = null
)

internal class SyncStats {
    val sourceErrors: MutableList<String> = java.util.Collections.synchronizedList(mutableListOf<String>())
    val catalogFailures = java.util.concurrent.atomic.AtomicInteger(0)
    @Volatile var lastCatalogError: String? = null
}

class LibraryRepositoryImpl(
    private val graphQLApi: EpicGraphQLApi,
    private val libraryServiceApi: EpicLibraryServiceApi,
    private val launcherAssetsApi: EpicLauncherAssetsApi,
    private val entitlementApi: EntitlementApi,
    private val catalogApi: CatalogApi,
    private val gameDao: GameDao,
    private val tokenStore: TokenStore,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) : LibraryRepository {

    override val games: Flow<List<Game>> = gameDao.getAllGames().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getGameById(id: String): Flow<Game?> = flow {
        val game = gameDao.getGameById(id)?.toDomain()
        emit(game)
    }

    override suspend fun clearCache() = withContext(Dispatchers.IO) {
        gameDao.clearAll()
    }

    override suspend fun refreshLibrary(force: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!networkMonitor.isCurrentlyOnline()) {
                Log.d(TAG, "Offline mode active. Browsing local cached library.")
                return@runCatching
            }

            var tokens = tokenStore.getTokens()
                ?: throw Exception("Not logged in. Please sign in with Epic Games.")

            if (tokens.isExpired()) {
                val refreshResult = authRepository.refreshToken()
                tokens = refreshResult.getOrThrow()
            }

            val bearerToken = "Bearer ${tokens.accessToken}"
            val accountId = tokens.accountId

            val rawRecords = mutableListOf<RawGameRecord>()

            val stats = SyncStats()

            // 1. Fetch from library-service endpoint (GameShelf / EGS direct library items API)
            try {
                var cursor: String? = null
                var page = 0
                val maxPages = 20

                do {
                    page++
                    val libRes = libraryServiceApi.getLibraryItems(
                        bearerToken = bearerToken,
                        includeMetadata = true,
                        cursor = cursor
                    )
                    if (libRes.isSuccessful && libRes.body()?.records != null) {
                        val body = libRes.body()!!
                        val items = body.records.orEmpty()
                        Log.d(TAG, "LibraryService Page $page returned ${items.size} items")

                        for (item in items) {
                            val catId = item.catalogItemId
                            val ns = item.namespace
                            if (!catId.isNullOrBlank() && !ns.isNullOrBlank()) {
                                if (ns.equals("ue", ignoreCase = true)) continue
                                if (item.sandboxType?.equals("PRIVATE", ignoreCase = true) == true) continue
                                if (item.appName == "1") continue
                                rawRecords.add(
                                    RawGameRecord(
                                        catalogItemId = catId,
                                        namespace = ns,
                                        productId = item.productId,
                                        appName = item.appName
                                    )
                                )
                            }
                        }

                        cursor = body.responseMetadata?.nextCursor
                    } else {
                        Log.w(TAG, "LibraryService unsuccessful: HTTP ${libRes.code()}")
                        stats.sourceErrors.add("library service HTTP ${libRes.code()}")
                        cursor = null
                    }
                } while (!cursor.isNullOrBlank() && page < maxPages)
            } catch (e: Exception) {
                Log.w(TAG, "LibraryService fetch exception: ${e.message}")
                stats.sourceErrors.add("library service: ${e.message}")
            }

            // 2. Fetch from launcher assets endpoint (Legendary Windows platform assets)
            try {
                val assetRes = launcherAssetsApi.getAssets(
                    bearerToken = bearerToken,
                    platform = "Windows",
                    label = "Live"
                )
                if (assetRes.isSuccessful && assetRes.body() != null) {
                    val assets = assetRes.body()!!
                    Log.d(TAG, "Launcher assets returned ${assets.size} items")
                    for (asset in assets) {
                        val catId = asset.catalogItemId
                        val ns = asset.namespace
                        if (!catId.isNullOrBlank() && !ns.isNullOrBlank()) {
                            if (ns.equals("ue", ignoreCase = true)) continue
                            if (asset.appName == "1") continue
                            rawRecords.add(
                                RawGameRecord(
                                    catalogItemId = catId,
                                    namespace = ns,
                                    appName = asset.appName
                                )
                            )
                        }
                    }
                } else {
                    Log.w(TAG, "Launcher assets unsuccessful: HTTP ${assetRes.code()}")
                    stats.sourceErrors.add("launcher assets HTTP ${assetRes.code()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Launcher assets fetch exception: ${e.message}")
                stats.sourceErrors.add("launcher assets: ${e.message}")
            }

            // 3. Fallback: If still empty, check entitlements endpoint
            if (rawRecords.isEmpty()) {
                try {
                    val entRes = entitlementApi.getEntitlements(
                        bearerAuth = bearerToken,
                        accountId = accountId,
                        start = 0,
                        count = 1000
                    )
                    if (entRes.isSuccessful && entRes.body() != null) {
                        val ents = entRes.body()!!
                        for (ent in ents) {
                            val catId = ent.catalogItemId
                            val ns = ent.namespace
                            if (!catId.isNullOrBlank() && !ns.isNullOrBlank()) {
                                if (ns.equals("ue", ignoreCase = true)) continue
                                rawRecords.add(
                                    RawGameRecord(
                                        catalogItemId = catId,
                                        namespace = ns
                                    )
                                )
                            }
                        }
                    } else {
                        Log.w(TAG, "Entitlements unsuccessful: HTTP ${entRes.code()}")
                        stats.sourceErrors.add("entitlements HTTP ${entRes.code()}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Entitlements fallback exception: ${e.message}")
                    stats.sourceErrors.add("entitlements: ${e.message}")
                }
            }

            // Deduplicate by catalogItemId
            val uniqueRecords = rawRecords.distinctBy { it.catalogItemId }
            if (uniqueRecords.isEmpty() && stats.sourceErrors.isNotEmpty()) {
                val hint = if (stats.sourceErrors.any { it.contains("401") || it.contains("403") }) {
                    " Try signing out and back in."
                } else ""
                throw Exception("Couldn't load your library (" + stats.sourceErrors.joinToString("; ") + ")." + hint)
            }
            Log.d(TAG, "Total unique game records to resolve: ${uniqueRecords.size}")

            // 4. Resolve titles and CDN images using Catalog API
            val resolvedGames = resolveCatalogDetails(uniqueRecords, bearerToken, stats)
            Log.d(TAG, "Successfully resolved ${resolvedGames.size} games")

            if (resolvedGames.isNotEmpty()) {
                gameDao.replaceAll(resolvedGames.map { it.toEntity() })
            } else if (uniqueRecords.isNotEmpty()) {
                throw Exception(
                    "Found ${uniqueRecords.size} titles but couldn't load their details " +
                        "(catalog: ${stats.lastCatalogError ?: "no matching items"}). Please try again."
                )
            }
        }
    }

    private suspend fun resolveCatalogDetails(
        records: List<RawGameRecord>,
        bearerToken: String,
        stats: SyncStats
    ): List<Game> = coroutineScope {
        // Limit concurrency to avoid Epic rate limiting (HTTP 429)
        val semaphore = Semaphore(4)
        records.map { record ->
            async(Dispatchers.IO) {
                semaphore.withPermit { resolveSingleGame(record, bearerToken, stats) }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun resolveSingleGame(
        record: RawGameRecord,
        bearerToken: String,
        stats: SyncStats
    ): Game? {
        val namespace = record.namespace
        val catalogItemId = record.catalogItemId

        // 1. First attempt with catalogItemId
        var item = fetchCatalogItem(namespace, catalogItemId, bearerToken, stats)

        // 2. Fallback attempt with productId if catalog lookup returned null
        if (item == null && !record.productId.isNullOrBlank()) {
            item = fetchCatalogItem(namespace, record.productId, bearerToken, stats)
        }

        if (item == null) {
            return null
        }

        // STRICT FILTER: Only keep main games
        val title = item.title?.takeIf { it.isNotBlank() } ?: return null
        if (title.contains("Audience", ignoreCase = true) ||
            title.contains("GeneralAudience", ignoreCase = true)
        ) {
            return null
        }

        val type = (item.catalogItemType ?: item.itemType).orEmpty().uppercase()
        if (type == "AUDIENCE" || type == "DLC" || type == "ADD_ON") {
            return null
        }

        // THE IMAGE HIERARCHY (From highest quality tall cover art to fallback)
        val images = item.keyImages.orEmpty()
        val imageUrl = images.firstOrNull {
            it.type.equals("DieselGameBoxTall", ignoreCase = true)
        }?.url
            ?: images.firstOrNull {
                it.type.equals("OfferImageTall", ignoreCase = true)
            }?.url
            ?: images.firstOrNull {
                it.type.equals("DieselStoreFrontTall", ignoreCase = true)
            }?.url
            ?: images.firstOrNull {
                it.type.equals("DieselGameBox", ignoreCase = true)
            }?.url
            ?: images.firstOrNull {
                it.type.equals("Thumbnail", ignoreCase = true)
            }?.url
            ?: images.firstOrNull()?.url

        val developer = item.developer?.ifBlank { "Epic Games" } ?: "Epic Games"
        val description = item.description?.ifBlank { "Owned game in Epic Games library." }
            ?: "Owned game in Epic Games library."

        return Game(
            id = catalogItemId,
            name = title,
            namespace = namespace,
            imageUrl = imageUrl,
            developer = developer,
            description = description,
            ownedSince = System.currentTimeMillis()
        )
    }

    private suspend fun fetchCatalogItem(
        namespace: String,
        id: String,
        bearerToken: String,
        stats: SyncStats
    ): com.example.data.api.CatalogItemDto? {
        repeat(3) { attempt ->
            try {
                val response = catalogApi.getBulkItems(
                    bearerAuth = bearerToken,
                    namespace = namespace,
                    catalogItemId = id,
                    country = "US",
                    locale = "en-US",
                    includeDLCDetails = true,
                    includeMainGameDetails = true
                )
                if (response.isSuccessful) {
                    return response.body()?.get(id)
                }
                val code = response.code()
                stats.lastCatalogError = "HTTP $code"
                if (code == 429 || code >= 500) {
                    delay(600L * (attempt + 1))
                    return@repeat
                }
                // 404 just means this item isn't in the catalog; other codes are real failures
                if (code != 404) stats.catalogFailures.incrementAndGet()
                return null
            } catch (e: Exception) {
                stats.lastCatalogError = e.message ?: e.javaClass.simpleName
                delay(300L * (attempt + 1))
            }
        }
        stats.catalogFailures.incrementAndGet()
        return null
    }

    companion object {
        private const val TAG = "LibraryRepositoryImpl"
    }
}
