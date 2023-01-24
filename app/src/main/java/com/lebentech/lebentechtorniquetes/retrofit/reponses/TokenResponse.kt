/**
 * Created by Gerardo Garzon on 27/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit.reponses

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("token") var token: String,
    @SerializedName("tokenRefresh") var tokenRefresh: String,
    @SerializedName("idSede") var idSede: String,
    @SerializedName("sede") var sedeName: String
)