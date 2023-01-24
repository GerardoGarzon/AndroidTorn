/**
 * Created by Gerardo Garzon on 22/12/22.
 */

package com.lebentech.lebentechtorniquetes.services

import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager.RunningTaskInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lebentech.lebentechtorniquetes.R
import com.lebentech.lebentechtorniquetes.views.activities.LaunchScreenActivity
import java.util.*

class ForegroundServiceApp : Service() {
    private val timer = Timer()
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val manager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        NotificationManagerCompat.from(this).cancelAll()
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT
        )
        val alarmService = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] =
            restartServicePendingIntent
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("OnDestroy", "Finalizado")
    }

    fun stopService() {
        timer.cancel()
        unregisterReceiver(broadcastReceiver)
        stopService(Intent(this, ForegroundServiceApp::class.java))
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification() {
        val context = applicationContext
        val res = context.resources
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val title = res.getString(R.string.lbl_notification_title)
            val body = res.getString(R.string.lbl_notification_body)
            val name: CharSequence = "My notification"
            val desc = "My notification desc"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL, name, importance)
            notificationChannel.description = desc
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            val intent = Intent() //same
            intent.action = "Detener servicio"
            intent.putExtra("RES", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            val pendingIntent1 = PendingIntent.getBroadcast(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_liveness)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent1)
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_exit, "Detener servicio", pendingIntent1)
            val notificationManagerCompat = NotificationManagerCompat.from(this)
            notificationManagerCompat.notify(1, builder.build())
            val intentFilter = IntentFilter()
            intentFilter.addAction("Detener servicio")
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private val isAppRunning: Unit
        get() {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        if (!checkAppRunning()) {
                            Thread.sleep(5000)
                            launchApp()
                            Thread.sleep(5000)
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }, 0, 1000)
        }

    private fun launchApp() {
        val launchIntent = Intent(this, LaunchScreenActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun checkAppRunning(): Boolean {
        var info: RunningTaskInfo
        val activityManager: ActivityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val l = activityManager.getRunningTasks(1000)
        println(l)
        val i: Iterator<RunningTaskInfo> = l.iterator()
        var packName: String
        ApplicationInfo()
        while (i.hasNext()) {
            info = i.next()
            packName = info.baseActivity!!.packageName
            if (packName == packageName) {
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