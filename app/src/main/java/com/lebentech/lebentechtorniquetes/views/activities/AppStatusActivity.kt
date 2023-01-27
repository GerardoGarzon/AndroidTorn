package com.lebentech.lebentechtorniquetes.views.activities

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieDrawable
import com.lebentech.lebentechtorniquetes.databinding.ActivityAppStatusBinding
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.views.activities.base.BaseActivity
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.AppStatusViewModel
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel

class AppStatusActivity : BaseActivity() {
    private val screen: Screen = Screen(false, Constants.STATUS_ACTIVITY)
    private lateinit var binding: ActivityAppStatusBinding
    private lateinit var viewModel: AppStatusViewModel
    private var actualStatus: Int = 0
    private var statusValues: Pair<String, String>? = null

    override fun getViewBinding(): ViewBinding {
        if (!this::binding.isInitialized) {
            binding = ActivityAppStatusBinding.inflate(layoutInflater)
        }
        return binding
    }

    override fun getScreen(): Screen {
        return screen
    }

    @SuppressLint("DiscouragedApi")
    override fun prepareComponents() {
        hideActionBar()

        viewModel = ViewModelProvider(this)[AppStatusViewModel::class.java]

        val statusID: Int = intent.getIntExtra("appStatus", 0)
        actualStatus = statusID
        statusValues = viewModel.getAppStatusValues(statusID)
        val imageName = this.resources.getIdentifier(statusValues?.first, "raw", packageName)
        val stringName = this.resources.getIdentifier(statusValues?.second, "string", packageName)

        binding.statusTextView.text = this.resources.getString(stringName)

        binding.statusImageView.setAnimation(imageName)
        binding.statusImageView.repeatCount = LottieDrawable.INFINITE
        binding.statusImageView.speed = 1.3F
        binding.statusImageView.playAnimation()

        if(statusID == 1) {
            binding.configButton.isVisible = false
        } else if (statusID == 3) {
            resetDeviceInfo()
            viewModel.setServerErrorFlag()
            binding.statusImageView.speed = 1.0F
        }

        binding.configButton.setOnClickListener {
            openSedeActivity(isFinish = true, startTimer = false, minutes = 0)
        }
    }
}