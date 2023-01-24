package com.lebentech.lebentechtorniquetes.views.activities

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.lebentech.lebentechtorniquetes.databinding.ActivityQrScannerBinding
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.modules.cameraqr.CameraQRManager
import com.lebentech.lebentechtorniquetes.views.activities.base.BaseActivity
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils

class QrScannerActivity : BaseActivity() {
    private val screen: Screen = Screen(false, Constants.SCANNER_ACTIVITY)
    private lateinit var binding: ActivityQrScannerBinding
    private var cameraManager: CameraQRManager? = null

    override fun getViewBinding(): ViewBinding {
        if (!this::binding.isInitialized) {
            binding = ActivityQrScannerBinding.inflate(layoutInflater)
        }
        return binding
    }

    override fun getScreen(): Screen {
        return screen
    }

    override fun prepareComponents() {
        hideActionBar()

        cameraManager = CameraQRManager(this, binding.scannerView) { qrScanned ->
            runOnUiThread {
                /* Read the QR scanned, it is read as a base64 String
                   Then it is decoded and saved to call the device login service */
                val endpointConverted = Utils.getBase64String(qrScanned)
                SettingsViewModel.shared.serverEndpoint = endpointConverted
                SedeConfigActivity.listener.onCaptured()
                finish()
            }
        }

        binding.switchButton.setOnClickListener {
            cameraManager?.switchCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager?.setupCamera()
    }

    override fun onResume() {
        super.onResume()
        cameraManager?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        cameraManager?.stopCamera()
    }
}