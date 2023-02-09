/**
 * Created by Gerardo Garzon on 03/01/23.
 */
package com.lebentech.lebentechtorniquetes.retrofit.service

import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.SedeResponse
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface SedePriorityService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Connection: keep-alive"
    )
    @GET("/LebTorniquetes/api/SedesAlter/Sedes-Alternativas")
    fun sendSedePriorityRequest(
        @Header("Authorization") auth: String,
        @Query("idDispositivo") idAndroid: String,
        @Query("idSede") idSede: String,
        @Query("charlesOrigen") charlesOrigin: String = SettingsViewModel.shared.serverEndpoint
    ): Call<GeneralResponse<List<SedeResponse>>>
}