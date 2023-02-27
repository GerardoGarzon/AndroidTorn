/**
 * Created by Gerardo Garzon on 17/01/23.
 */

package com.lebentech.lebentechtorniquetes.interfaces

import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse

interface PhotoTakenListener {
    fun onSuccess(model: GeneralResponse<EmployeeInfoResponse>)
    fun onFailure(error: Int, errorMessage: String)
    fun photoTaken()
}