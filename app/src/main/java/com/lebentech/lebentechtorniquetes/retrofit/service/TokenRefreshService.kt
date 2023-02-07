/**
 * Created by Gerardo Garzon on 27/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit.service

import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.TokenResponse
import com.lebentech.lebentechtorniquetes.retrofit.request.TokenRefreshRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TokenRefreshService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Connection: keep-alive"
    )
    @POST("/LebTorniquetes/api/LoginServer/Refresh-Token")
    fun sendTokenRefreshRequest(
        @Body body: TokenRefreshRequest?
    ): Call<GeneralResponse<TokenResponse>>
}