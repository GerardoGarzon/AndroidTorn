/**
 * Created by Gerardo Garzon on 03/01/23.
 */

package com.lebentech.lebentechtorniquetes.repositories.base

import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.lebentech.lebentechtorniquetes.database.DatabaseHelper
import com.lebentech.lebentechtorniquetes.interfaces.ServerErrorListener
import com.lebentech.lebentechtorniquetes.retrofit.RequestManager
import com.lebentech.lebentechtorniquetes.retrofit.service.DeviceLoginService
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import java.io.IOException
import java.util.*


open class BaseRepository {
    companion object {
        lateinit var serverErrorListener: ServerErrorListener
        var nextPriority: Int = 0
        var needRestart: Boolean = false

        fun activateServerFlag() {
            needRestart = true
        }

        @JvmName("setServerErrorListener1")
        fun setServerErrorListener(listener: ServerErrorListener) {
            this.serverErrorListener = listener
        }
    }

    fun changeServerEndpoint(context: Context): Boolean {
        val db = DatabaseHelper(context)
        val priority = Utils.getPrivatePreferences(context, Constants.SEDE_PRIORITY_ID, 0)
        val sedes = db.getSedes(priority)

        return if (sedes.isNotEmpty()) {
            nextPriority = sedes[0].idPriority
            SettingsViewModel.shared.SERVER_ENDPOINT = sedes[0].sedeIP
            true
        } else {

            false
        }
    }

    fun sendAsyncDeviceLogin(context: Context, nextPriority: Int): Boolean {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val service = RequestManager.getClient(SettingsViewModel.shared.SERVER_ENDPOINT, true)
            .create(DeviceLoginService::class.java)
        val idAndroid = Utils.getAndroidID(context)
        val secretKey = Utils.getSha256(Constants.SECRET_KEY)?.lowercase(Locale.getDefault()) ?: ""
        val initiateLogin = service.sendDeviceRequest(
            idAndroid,
            secretKey,
            Constants.DEVICE_ORIGIN,
            Utils.getIpOrigin(context)
        )

        Utils.setPrivatePreferences(Constants.SEDE_PRIORITY_ID, nextPriority, context)

        try {
            val body = initiateLogin.execute().body()
            return if (body != null) {
                val tokenJWT = body.data.token
                val tokenRefresh = body.data.tokenRefresh
                val idSede = body.data.idSede
                val sedeName = "Torre " + body.data.sedeName

                Utils.setPrivatePreferences(Constants.TOKEN_KEY, "Bearer $tokenJWT", context)
                Utils.setPrivatePreferences(Constants.TOKEN_REFRESH_KEY, tokenRefresh, context)
                Utils.setPrivatePreferences(Constants.ID_SEDE_KEY, idSede, context)
                Utils.setPrivatePreferences(Constants.SEDE_NAME_KEY, sedeName, context)
                Utils.setPrivatePreferences(Constants.SEDE_PRIORITY_ID, nextPriority, context)
                true
            } else {
                false
            }
        } catch (ex: IOException) {
            return false
        }
    }

    fun <T, S> checkGeneralRetry(model: T, listener: S, context: Context, function: (model: T, listener: S, context: Context) -> Unit ): Boolean {
        if ( needRestart ) {
            needRestart = false
            while ( changeServerEndpoint(context) ) {
                if ( sendAsyncDeviceLogin(context, nextPriority) ) {
                    function(model, listener, context)
                }
            }
            serverErrorListener.onServerError()
        }
        return false
    }
}