/**
 * Created by Gerardo Garzon on 09/01/23.
 */

package com.lebentech.lebentechtorniquetes

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.lebentech.lebentechtorniquetes.retrofit.RetrofitInterceptor

class LebentechApplication: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var interceptor: RetrofitInterceptor
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        interceptor = RetrofitInterceptor(applicationContext)
        appContext = applicationContext
    }
}