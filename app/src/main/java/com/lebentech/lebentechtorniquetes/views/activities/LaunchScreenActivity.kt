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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.lebentech.lebentechtorniquetes.databinding.ActivityLaunchScreenBinding
import com.lebentech.lebentechtorniquetes.interfaces.ServerErrorListener
import com.lebentech.lebentechtorniquetes.managers.WriterManager
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
import java.io.File
import java.nio.charset.Charset
import java.util.*

@SuppressLint("CustomSplashScreen")
class LaunchScreenActivity : BaseActivity() {

    private val REQUEST_NOTIFICATION_PERMISSION_CODE = 123
    private val REQUEST_LOCATION_PERMISSIONS_CODE = 99
    private val REQUEST_CAMERA_PERMISSION_CODE = 200

    private var isRequestingPermissions = true
    private val lifeTestMinutesPeriod = 1
    private var isRequestingAdminPermissions = false

    private val screen: Screen = Screen(true, Constants.LAUNCH_ACTIVITY)
    private lateinit var binding: ActivityLaunchScreenBinding
    private lateinit var lifeTestViewModel: LifeTestViewModel

    /**
     * Execute the service to delete the old logs in the device
     */
    private val runnableFileManager: Runnable = Runnable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                FileManagerService.deleteOldRecords()
            }
        } else {
            FileManagerService.deleteOldRecords()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun prepareComponents() {

        // Create startup log
        WriterManager().createTextLog("Startup application", arrayOf(
            "Application started up at: ${Date()}"
        ), "startup")

        /* val arr = Utils.convertImageToByteArray("/storage/emulated/0/Documents/FR_IMAGE.jpeg").contentToString()
        val file = File("/storage/emulated/0/Documents/FR_IMAGE.txt")
        file.writeText(arr) */

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

    /**
     * When the activity is in foreground and it is not asking for permissions it will close the
     * activity
     */
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
            REQUEST_LOCATION_PERMISSIONS_CODE
        )
    }

    /**
     * Request the camera permissions if the device has not accepted yet, for android 11
     * and newer it only request the permissions twice
     */
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf( Manifest.permission.CAMERA ),
            REQUEST_CAMERA_PERMISSION_CODE
        )
    }

    /**
     * Launch the alert for the notifications permissions to the user
     */
    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION_CODE
            )
        } else {
            requestCameraPermission()
        }
    }

    /**
     * Request permissions to display the application over the other apps when it is needed
     */
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayActivityResult.launch(intent)
    }

    /**
     * It starts the storage activity view from the system
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestAllStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        storageActivityResult.launch(intent)
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
            adminActivityResult.launch(intent)
        }
    }

    /**
     * Start the services for files manager and autostart application, then it runs the recognition
     * camera
     * First it verify if all the permissions are given
     */
    private fun startServices() {
        isRequestingPermissions = false
        Thread(runnableMonitor).start()
        Thread(runnableFileManager).start()
        Thread(runnableNetworking).start()
        sendLifeTest()
        SettingsViewModel.shared.loadPreferences()
    }

    /**
     * Start to send life test request each 3 minutes or the specific value in lifeTestMinutesPeriod
     */
    private fun sendLifeTest() {
        Handler(Looper.getMainLooper()).postDelayed(
            lifeTestRunnable ,
            (60000 * lifeTestMinutesPeriod).toLong()
        )
    }

    /**
     * Manage the result for the admin device activity result
     */
    private val adminActivityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
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
    }

    /**
     * Manage the result for the overlay activity result
     */
    private val overlayActivityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        adminDeviceApp()
    }

    /**
     * Manage the result for the storage activity result
     */
    private val storageActivityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        startServices()
    }
}
