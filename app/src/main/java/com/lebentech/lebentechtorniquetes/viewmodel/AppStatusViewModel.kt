/**
 * Created by Gerardo Gonzalez on 24/11/22.
 */

package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lebentech.lebentechtorniquetes.repositories.AppStatusRepository
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils

class AppStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppStatusRepository()

    fun getAppStatusValues(index: Int): Pair<String, String> {
        return repository.getAppStatus(index)
    }

    fun setServerErrorFlag() {
        Utils.setPrivatePreferences(Constants.SERVER_ERROR_KEY, Constants.SERVER_ERROR_ON, getApplication<Application>().applicationContext)
    }
}