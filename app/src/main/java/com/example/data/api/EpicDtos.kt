package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "account_id") val accountId: String,
    @Json(name = "displayName") val displayName: String? = null
)

@JsonClass(generateAdapter = true)
data class EntitlementDto(
    @Json(name = "id") val id: String?,
    @Json(name = "entitlementName") val entitlementName: String?,
    @Json(name = "namespace") val namespace: String?,
    @Json(name = "catalogItemId") val catalogItemId: String?,
    @Json(name = "accountId") val accountId: String?,
    @Json(name = "entitlementType") val entitlementType: String? = null,
    @Json(name = "group") val group: String? = null,
    @Json(name = "created") val created: String? = null
)

@JsonClass(generateAdapter = true)
data class KeyImageDto(
    @Json(name = "type") val type: String?,
    @Json(name = "url") val url: String?
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
    @Json(name = "path") val path: String? = null
)

@JsonClass(generateAdapter = true)
data class CatalogItemDto(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "description") val description: String? = null,
    @Json(name = "developer") val developer: String? = null,
    @Json(name = "namespace") val namespace: String? = null,
    @Json(name = "catalogItemType") val catalogItemType: String? = null,
    @Json(name = "itemType") val itemType: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "categories") val categories: List<CategoryDto>? = null,
    @Json(name = "keyImages") val keyImages: List<KeyImageDto>? = null
)
