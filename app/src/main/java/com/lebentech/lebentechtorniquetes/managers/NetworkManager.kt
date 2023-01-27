/**
 * Created by Gerardo Garzon on 30/12/22.
 */
package com.lebentech.lebentechtorniquetes.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.lebentech.lebentechtorniquetes.interfaces.NetworkListener

class NetworkManager(context: Context, listener: NetworkListener) {
    private var networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

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