package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GameEntity
import com.example.data.local.toDomain
import com.example.domain.model.EpicTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EpicLibrary", appName)
  }

  @Test
  fun `game entity maps to domain correctly`() {
    val entity = GameEntity(
      id = "test_game_1",
      name = "Fortnite",
      namespace = "fn",
      imageUrl = "https://example.com/cover.jpg",
      developer = "Epic Games",
      description = "Battle Royale game",
      ownedSince = 1000L
    )
    val domain = entity.toDomain()
    assertEquals("test_game_1", domain.id)
    assertEquals("Fortnite", domain.name)
    assertEquals("fn", domain.namespace)
    assertEquals("Epic Games", domain.developer)
  }

  @Test
  fun `token expiry calculation behaves properly`() {
    val nonExpiredTokens = EpicTokens(
      accessToken = "token_abc",
      refreshToken = "token_xyz",
      expiresIn = 3600L,
      accountId = "acc_1",
      displayName = "PlayerOne",
      fetchedAt = System.currentTimeMillis()
    )
    assertFalse(nonExpiredTokens.isExpired())

    val expiredTokens = EpicTokens(
      accessToken = "token_abc",
      refreshToken = "token_xyz",
      expiresIn = 60L,
      accountId = "acc_1",
      displayName = "PlayerOne",
      fetchedAt = System.currentTimeMillis() - 70000L
    )
    assertTrue(expiredTokens.isExpired())
  }
}
