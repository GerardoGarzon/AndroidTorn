/**
 * Created by Gerardo Garzon on 23/12/22.
 */
package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lebentech.lebentechtorniquetes.interfaces.LoginRequestListener
import com.lebentech.lebentechtorniquetes.repositories.LoginRepository
import com.lebentech.lebentechtorniquetes.retrofit.request.UserBodyLoginRequest
import com.lebentech.lebentechtorniquetes.retrofit.request.UserLoginRequest
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.getAndroidID
import com.lebentech.lebentechtorniquetes.utils.Utils.Companion.getPrivatePreferences

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val loginRepository = LoginRepository()
    var inProgress = MutableLiveData<Boolean>()
    var loginProcess = MutableLiveData<Int>()
    var minutesTimer = MutableLiveData<Int>()

    init {
        loginProcess.postValue(Constants.NOT_LOGGED_IN)
    }

    fun loginUser(user: String, pass: String) {
        inProgress.postValue(true)
        loginProcess.postValue(Constants.LOGIN_PROCESS)
        val authToken = getPrivatePreferences(
            getApplication<Application>().applicationContext,
            Constants.TOKEN_KEY
        )
        val loginModel = UserLoginRequest(
            getAndroidID(getApplication<Application>().applicationContext),
            authToken,
            UserBodyLoginRequest(
                Constants.EMPTY_STRING,
                user,
                pass
            )
        )
        loginRepository.sendUserLoginRequest( loginModel,
            object : LoginRequestListener {

                override fun onSuccess(timer: Int, code: Int) {
                    inProgress.postValue(false)
                    minutesTimer.postValue(timer)
                    if (code == 200) {
                        loginProcess.postValue(Constants.LOGGED_IN)
                    } else if (code == 404) {
                        loginProcess.postValue(Constants.UNAUTHORIZED_DEVICE)
                    } else if (code == 0) {
                        loginProcess.postValue(Constants.ERROR_IN_LOGIN)
                    }
                }

                override fun onFailure() {
                    inProgress.postValue(false)
                    loginProcess.postValue(Constants.ERROR_IN_LOGIN)
                }

            }, getApplication<Application>().applicationContext)
    }

    val progress: LiveData<Boolean>
        get() = inProgress
}