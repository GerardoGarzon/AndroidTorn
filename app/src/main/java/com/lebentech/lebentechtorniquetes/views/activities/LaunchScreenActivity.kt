package com.lebentech.lebentechtorniquetes.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.lebentech.lebentechtorniquetes.databinding.ActivityLaunchScreenBinding
import com.lebentech.lebentechtorniquetes.interfaces.ServerErrorListener
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.receivers.DeviceReceiver
import com.lebentech.lebentechtorniquetes.repositories.base.BaseRepository
import com.lebentech.lebentechtorniquetes.services.FileManagerService
import com.lebentech.lebentechtorniquetes.services.ForegroundServiceApp
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.LogUtils
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.LifeTestViewModel
import com.lebentech.lebentechtorniquetes.viewmodel.SettingsViewModel
import com.lebentech.lebentechtorniquetes.views.activities.base.BaseActivity

@SuppressLint("CustomSplashScreen")
class LaunchScreenActivity : BaseActivity() {

    private val REQUEST_NOTIFICATION_PERMISSION_CODE = 123
    private val REQUEST_ALL_STORAGE_PERMISSIONS_CODE = 1
    private val REQUEST_LOCATION_PERMISSIONS_CODE = 99
    private val REQUEST_OVERLAY_PERMISSIONS_CODE = 0
    private val REQUEST_CAMERA_PERMISSION_CODE = 200

    private var isRequestingPermissions = true
    private val lifeTestMinutesPeriod = 1

    private val screen: Screen = Screen(true, Constants.LAUNCH_ACTIVITY)
    private lateinit var binding: ActivityLaunchScreenBinding
    private lateinit var lifeTestViewModel: LifeTestViewModel

    private var isRequestingAdminPermissions = false

    // Services to run when the app starts

