/**
 * Created by Gerardo Garzon on 27/12/22.
 */

package com.lebentech.lebentechtorniquetes.interfaces

interface DeviceLoginRequestListener {
    fun onSuccess(code: Int)
    fun onFailure()
}