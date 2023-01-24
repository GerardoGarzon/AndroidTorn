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
                listener.onDisconnected()
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                listener.onConnected()
            }
        }

        val connectivityManager = context.getSystemService(
            ConnectivityManager::class.java
        ) as ConnectivityManager

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
}