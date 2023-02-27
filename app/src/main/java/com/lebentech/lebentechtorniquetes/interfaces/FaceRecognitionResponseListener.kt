/**
 * Created by Gerardo Garzon on 17/01/23.
 */

package com.lebentech.lebentechtorniquetes.interfaces

import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse

interface FaceRecognitionResponseListener {
    fun onSuccess(model: GeneralResponse<EmployeeInfoResponse>)
    fun onFailure(code: Int, errorMessage: String)
}