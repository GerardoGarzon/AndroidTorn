/**
 * Created by Gerardo Garzon on 30/12/22.
 */
package com.lebentech.lebentechtorniquetes.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.util.Log
import com.lebentech.lebentechtorniquetes.interfaces.NetworkListener

class NetworkManager(context: Context, listener: NetworkListener) {
    /**
     * It will monitor the devices network and it will call the listener when an event is
     * launched, when the device is offline it will launch the app status activity with the network
     * error message
     * When the connection is back it will open the initial activities to open the activity depending
     * on the sede configuration
     */
    init {
        val networkCallback: NetworkCallback = object : NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.println(Log.INFO, "NetworkCallback", "On lost")
                listener.onDisconnected()
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.println(Log.INFO, "NetworkCallback", "Available")
                listener.onConnected()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.println(Log.INFO, "NetworkCallback", "Unavailable")
                listener.onDisconnected()
            }
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }
}