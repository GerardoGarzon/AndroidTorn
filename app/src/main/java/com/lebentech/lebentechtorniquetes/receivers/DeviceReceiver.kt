/**
 * Created by Gerardo Garzon on 30/01/23.
 */
package com.lebentech.lebentechtorniquetes.receivers

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class DeviceReceiver: DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }

}