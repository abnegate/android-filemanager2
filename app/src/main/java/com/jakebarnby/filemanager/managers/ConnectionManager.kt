package com.jakebarnby.filemanager.managers

import android.net.ConnectivityManager

class ConnectionManager(
    val connectivityManager: ConnectivityManager
) {
    val isConnected: Boolean
        get() = true
}