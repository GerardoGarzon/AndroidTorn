/**
 * Created by Gerardo Garzon on 27/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit

import android.content.Context
import com.lebentech.lebentechtorniquetes.managers.WriterManager
import com.lebentech.lebentechtorniquetes.repositories.base.BaseRepository
import com.lebentech.lebentechtorniquetes.retrofit.request.TokenRefreshRequest
import com.lebentech.lebentechtorniquetes.retrofit.service.TokenRefreshService
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.LogUtils
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import okhttp3.*

class RetrofitInterceptor constructor(context: Context) : Interceptor, Authenticator {

    private var context: Context

    init {
        this.context = context
    }

    /**
     * Intercept the request and execute it, it will manage the response and will save the log
     * if the response is not successfully
     * It will save the logs with the request and response information, it will not save the server
     * url for security reasons
     * When the interceptor reads a error response it will resend the request adding 1 to the error
     * count, if the error count is equals to 3 it will change the server endpoint and restart the
     * process
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val writerManager = WriterManager()
        var countErrors = 0
        val startTimeNs = System.nanoTime()
        val request = chain.request()
        var response = chain.proceed(request)
        var endTimeNS = System.nanoTime()

        if (response.isSuccessful) {
            return response
        } else {
            writerManager.createErrorLog(request, response, startTimeNs, endTimeNS)
            while ( countErrors < 3 || response.isSuccessful) {
                try {
                    response = chain.proceed(request)
                    endTimeNS = System.nanoTime()
                    if (response.isSuccessful) {
                        break
                    } else {
                        writerManager.createErrorLog(request, response, startTimeNs, endTimeNS)
                    }
                } catch (ex: Exception) {
                    LogUtils.printLogError("Interceptor", ex.localizedMessage ?: "", ex)
                }
                countErrors += 1
            }

            if (countErrors == 3 && !response.isSuccessful) {
                BaseRepository.activateServerFlag()
            }

            return response
        }
    }

    /**
     * When the API call return a 401 code for Unauthorized request it means that the token is
     * expired, then it will make a synchronous call to the refresh token api
     * Authenticator implementation should call the request again after the token is refreshed
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshedToken = newToken
        return response.request()
            .newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", refreshedToken)
            .build()
    }

    /**
     * Execute a synchronous api call to the refresh token service and it will save it again in
     * the shared preferences,
     * It will return the new token to send it through the headers in the new request
     */
    private val newToken: String
        get() {
            val expiredToken = Utils.getPrivatePreferences(
                context,
                Constants.TOKEN_KEY
            ).replace("Bearer ", Constants.EMPTY_STRING)

            val refreshToken = Utils.getPrivatePreferences(
                context,
                Constants.TOKEN_REFRESH_KEY
            )

            val androidID = Utils.getAndroidID(context)
            val service = RequestManager.getClient(SettingsViewModel.shared.SERVER_ENDPOINT)
                                        .create(TokenRefreshService::class.java)
            val request = TokenRefreshRequest(
                expiredToken,
                refreshToken,
                androidID,
                Utils.getIpOrigin(context),
                Constants.DEVICE_ORIGIN
            )
            val response = service.sendTokenRefreshRequest(request).execute().body()
            return if (response != null) {
                if (response.data != null) {
                    Utils.setPrivatePreferences(Constants.TOKEN_KEY, "Bearer " + response.data.token, context)
                    Utils.setPrivatePreferences(Constants.TOKEN_REFRESH_KEY, response.data.tokenRefresh, context)
                    "Bearer ${response.data.token}"
                } else {
                    Constants.EMPTY_STRING
                }
            } else {
                Constants.EMPTY_STRING
            }
        }
}