/**
 * Created by Gerardo Garzon on 22/12/22.
 */

package com.lebentech.lebentechtorniquetes.utils

import android.util.Log
import androidx.viewbinding.BuildConfig

class LogUtils {
    companion object {
        fun printLog(tag: String?, body: String) {
            if (body.isNotEmpty()) {
                Log.d(tag, body)
            }
        }

        fun printLog(body: String) {
            val tag = Constants.LOG_TAG
            if (BuildConfig.DEBUG && body.isNotEmpty()) {
                Log.d(tag, body)
            }
        }

        fun printLogError(tag: String?, body: String, error: Exception?) {
            if (BuildConfig.DEBUG && body.isNotEmpty()) {
                Log.e(tag, body, error)
            }
        }
    }
}