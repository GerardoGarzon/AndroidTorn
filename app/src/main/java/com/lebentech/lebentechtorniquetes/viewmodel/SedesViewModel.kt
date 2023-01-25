/**
 * Created by Gerardo Garzon on 04/01/23.
 */
package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lebentech.lebentechtorniquetes.interfaces.GeneralResponseListener
import com.lebentech.lebentechtorniquetes.models.SedesModel
import com.lebentech.lebentechtorniquetes.repositories.SedesRepository

class SedesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SedesRepository()
    private var listener: GeneralResponseListener? = null
    fun setListener(listener: GeneralResponseListener?) {
        this.listener = listener
    }

    fun getAlternativeSedes(model: SedesModel?) {
        repository.sendAlternativesSedesRequest(
            model!!,
            listener!!,
            getApplication<Application>().applicationContext
        )
    }
}