    /**
     * Execute the service to delete the old logs in the device
     */
    private val runnableFileManager: Runnable = Runnable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                FileManagerService.deleteOldRecords(this)
            }
        } else {
            FileManagerService.deleteOldRecords(this)
        }
    }

    /**
     * Execute the service to auto run the application when it is closed or in foreground
     */
    private val runnableMonitor: Runnable = Runnable {
        val intent = Intent(applicationContext, ForegroundServiceApp::class.java)
        startService(intent)
    }

    /**
     * Execute the life test service and recall it self to be sent each 3 minutes
     */
    private val lifeTestRunnable: Runnable = Runnable {
        lifeTestViewModel.sendLifeTest()
        sendLifeTest()
    }

    /**
     * Execute the service that monitor the network and launch the app status activity when the
     * device is offline
     */
    private val runnableNetworking: Runnable = Runnable {
        configureNetworkManager()
    }

    override fun getViewBinding(): ViewBinding {
        if(!this::binding.isInitialized) {
            binding = ActivityLaunchScreenBinding.inflate(layoutInflater)
        }
        return binding
    }

    override fun getScreen(): Screen {
        return screen
    }

    override fun prepareComponents() {
        SettingsViewModel.shared = ViewModelProvider(this)[SettingsViewModel::class.java]
        lifeTestViewModel = ViewModelProvider(this)[LifeTestViewModel::class.java]

        BaseRepository.setServerErrorListener(object: ServerErrorListener {
            override fun onServerError() {
                Utils.setPrivatePreferences(Constants.SEDE_PRIORITY_ID, 0, applicationContext)
                openAppStatusActivity(Constants.APP_SERVER_ERROR)
            }
        })

        requestLocationPermission()
    }

    override fun onStop() {
        super.onStop()
        if ( !isRequestingPermissions ) {
            finishAfterTransition()
        }
    }

    /**
     * If the user denies any permission then it will launch an alert to open the app
     * configuration and manually gives the permissions
     */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSIONS_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                createSettingsAlert(Constants.PERMISSIONS_LOCATION_ALERT_BODY, Constants.PERMISSIONS_ALERT_OPTION, Constants.PERMISSIONS_ALERT_TITLE)
                return
            }
            requestNotificationPermissions()
        } else if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                createSettingsAlert(Constants.PERMISSIONS_NOTIFICATION_ALERT_BODY, Constants.PERMISSIONS_ALERT_OPTION, Constants.PERMISSIONS_ALERT_TITLE)
                return
            } else {
                requestCameraPermission()
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                createSettingsAlert(Constants.PERMISSIONS_CAMERA_ALERT_BODY, Constants.PERMISSIONS_ALERT_OPTION, Constants.PERMISSIONS_ALERT_TITLE)
                return
            } else {
                if (!Settings.canDrawOverlays(this)) {
                    requestOverlayPermission()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
                        if (!Environment.isExternalStorageManager()) {
                            requestAllStoragePermission()
                        } else {
                            startServices()
                        }
                    } else {
                        startServices()
                    }
                }
            }
        }
    }

    /**
     * Manage the activities permissions views, when the activity is finished it will return the request code and it will perform
     * the next task
     */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( isRequestingAdminPermissions ) {
            isRequestingAdminPermissions = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
                if (!Environment.isExternalStorageManager()) {
                    requestAllStoragePermission()
                } else {
                    startServices()
                }
            } else {
                startServices()
            }
        } else if (requestCode == REQUEST_OVERLAY_PERMISSIONS_CODE) {
            adminDeviceApp()
        } else if (requestCode == REQUEST_ALL_STORAGE_PERMISSIONS_CODE) {
            startServices()
        }
    }

    /**
     * Create a custom alert that opens the app settings on the system configuration
     */
    private fun createSettingsAlert(bodyMessage: String, alertOption: String, alertTitle: String) {
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder.setMessage(bodyMessage)
            .setPositiveButton(alertOption) { _, _ ->
                openAppSystemSettings()
                finishAfterTransition()
            }
        val alert = dialogBuilder.create()
        alert.setTitle(alertTitle)
        alert.show()
    }

    /**
     * Open the system configuration for the application, it allows the user to accept the requested
     * permissions
     */
    private fun Context.openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }

    /**
     * Launch the alert for the location permissions to the user
     */
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSIONS_CODE)
    }

    /**
     * Request the camera permissions if the device has not accepted yet, for android 11
     * and newer it only request the permissions twice
     */
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.CAMERA ), REQUEST_CAMERA_PERMISSION_CODE)
    }

    /**
     * Launch the alert for the notifications permissions to the user
     */
    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION_CODE )
        } else {
            requestCameraPermission()
        }
    }

    /**
     * Request permissions to display the application over the other apps when it is needed
     * There are two intents in case that the device is manufactured by Xiaomi
     */
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSIONS_CODE)
    }

    /**
     * It starts the storage activity view from the system
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestAllStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_ALL_STORAGE_PERMISSIONS_CODE)
    }

    /**
     * Launch an intent to request permissions to give administrator permissions to the app, it will
     * allows to lock and unlock the device when it is needed
     */
    private fun adminDeviceApp() {
        isRequestingAdminPermissions = true

        val deviceManger = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DeviceReceiver::class.java)
        val isAdminActive: Boolean = deviceManger.isAdminActive(componentName)

        if (isAdminActive) {
            LogUtils.printLog("YES isAdminActive")
        } else {
            LogUtils.printLog("NO isAdminActive")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            startActivityForResult(intent, 1)
        }
    }

    /**
     * Start the services for files manager and autostart application, then it runs the recognition
     * camera
     * First it verify if all the permissions are given
     */
    private fun startServices() {
        isRequestingPermissions = false
        if (!isServiceRunning(ForegroundServiceApp::class.java)) {
            Thread(runnableMonitor).start()
        }
        // Thread(runnableFileManager).start()
        Thread(runnableNetworking).start()
        // sendLifeTest()
        SettingsViewModel.shared.loadPreferences()
    }

    /**
     * Before launching the service it has to verify that it is not already running
     */
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)

        if (services != null) {
            for (i in services.indices) {
                if (serviceClass.name.equals(services[i].service.className) && services[i].pid != 0) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Start to send life test request each 3 minutes or the specific value in lifeTestMinutesPeriod
     */
    private fun sendLifeTest() {
        Handler(Looper.getMainLooper()).postDelayed( lifeTestRunnable , (60000 * lifeTestMinutesPeriod).toLong())
    }
}