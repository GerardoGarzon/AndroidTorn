/**
 * Created by Gerardo Garzon on 26/12/22.
 */
package com.lebentech.lebentechtorniquetes.retrofit.service

import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.TokenResponse
import com.lebentech.lebentechtorniquetes.retrofit.request.UserBodyLoginRequest
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import retrofit2.Call
import retrofit2.http.*

interface LoginService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Connection: keep-alive"
    )
    @POST("/LebTorniquetes/api/LoginUsuarios/Sesion-Usuario?")
    fun sendLoginRequest(
        @Header("Authorization") auth: String,
        @Query("idDispositivo") idAndroid: String,
        @Query("origen") origin: String,
        @Body body: UserBodyLoginRequest,
        @Query("charlesOrigen") charlesOrigin: String = SettingsViewModel.shared.serverEndpoint
    ): Call<GeneralResponse<TokenResponse>>
}