package com.jakebarnby.filemanager.managers

import android.net.ConnectivityManager
import javax.inject.Inject

class ConnectionManager @Inject constructor(
    val connectivityManager: ConnectivityManager
) {
    val isConnected: Boolean
        get() = true
}