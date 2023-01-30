/**
 * Created by Gerardo Garzon on 26/12/22.
 */
package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.getPrivatePreferences
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.setPrivatePreferences

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    var SERVER_ENDPOINT = Constants.EMPTY_STRING

    private val appContext: Context
        get() = getApplication<Application>().applicationContext

    var serverEndpoint: String
        get() = getPrivatePreferences(appContext, Constants.ENDPOINT_KEY)
        set(endpoint) {
            setPrivatePreferences(Constants.ENDPOINT_KEY, endpoint, getApplication<Application>().applicationContext)
        }

    fun loadPreferences() {
        shared.serverEndpoint = getPrivatePreferences(
            appContext, Constants.ENDPOINT_KEY
        )
    }

    companion object {
        lateinit var shared: SettingsViewModel
    }
}