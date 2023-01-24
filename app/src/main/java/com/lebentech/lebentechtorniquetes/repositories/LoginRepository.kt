/**
 * Created by Gerardo Garzon on 26/12/22.
 */

package com.lebentech.lebentechtorniquetes.repositories

import android.content.Context
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

        val service = RequestManager.getClient(SettingsViewModel.shared.SERVER_ENDPOINT)
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
                    checkRetry(model, listener, context)
                }
            }

            override fun onFailure(call: Call<GeneralResponse<TokenResponse>>, t: Throwable) {
                checkRetry(model, listener, context)
            }
        })
    }

    fun checkRetry(model: UserLoginRequest, listener: LoginRequestListener, context: Context) {
        if ( needRestart ) {
            needRestart = false
            while ( changeServerEndpoint(context) ) {
                if ( sendAsyncDeviceLogin(context, nextPriority) ) {
                    sendUserLoginRequest(model, listener, context)
                }
            }
            serverErrorListener.onServerError()
            listener.onFailure()
        } else {
            listener.onFailure()
        }
    }

    fun sendDeviceLoginRequest(model: DeviceLoginRequest, listener: DeviceLoginRequestListener, priority: Int, context: Context) {
        val service = RequestManager.getClient(SettingsViewModel.shared.SERVER_ENDPOINT, true)
                                    .create(DeviceLoginService::class.java)

        val initiateLogin = service.sendDeviceRequest(
            model.idDispositivo,
            model.secretKey,
            Constants.DEVICE_ORIGIN,
            Utils.getIpOrigin(context)
        )

        initiateLogin.enqueue(object : Callback<GeneralResponse<TokenResponse>> {
            override fun onResponse(call: Call<GeneralResponse<TokenResponse>>, response: Response<GeneralResponse<TokenResponse>>) {
                if (response.isSuccessful) {
                    assert(response.body() != null)
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