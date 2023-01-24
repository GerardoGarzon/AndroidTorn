/**
 * Created by Gerardo Garzon on 03/01/23.
 */

package com.lebentech.lebentechtorniquetes.retrofit.reponses

import com.google.gson.annotations.SerializedName

class SedeResponse (
    @SerializedName("sede") var sedeName: String,
    @SerializedName("idSede") var idSede: String,
    @SerializedName("ipServidor") var sedeIP: String,
    @SerializedName("idPrioridad") var idPriority: Int
)