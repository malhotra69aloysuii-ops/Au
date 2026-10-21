package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object EpicQueries {
    const val GET_LIBRARY = """
        query libraryQuery(${'$'}cursor: String) {
          Library(cursor: ${'$'}cursor) {
            records {
              catalogItemId
              namespace
              productId
              sandboxName
              recordId
              appName
            }
            responseMetadata {
              nextCursor
            }
          }
        }
    """
}

interface EpicGraphQLApi {
    @POST("graphql")
    @Headers(
        "Content-Type: application/json",
        "User-Agent: EpicGamesLauncher/15.17.1-23270134+++Portal+Release-Live"
    )
    suspend fun getLibrary(
        @Header("Authorization") bearerToken: String,
        @Body body: GraphQLRequest
    ): Response<GraphQLResponse>
}

@JsonClass(generateAdapter = true)
data class GraphQLRequest(
    @Json(name = "query") val query: String,
    @Json(name = "variables") val variables: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class GraphQLResponse(
    @Json(name = "data") val data: LibraryData? = null,
    @Json(name = "errors") val errors: List<GraphQLErrorItem>? = null
)

@JsonClass(generateAdapter = true)
data class GraphQLErrorItem(
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class LibraryData(
    @Json(name = "Library") val library: LibraryRecords? = null
)

@JsonClass(generateAdapter = true)
data class LibraryRecords(
    @Json(name = "records") val records: List<LibraryRecordDto>? = null,
    @Json(name = "responseMetadata") val responseMetadata: ResponseMetadataDto? = null
)

@JsonClass(generateAdapter = true)
data class LibraryRecordDto(
    @Json(name = "catalogItemId") val catalogItemId: String?,
    @Json(name = "namespace") val namespace: String?,
    @Json(name = "productId") val productId: String? = null,
    @Json(name = "sandboxName") val sandboxName: String? = null,
    @Json(name = "recordId") val recordId: String? = null,
    @Json(name = "appName") val appName: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseMetadataDto(
    @Json(name = "nextCursor") val nextCursor: String? = null
)

interface EpicLibraryServiceApi {
    @GET("library/api/public/items")
    @Headers(
        "User-Agent: EpicGamesLauncher/15.17.1-23270134+++Portal+Release-Live"
    )
    suspend fun getLibraryItems(
        @Header("Authorization") bearerToken: String,
        @Query("includeMetadata") includeMetadata: Boolean = true,
        @Query("cursor") cursor: String? = null
    ): Response<LibraryServiceResponseDto>
}

@JsonClass(generateAdapter = true)
data class LibraryServiceResponseDto(
    @Json(name = "records") val records: List<LibraryServiceRecordDto>? = null,
    @Json(name = "responseMetadata") val responseMetadata: ResponseMetadataDto? = null
)

@JsonClass(generateAdapter = true)
data class LibraryServiceRecordDto(
    @Json(name = "appName") val appName: String? = null,
    @Json(name = "catalogItemId") val catalogItemId: String? = null,
    @Json(name = "namespace") val namespace: String? = null,
    @Json(name = "productId") val productId: String? = null,
    @Json(name = "sandboxType") val sandboxType: String? = null
)

interface EpicLauncherAssetsApi {
    @GET("launcher/api/public/assets/{platform}")
    @Headers(
        "User-Agent: EpicGamesLauncher/15.17.1-23270134+++Portal+Release-Live"
    )
    suspend fun getAssets(
        @Header("Authorization") bearerToken: String,
        @Path("platform") platform: String = "Windows",
        @Query("label") label: String = "Live"
    ): Response<List<LauncherAssetDto>>
}

@JsonClass(generateAdapter = true)
data class LauncherAssetDto(
    @Json(name = "appName") val appName: String? = null,
    @Json(name = "catalogItemId") val catalogItemId: String? = null,
    @Json(name = "namespace") val namespace: String? = null,
    @Json(name = "assetId") val assetId: String? = null,
    @Json(name = "buildVersion") val buildVersion: String? = null
)
