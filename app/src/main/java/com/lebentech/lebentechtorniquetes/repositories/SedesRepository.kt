/**
 * Created by Gerardo Garzon on 03/01/23.
 */

package com.lebentech.lebentechtorniquetes.repositories

import android.content.Context
import com.lebentech.lebentechtorniquetes.database.DatabaseHelper
import com.lebentech.lebentechtorniquetes.interfaces.GeneralResponseListener
import com.lebentech.lebentechtorniquetes.models.SedesModel
import com.lebentech.lebentechtorniquetes.repositories.base.BaseRepository
import com.lebentech.lebentechtorniquetes.retrofit.RequestManager
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.SedeResponse
import com.lebentech.lebentechtorniquetes.retrofit.service.SedePriorityService
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SedesRepository: BaseRepository() {
    fun sendAlternativesSedesRequest(model: SedesModel, listener: GeneralResponseListener, context: Context) {

        val service = RequestManager.getClient(SettingsViewModel.shared.SERVER_ENDPOINT)
                                    .create(SedePriorityService::class.java)
        val initiateSedes = service.sendSedePriorityRequest(model.token, model.idAndroid, model.idSede)
        initiateSedes.enqueue(object : Callback<GeneralResponse<List<SedeResponse>>> {
            override fun onResponse(call: Call<GeneralResponse<List<SedeResponse>>>, response: Response<GeneralResponse<List<SedeResponse>>>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val list = response.body()!!.data
                        saveSedes(list, context)
                        listener.onSuccess()
                    } else {
                        if ( !checkGeneralRetry(model, listener, context, ::sendAlternativesSedesRequest ) ) {
                            listener.onFailure()
                        }
                    }
                } else {
                    if ( !checkGeneralRetry(model, listener, context, ::sendAlternativesSedesRequest ) ) {
                        listener.onFailure()
                    }
                }
            }

            override fun onFailure(call: Call<GeneralResponse<List<SedeResponse>>>, t: Throwable) {
                if ( !checkGeneralRetry(model, listener, context, ::sendAlternativesSedesRequest ) ) {
                    listener.onFailure()
                }
            }
        })
    }

    fun saveSedes(listSedes: List<SedeResponse>, context: Context?) {
        val db = DatabaseHelper(context)
        db.insertSedes(listSedes)
    }
}