package com.lebentech.lebentechtorniquetes.views.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.lebentech.lebentechtorniquetes.R
import com.lebentech.lebentechtorniquetes.databinding.ActivitySedeConfigBinding
import com.lebentech.lebentechtorniquetes.interfaces.DeviceLoginRequestListener
import com.lebentech.lebentechtorniquetes.interfaces.QRScannedListener
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.views.activities.base.BaseActivity
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.DeviceViewModel
import com.lebentech.lebentechtorniquetes.viewmodel.SedesViewModel

class SedeConfigActivity : BaseActivity() {
    private val screen: Screen = Screen(false, Constants.SEDE_ACTIVITY)
    private lateinit var binding: ActivitySedeConfigBinding
    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var viewModel: DeviceViewModel
    private lateinit var sedesViewModel: SedesViewModel

    companion object {
        lateinit var listener: QRScannedListener
    }

    private val runnable: Runnable = Runnable {
        openRecognitionCamera()
    }

    override fun getViewBinding(): ViewBinding {
        if (!this::binding.isInitialized) {
            binding = ActivitySedeConfigBinding.inflate(layoutInflater)
        }
        return binding
    }

    override fun getScreen(): Screen {
        return screen
    }

    @SuppressLint("ResourceAsColor")
    override fun prepareComponents() {
        hideActionBar()
        configureListener()
        configureViewModel()
        setTimerTask()

        binding.acceptButton.setOnClickListener {
            if (SettingsViewModel.shared.serverEndpoint == "") {
                Utils.createSnackBar(applicationContext, binding.lblWelcome ,applicationContext.getString(
                    R.string.lbl_no_configured_sede), R.color.colorGolden)
            } else {
                openRecognitionCamera()
            }
        }

        binding.configButton.setOnClickListener {
            openQRScannerActivity()
        }

        setAndroidID()
        setSedeName()
        setEndpoint()

        Utils.createSnackBar(applicationContext, binding.lblWelcome ,applicationContext.getString(
            R.string.lbl_access_correct), R.color.colorGreen)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBackPressed() {
        val text = binding.endpointEditText.text.toString()
        if (text == "") {
            Utils.createSnackBar(applicationContext, binding.lblWelcome ,applicationContext.getString(
                R.string.lbl_no_configured_sede), R.color.colorGolden)
        } else {
            openRecognitionCamera()
        }
    }

    /**
     * If the user close the configuration view before the timer is over, it will stop the runnable
     * to prevent duplicated intents
     */
    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(runnable)
    }

    /**
     * If the timer is enabled the activity will start the runnable that open the camera activity
     * when the session time is over
     */
    private fun setTimerTask() {
        val isTimerEnabled = intent.getBooleanExtra("startTimer", false)
        if (isTimerEnabled) {
            val minutes = intent.getIntExtra("setTimer", 5)
            handler.postDelayed(runnable, (60000 * minutes).toLong())
        }
    }

    /**
     * Print the endpoint from the server in the text-field of the view
     */
    private fun setEndpoint() {
        binding.endpointEditText.setText(SettingsViewModel.shared.serverEndpoint)
    }

    /**
     * Takes the id from the device and display it on the screen, this id is the same as the one
     * from adb devices command
     */
    @SuppressLint("HardwareIds")
    private fun setAndroidID() {
        val id: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        binding.androidID.text = String.format(
            Utils.getStringByIdName(this, "lbl_android_id"),
            id
        )
    }

    private fun setSedeName() {
        val sedeName = Utils.getPrivatePreferences(applicationContext, Constants.SEDE_NAME_KEY)
        if (sedeName == "") {
            binding.lblWelcome.text = applicationContext.getString(R.string.lbl_sede_title)
        } else {
            binding.lblWelcome.text = sedeName
        }
    }

    private fun configureListener() {
        listener = object : QRScannedListener {
            override fun onSuccess() {
                //
            }

            @SuppressLint("ResourceAsColor")
            override fun onFailure() {
                Utils.createSnackBar(applicationContext, binding.imgLogo, applicationContext.getString(R.string.lbl_user_pass_incorrect), R.color.materialRed)
            }

            override fun onCaptured() {
                // Send login request, because it is recently captured by the QR camera it should be with 0 priority
                viewModel.loginDevice(0)
            }
        }
    }

    private fun configureViewModel() {
        val context = this
        viewModel = ViewModelProvider(this)[DeviceViewModel::class.java]
        sedesViewModel = ViewModelProvider(this)[SedesViewModel::class.java]

        viewModel.progress.observe(this) { inProgress ->
            if (inProgress) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.setListener(object : DeviceLoginRequestListener {
            @SuppressLint("ResourceAsColor")
            override fun onSuccess(code: Int) {
                if (code == 200) {
                    openRecognitionCamera()
                } else if (code == 404) {
                    SettingsViewModel.shared.serverEndpoint = ""
                    Utils.createSnackBar(context, binding.lblWelcome, applicationContext.getString(R.string.lbl_unauthorized_device), R.color.materialRed)
                }
            }

            @SuppressLint("ResourceAsColor")
            override fun onFailure() {
                SettingsViewModel.shared.serverEndpoint = ""
                Utils.createSnackBar(context, binding.lblWelcome, applicationContext.getString(R.string.lbl_incorrect_endpoint), R.color.materialRed)
            }
        })

        viewModel.setSedesViewModel(sedesViewModel)
    }
}