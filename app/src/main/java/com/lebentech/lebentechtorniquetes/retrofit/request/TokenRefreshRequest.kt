package com.lebentech.lebentechtorniquetes.retrofit.request

/**
 * Created by Gerardo Garzon on 09/01/23.
 */
class TokenRefreshRequest (
    // Body
    var tokenExpirado: String,
    var tokenRefresh: String,
    var idDispositivo: String,
    var ipOrigen: String,
    var origen: String
)