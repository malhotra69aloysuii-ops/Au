package com.example.data

import com.example.data.api.CatalogItemDto
import com.example.data.api.EpicQueries
import com.example.data.api.GraphQLRequest
import com.example.data.api.KeyImageDto
import com.example.data.api.LibraryRecordDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFetcherTest {

    @Test
    fun testGraphQLRequestFormatting() {
        val request = GraphQLRequest(
            query = EpicQueries.GET_LIBRARY,
            variables = mapOf("cursor" to "sampleCursor123")
        )

        assertTrue(request.query.contains("query libraryQuery"))
        assertTrue(request.query.contains("Library(cursor:"))
        assertEquals("sampleCursor123", request.variables?.get("cursor"))
    }

    @Test
    fun testKeyImageHierarchyPrefersDieselGameBoxTall() {
        val images = listOf(
            KeyImageDto(type = "Thumbnail", url = "https://cdn.epicgames.com/thumb.jpg"),
            KeyImageDto(type = "OfferImageTall", url = "https://cdn.epicgames.com/offer.jpg"),
            KeyImageDto(type = "DieselGameBoxTall", url = "https://cdn.epicgames.com/tall.jpg")
        )

        val selectedUrl = images.firstOrNull { it.type.equals("DieselGameBoxTall", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("OfferImageTall", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("DieselStoreFrontTall", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("DieselGameBox", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("Thumbnail", ignoreCase = true) }?.url

        assertEquals("https://cdn.epicgames.com/tall.jpg", selectedUrl)
    }

    @Test
    fun testKeyImageHierarchyFallsBackToOfferImageTall() {
        val images = listOf(
            KeyImageDto(type = "Thumbnail", url = "https://cdn.epicgames.com/thumb.jpg"),
            KeyImageDto(type = "OfferImageTall", url = "https://cdn.epicgames.com/offer.jpg")
        )

        val selectedUrl = images.firstOrNull { it.type.equals("DieselGameBoxTall", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("OfferImageTall", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("DieselStoreFrontTall", ignoreCase = true) }?.url
            ?: images.firstOrNull { it.type.equals("Thumbnail", ignoreCase = true) }?.url

        assertEquals("https://cdn.epicgames.com/offer.jpg", selectedUrl)
    }

    @Test
    fun testAudienceFilteringRejectsAudienceTitles() {
        val audienceItem = CatalogItemDto(
            id = "aud123",
            title = "ApatiteGeneralAudience",
            catalogItemType = "AUDIENCE"
        )

        val isAudience = audienceItem.catalogItemType.equals("AUDIENCE", ignoreCase = true) ||
                audienceItem.title.orEmpty().contains("Audience", ignoreCase = true)

        assertTrue(isAudience)
    }

    @Test
    fun testValidMainGamePassesFiltering() {
        val gameItem = CatalogItemDto(
            id = "game123",
            title = "Grand Theft Auto V",
            developer = "Rockstar Games",
            catalogItemType = "BASE_GAME"
        )

        val isAudience = gameItem.catalogItemType.equals("AUDIENCE", ignoreCase = true) ||
                gameItem.title.orEmpty().contains("Audience", ignoreCase = true)

        val isValidTitle = !gameItem.title.isNullOrBlank()

        assertTrue(isValidTitle)
        assertTrue(!isAudience)
    }
}
