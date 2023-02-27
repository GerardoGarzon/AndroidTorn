/**
 * Created by Gerardo Garzon on 28/12/22.
 */
package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.lebentech.lebentechtorniquetes.interfaces.DeviceLoginRequestListener
import com.lebentech.lebentechtorniquetes.interfaces.GeneralResponseListener
import com.lebentech.lebentechtorniquetes.models.SedesModel
import com.lebentech.lebentechtorniquetes.repositories.LoginRepository
import com.lebentech.lebentechtorniquetes.retrofit.request.DeviceLoginRequest
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.getAndroidID
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.getPrivatePreferences
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.getSha256
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.setPrivatePreferences
import java.util.*

class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var sedesViewModel: SedesViewModel

    private lateinit var listener: DeviceLoginRequestListener

    private val loginRepository = LoginRepository()

    val progress = MutableLiveData<Boolean>()

    fun setListener(listener: DeviceLoginRequestListener) {
        this.listener = listener
    }

    fun setSedesViewModel(viewModel: SedesViewModel) {
        this.sedesViewModel = viewModel
    }

    fun loginDevice(priority: Int) {
        progress.postValue(true)

        if ( !URLUtil.isValidUrl(SettingsViewModel.shared.serverEndpoint) ) {
            resetDeviceInfo()
            listener.onFailure()
            progress.postValue(false)
            return
        }

        val model = DeviceLoginRequest(
            getAndroidID(getApplication<Application>().applicationContext),
            getSha256(Constants.SECRET_KEY).lowercase(Locale.getDefault()) ?: ""
        )

        loginRepository.sendDeviceLoginRequest(model, object : DeviceLoginRequestListener {
            override fun onSuccess(code: Int) {
                val sedesModel = SedesModel(
                    getPrivatePreferences(
                        getApplication<Application>().applicationContext,
                        Constants.TOKEN_KEY
                    ),
                    getAndroidID(getApplication<Application>().applicationContext),
                    getPrivatePreferences(
                        getApplication<Application>().applicationContext,
                        Constants.ID_SEDE_KEY
                    )
                )
                sedesViewModel.setListener(object : GeneralResponseListener {
                    override fun onSuccess() {
                        progress.postValue(false)
                        listener.onSuccess(code)
                    }

                    override fun onFailure() {
                        resetDeviceInfo()
                        listener.onFailure()
                        progress.postValue(false)
                    }
                })
                sedesViewModel.getAlternativeSedes(sedesModel)
            }

            override fun onFailure() {
                resetDeviceInfo()
                listener.onFailure()
                progress.postValue(false)
            }
        }, priority, getApplication<Application>().applicationContext)
    }

    fun resetDeviceInfo() {
        SettingsViewModel.shared.serverEndpoint = ""
        setPrivatePreferences(Constants.TOKEN_KEY, "", getApplication<Application>().applicationContext)
        setPrivatePreferences(Constants.TOKEN_REFRESH_KEY, "", getApplication<Application>().applicationContext)
        setPrivatePreferences(Constants.ID_SEDE_KEY, "", getApplication<Application>().applicationContext)
        setPrivatePreferences(Constants.SEDE_PRIORITY_ID, "", getApplication<Application>().applicationContext)
        setPrivatePreferences(Constants.SEDE_NAME_KEY, "", getApplication<Application>().applicationContext)
    }
}