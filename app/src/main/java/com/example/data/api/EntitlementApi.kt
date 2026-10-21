package com.example.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface EntitlementApi {

    @GET("entitlement/api/account/{accountId}/entitlements")
    suspend fun getEntitlements(
        @Header("Authorization") bearerAuth: String,
        @Path("accountId") accountId: String,
        @Query("start") start: Int = 0,
        @Query("count") count: Int = 1000
    ): Response<List<EntitlementDto>>
}
