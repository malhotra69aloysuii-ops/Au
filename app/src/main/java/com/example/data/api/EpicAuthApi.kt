package com.example.data.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.DELETE

interface EpicAuthApi {

    @FormUrlEncoded
    @POST("account/api/oauth/token")
    suspend fun exchangeCode(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("token_type") tokenType: String = "eg1"
    ): Response<TokenResponseDto>

    @FormUrlEncoded
    @POST("account/api/oauth/token")
    suspend fun refreshToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("token_type") tokenType: String = "eg1"
    ): Response<TokenResponseDto>

    @DELETE("account/api/oauth/sessions/kill/{accessToken}")
    suspend fun killSession(
        @Header("Authorization") bearerAuth: String,
        @Path("accessToken") accessToken: String
    ): Response<Unit>
}
