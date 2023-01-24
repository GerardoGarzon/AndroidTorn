/**
 * Created by Gerardo Garzon on 27/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit.service

import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.TokenResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface DeviceLoginService {

    @POST("/LebTorniquetes/api/LoginServer/Solicitud-Inicio?")
    fun sendDeviceRequest(
        @Query("idDispositivo") idAndroid: String,
        @Query("secretKey") secretKey: String,
        @Query("origen") origin: String,
        @Query("ipOrigen") ipOrigin: String
    ): Call<GeneralResponse<TokenResponse>>
}