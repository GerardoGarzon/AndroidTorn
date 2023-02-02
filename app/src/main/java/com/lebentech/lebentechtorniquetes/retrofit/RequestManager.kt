/**
 * Created by Gerardo Garzon on 26/12/22.
 */

package com.lebentech.lebentechtorniquetes.retrofit

import com.lebentech.lebentechtorniquetes.LebentechApplication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class RequestManager {
    companion object {
        private const val TIMEOUT_IN_SECONDS: Long = 20
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

        private fun getClientConfiguration(): OkHttpClient {
            val retrofitInterceptor = LebentechApplication.interceptor
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("10.50.89.12", 8888))
            return OkHttpClient.Builder()
                .authenticator(retrofitInterceptor)
                .addInterceptor(retrofitInterceptor)
                .connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                //.proxy(proxy)
                .build()
        }
    }
}