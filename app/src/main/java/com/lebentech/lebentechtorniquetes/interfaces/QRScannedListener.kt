/**
 * Created by Gerardo Garzon on 28/12/22.
 */

package com.lebentech.lebentechtorniquetes.interfaces

interface QRScannedListener {
    fun onSuccess()
    fun onFailure()
    fun onCaptured()
}