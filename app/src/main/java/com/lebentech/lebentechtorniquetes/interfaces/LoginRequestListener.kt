/**
 * Created by Gerardo Garzon on 26/12/22.
 */

package com.lebentech.lebentechtorniquetes.interfaces

interface LoginRequestListener {
    fun onSuccess(timer: Int, code: Int)
    fun onFailure()
}