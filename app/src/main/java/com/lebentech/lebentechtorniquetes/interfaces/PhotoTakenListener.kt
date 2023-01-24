/**
 * Created by Gerardo Garzon on 17/01/23.
 */

package com.lebentech.lebentechtorniquetes.interfaces

import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse

interface PhotoTakenListener {
    fun onSuccess(model: EmployeeInfoResponse)
    fun onFailure(error: Int)
    fun photoTaken()
}