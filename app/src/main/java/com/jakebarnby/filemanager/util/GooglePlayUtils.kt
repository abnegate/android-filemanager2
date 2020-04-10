package com.jakebarnby.filemanager.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.jakebarnby.filemanager.util.Constants.RequestCodes

/**
 * Created by jakebarnby on 3/10/17.
 */
object GooglePlayUtils {
    /**
     * Check that Google Play services APK is installed and up to date.
     * @return True if Google Play Services is available and up to date on this device, false otherwise.
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    fun acquireGooglePlayServices(activity: Activity?) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(activity: Activity?, connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                RequestCodes.GOOGLE_PLAY_SERVICES)
        dialog.show()
    }
}