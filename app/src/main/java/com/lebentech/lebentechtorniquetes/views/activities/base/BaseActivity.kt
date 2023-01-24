/**
 * Created by Gerardo Garzon on 22/12/22.
 */

package com.lebentech.lebentechtorniquetes.views.activities.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.lebentech.lebentechtorniquetes.interfaces.NetworkListener
import com.lebentech.lebentechtorniquetes.managers.NetworkManager
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.views.activities.*

abstract class BaseActivity: AppCompatActivity() {

    /**
     * Get the view binding of each activity to manage its objects
     */
    protected abstract fun getViewBinding(): ViewBinding

    /**
     * Information about each screen displayed, isNetworkRequired and TAG name
     */
    protected abstract fun getScreen(): Screen

    /**
     * Override this method to prepare the components that are required for each activity
     * like the viewModel, permissions, services, threads, etc.
     */
    protected abstract fun prepareComponents()

    /**
     * Network manager instance to monitor the network status of the phone
     */
    private var networkManager: NetworkManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getViewBinding().root)
        applicationSettings()
        prepareComponents()
    }

    /**
     * System configurations for the application
     */
    private fun applicationSettings() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Full screen activity for those activities that needs it
     */
    protected open fun setFullScreenActivity() {
        hideActionBar()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    /**
     * No action bar
     */
    protected open fun hideActionBar() {
        val actionBar = supportActionBar
        actionBar?.hide()
    }


    protected open fun openRecognitionCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
        finish()
    }

    protected open fun openLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    protected open fun openAppStatusActivity(index: Int) {
        val intent = Intent(this, AppStatusActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("appStatus", index)
        startActivity(intent)
        finish()
    }

    protected open fun openSedeActivity(isFinish: Boolean, startTimer: Boolean, minutes: Int) {
        val intent = Intent(this, SedeConfigActivity::class.java)
        intent.putExtra("startTimer", startTimer)
        if (startTimer) {
            intent.putExtra("setTimer", minutes)
        }
        startActivity(intent)
        if (isFinish) {
            finish()
        }
    }

    protected open fun openQRScannerActivity() {
        val intent = Intent(this, QrScannerActivity::class.java)
        startActivity(intent)
    }

    /**
     * Create an instance to Network manager class and create a listener to launch the correspondent
     * activity, if the connection is lost it will launch the app status activity
     * Otherwise it will launch the recognition camera when the connection is available
     */
    protected open fun configureNetworkManager() {
        networkManager = NetworkManager(this, object : NetworkListener {
            override fun onConnected() {
                openRecognitionCamera()
            }

            override fun onDisconnected() {
                openAppStatusActivity(Constants.APP_NETWORK_ERROR)
            }
        })
    }

}