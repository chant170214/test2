package com.shadowchat.data.tor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class TorController(private val context: Context) {

    fun isTorReady(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    fun startHiddenService(): String {
        return "shadow.onion"
    }
}
