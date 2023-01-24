/**
 * Created by Gerardo Gonzalez on 24/11/22.
 */

package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lebentech.lebentechtorniquetes.repositories.AppStatusRepository

class AppStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppStatusRepository()

    fun getAppStatusValues(index: Int): Pair<String, String> {
        return repository.getAppStatus(index)
    }
}