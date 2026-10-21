package com.example.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogApi {

    @GET("catalog/api/shared/namespace/{namespace}/bulk/items")
    @Headers(
        "User-Agent: EpicGamesLauncher/15.17.1-23270134+++Portal+Release-Live"
    )
    suspend fun getBulkItems(
        @Header("Authorization") bearerAuth: String,
        @Path("namespace") namespace: String,
        @Query("id") catalogItemId: String,
        @Query("country") country: String = "US",
        @Query("locale") locale: String = "en-US",
        @Query("includeDLCDetails") includeDLCDetails: Boolean = true,
        @Query("includeMainGameDetails") includeMainGameDetails: Boolean = true
    ): Response<Map<String, CatalogItemDto>>
}
