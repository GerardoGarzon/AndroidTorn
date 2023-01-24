/**
 * Created by Gerardo Garzon on 17/01/23.
 */

package com.lebentech.lebentechtorniquetes.repositories

import com.lebentech.lebentechtorniquetes.interfaces.FaceRecognitionResponseListener
import com.lebentech.lebentechtorniquetes.repositories.base.BaseRepository
import com.lebentech.lebentechtorniquetes.retrofit.RequestManager
import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.request.FaceRecognitionRequest
import com.lebentech.lebentechtorniquetes.retrofit.service.FaceRecognitionService
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FaceRecognitionRepository: BaseRepository() {
    fun sendFaceRecognitionRequest(model: FaceRecognitionRequest, listener: FaceRecognitionResponseListener) {

        val service = RequestManager.getClient(SettingsViewModel.shared.SERVER_ENDPOINT)
            .create(FaceRecognitionService::class.java)

        val initiateRecognition = service.sendFaceRecognitionRequest(model)

        initiateRecognition.enqueue(object: Callback<GeneralResponse<EmployeeInfoResponse>> {
            override fun onResponse( call: Call<GeneralResponse<EmployeeInfoResponse>>,  response: Response<GeneralResponse<EmployeeInfoResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    if ( response.body()?.code == 200) {
                        val employeeInfo = response.body()!!.data
                        listener.onSuccess(employeeInfo)
                    } else {
                        listener.onFailure(407)
                    }
                } else {
                    listener.onFailure(400)
                }
            }

            override fun onFailure( call: Call<GeneralResponse<EmployeeInfoResponse>>, t: Throwable) {
                listener.onFailure(500)
            }
        })
    }
}