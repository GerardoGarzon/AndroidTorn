/**
 * Created by Gerardo Garzon on 22/12/22.
 */

package com.lebentech.lebentechtorniquetes.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lebentech.lebentechtorniquetes.R
import com.lebentech.lebentechtorniquetes.managers.WriterManager
import com.lebentech.lebentechtorniquetes.views.activities.LaunchScreenActivity
import java.util.*


class ForegroundServiceApp : Service() {
    private val timer = Timer()

    /**
     * Broadcast receiver to stop the foreground sevice when the notification is pressed
     */
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val manager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.cancelAll()
            val state = intent.action
            if (state == "Detener servicio") {
                stopService()
                Toast.makeText(context, "Se detuvo el servicio autostart", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * When the service is created it will creater the notification channel and the notification to
     * stop the service
     */
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
        createNotification()
        isAppRunning
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * When the application is closed it will call onTaskRemoved method and it will restart the
     * service, then it will open the application again
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        NotificationManagerCompat.from(applicationContext).cancelAll()
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] =
            restartServicePendingIntent
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("OnDestroy", "Finalizado")
    }

    /**
     * When the service is stop by the notification it will unregister the broadcast receiver and
     * stop the service
     */
    fun stopService() {
        timer.cancel()
        unregisterReceiver(broadcastReceiver)
        stopService(Intent(applicationContext, ForegroundServiceApp::class.java))
    }

    /**
     * It will create the notification to stop the service
     */
    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val title = applicationContext.resources.getString(R.string.lbl_notification_title)
            val body = applicationContext.resources.getString(R.string.lbl_notification_body)
            val name: CharSequence = "My notification"
            val desc = "My notification desc"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL, name, importance)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationChannel.description = desc
            notificationManager.createNotificationChannel(notificationChannel)

            val intent = Intent() //same
            intent.action = "Detener servicio"
            intent.putExtra("RES", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            val pendingIntent1 = PendingIntent.getBroadcast(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_liveness)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent1)
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_exit, "Detener servicio", pendingIntent1)
            val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notificationManagerCompat.notify(1, builder.build())
            val intentFilter = IntentFilter()
            intentFilter.addAction("Detener servicio")
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    /**
     * If the application is not running in foreground or it is closed it will automatically launch
     * if after 5 seconds
     */
    private val isAppRunning: Unit
        get() {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        if (!checkAppRunning()) {
                            Thread.sleep(5000)
                            if (!checkAppRunning()) {
                                WriterManager().createTextLog("Application closed", arrayOf(
                                    "Application is closed at time: ${Date()}"
                                ), "closed")
                                launchApp()
                            }
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }, 0, 1000)
        }

    /**
     * It will launch the application as a new task
     */
    private fun launchApp() {
        val launchIntent = Intent(applicationContext, LaunchScreenActivity::class.java)
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(launchIntent)
    }

    /**
     * List all process running and verify if the application package name is there, if it is, it
     * will verify if it is in foreground or in background
     * It will return true if the app is open and in foreground
     * Return false if the application is closed or in background
     */
    fun checkAppRunning(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcessInfo = am.runningAppProcesses

        for (i in runningAppProcessInfo.indices) {
            if (runningAppProcessInfo[i].processName == packageName) {
                return isAppForeground
            }
        }
        return false
    }

    private val isAppForeground: Boolean
        get() {
            val appProcessInfo = RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(appProcessInfo)
            return appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE
        }

    companion object {
        private const val NOTIFICATION_CHANNEL = "CloseNotification"
    }
}