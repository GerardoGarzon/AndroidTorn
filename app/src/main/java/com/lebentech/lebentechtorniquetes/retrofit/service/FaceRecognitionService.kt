/**
 * Created by Gerardo Garzon on 16/01/23.
 */

package com.lebentech.lebentechtorniquetes.retrofit.service

import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.request.FaceRecognitionRequest
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import retrofit2.Call
import retrofit2.http.*

interface FaceRecognitionService {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
        "Connection: keep-alive"
    )
    @POST("/LebTorniquetes/api/Reconocimiento/Identificacion")
    fun sendFaceRecognitionRequest(
        @Body body: FaceRecognitionRequest
    ): Call<GeneralResponse<EmployeeInfoResponse>>
}