package com.lebentech.lebentechtorniquetes.retrofit.reponses

import com.google.gson.annotations.SerializedName

/**
 * Created by Gerardo Garzon on 17/01/23.
 */
class EmployeeInfoResponse (
    @SerializedName("clienteUnico") var employeeNumber: String,
    @SerializedName("nombre") var employeeName: String,
    @SerializedName("fechaNacimiento") var employeeBirthday: String
)