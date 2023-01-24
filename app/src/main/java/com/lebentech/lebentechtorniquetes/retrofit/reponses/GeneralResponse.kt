/**
 * Created by Gerardo Garzon on 23/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit.reponses

import com.google.gson.annotations.SerializedName

data class GeneralResponse<T> (
    @SerializedName("codigo") var code: Int,
    @SerializedName("mensaje") var message: String,
    @SerializedName("total") var total: Int,
    @SerializedName("vigencia") var validity: Int,

    // Data fields for different kind of responses
    @SerializedName("datos") var data: T
)