/**
 * Created by Gerardo Gonzalez on 20/12/22.
 */

package com.lebentech.lebentechtorniquetes.receivers

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.lebentech.lebentechtorniquetes.services.ForegroundServiceApp
import com.lebentech.lebentechtorniquetes.views.activities.LaunchScreenActivity

class RebootReceiver : BroadcastReceiver() {
    /**
     * This method receives two different actions from the system, when it is boot_completed
     * it will launch the app after all the system configuration are ready
     * When it is the alarm service it will start the service to re launch the application
     */
    override fun onReceive(context: Context, intent: Intent) {
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            // Reboot broadcast receiver
            val intentAutoStart = Intent(context, LaunchScreenActivity::class.java)
            intentAutoStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentAutoStart)
            Toast.makeText(context, "Servicio de autostart ejecutado", Toast.LENGTH_LONG).show()
        } else if ("CHECK_SERVICE_ALARM" == intent.action) {
            // Alarm manager broadcast receiver
            Toast.makeText(context, "Servicio de alarma ejecutado", Toast.LENGTH_LONG).show()
            val serviceIntent = Intent(context, ForegroundServiceApp::class.java)
            context.startService(serviceIntent)
        }
    }
}