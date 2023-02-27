/**
 * Created by Gerardo Garzon on 26/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit

import android.annotation.SuppressLint
import com.lebentech.lebentechtorniquetes.LebentechApplication
import com.lebentech.lebentechtorniquetes.utils.LogUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


class RequestManager {
    companion object {
        private const val TIMEOUT_IN_SECONDS: Long = 2100
        private lateinit var retrofit: Retrofit

        /**
         * Returns the instance for the retrofit object
         */
        fun getClient(baseUrl: String, needRestart: Boolean = false): Retrofit {
            if (!this::retrofit.isInitialized || needRestart) {
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClientConfiguration())
                    .build()
            }
            return retrofit
        }

        /**
         * Create the retrofit client which contains the interceptor and proxy (For development process)
         */
        private fun getClientConfiguration(): OkHttpClient {
            val retrofitInterceptor = LebentechApplication.interceptor
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("10.50.89.16", 8888))
            return OkHttpClient.Builder()
                .authenticator(retrofitInterceptor)
                .addInterceptor(retrofitInterceptor)
                .connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                // .proxy(proxy)
                .build()
        }
    }
}