package com.lebentech.lebentechtorniquetes.retrofit.request

/**
 * Created by Gerardo Garzon on 09/01/23.
 */
class UserLoginRequest (
    // Headers
    var idDispositivo: String,
    var authorization: String,
    var body: UserBodyLoginRequest
)

class UserBodyLoginRequest (
    var id: String,
    var user: String,
    var password: String
)