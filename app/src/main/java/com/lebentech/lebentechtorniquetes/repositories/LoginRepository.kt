/**
 * Created by Gerardo Garzon on 26/12/22.
 */

package com.lebentech.lebentechtorniquetes.repositories

import android.content.Context
import android.util.Log
import com.lebentech.lebentechtorniquetes.database.DatabaseHelper
import com.lebentech.lebentechtorniquetes.interfaces.DeviceLoginRequestListener
import com.lebentech.lebentechtorniquetes.interfaces.LoginRequestListener
import com.lebentech.lebentechtorniquetes.repositories.base.BaseRepository
import com.lebentech.lebentechtorniquetes.retrofit.RequestManager
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.TokenResponse
import com.lebentech.lebentechtorniquetes.retrofit.request.DeviceLoginRequest
import com.lebentech.lebentechtorniquetes.retrofit.request.UserLoginRequest
import com.lebentech.lebentechtorniquetes.retrofit.service.DeviceLoginService
import com.lebentech.lebentechtorniquetes.retrofit.service.LoginService
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository : BaseRepository() {

    fun sendUserLoginRequest(model: UserLoginRequest, listener: LoginRequestListener, context: Context, isRetry: Boolean = false) {

        val service = RequestManager.getClient(SettingsViewModel.shared.serverEndpoint)
                                    .create(LoginService::class.java)

        val initiateLogin = service.sendLoginRequest(
            model.authorization,
            model.idDispositivo,
            Constants.DEVICE_ORIGIN,
            model.body
        )

        initiateLogin.enqueue(object : Callback<GeneralResponse<TokenResponse>> {
            override fun onResponse(call: Call<GeneralResponse<TokenResponse>>, response: Response<GeneralResponse<TokenResponse>>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val minutes = response.body()!!.validity
                        val code = response.body()!!.code

                        listener.onSuccess(minutes, code)
                    }
                } else {
                    if (!checkGeneralRetry(model, listener, context, ::sendUserLoginRequest )) {
                        listener.onFailure()
                    }
                }
            }

            override fun onFailure(call: Call<GeneralResponse<TokenResponse>>, t: Throwable) {
                if (!checkGeneralRetry(model, listener, context, ::sendUserLoginRequest )) {
                    listener.onFailure()
                }
            }
        })
    }

    fun sendDeviceLoginRequest(model: DeviceLoginRequest, listener: DeviceLoginRequestListener, priority: Int, context: Context) {
        val service = RequestManager.getClient(SettingsViewModel.shared.serverEndpoint, true)
                                    .create(DeviceLoginService::class.java)

        val initiateLogin = service.sendDeviceRequest(
            model.idDispositivo,
            model.secretKey,
            Constants.DEVICE_ORIGIN,
            Utils.getIpOrigin(context)
        )

        initiateLogin.enqueue(object : Callback<GeneralResponse<TokenResponse>> {
            override fun onResponse(call: Call<GeneralResponse<TokenResponse>>, response: Response<GeneralResponse<TokenResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val code = response.body()!!.code
                    if (code == 200) {
                        val tokenJWT = response.body()!!.data.token
                        val tokenRefresh = response.body()!!.data.tokenRefresh
                        val idSede = response.body()!!.data.idSede
                        val sedeName = "Torre " + response.body()!!.data.sedeName

                        if (tokenJWT == null || tokenRefresh == null || idSede == null || sedeName == null) {
                            listener.onFailure()
                        } else {
                            Utils.setPrivatePreferences(Constants.TOKEN_KEY, "Bearer $tokenJWT", context)
                            Utils.setPrivatePreferences(Constants.TOKEN_REFRESH_KEY, tokenRefresh, context)
                            Utils.setPrivatePreferences(Constants.ID_SEDE_KEY, idSede, context)
                            Utils.setPrivatePreferences(Constants.SEDE_NAME_KEY, sedeName, context)
                            Utils.setPrivatePreferences(Constants.SEDE_PRIORITY_ID, priority, context)
                            Utils.setPrivatePreferences(Constants.SERVER_ERROR_KEY, Constants.SERVER_ERROR_OFF, context)

                            val database = DatabaseHelper(context)
                            database.deleteSedes()

                            listener.onSuccess(code)
                        }
                    } else {
                        listener.onFailure()
                    }
                } else {
                    listener.onFailure()
                }
            }

            override fun onFailure(call: Call<GeneralResponse<TokenResponse>>, t: Throwable) {
                listener.onFailure()
            }
        })
    }
}