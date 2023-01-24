/**
 * Created by Gerardo Garzon on 05/01/23.
 */

package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lebentech.lebentechtorniquetes.repositories.LifeTestRepository

class LifeTestViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: LifeTestRepository = LifeTestRepository()

    fun sendLifeTest() {
        repository.sendLifeTest(getApplication<Application>().applicationContext)
    }
}