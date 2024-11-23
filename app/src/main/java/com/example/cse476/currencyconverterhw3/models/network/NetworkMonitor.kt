package com.example.cse476.currencyconverterhw3.models.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkMonitor(context: Context) {
    private val _connectivityManager =
        context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager

    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean> = this._networkState

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            this@NetworkMonitor._networkState.postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            this@NetworkMonitor._networkState.postValue(false)
        }
    }

    fun startMonitoringNetwork() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        this._connectivityManager.registerNetworkCallback(request, this._networkCallback)
    }

    fun stopMonitoringNetwork() {
        this._connectivityManager.unregisterNetworkCallback(this._networkCallback)
    }
}