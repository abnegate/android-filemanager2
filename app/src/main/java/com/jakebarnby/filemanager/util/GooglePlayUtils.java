package com.jakebarnby.filemanager.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by jakebarnby on 3/10/17.
 */

public class GooglePlayUtils {

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return True if Google Play Services is available and up to date on this device, false otherwise.
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    public static void acquireGooglePlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(activity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public static void showGooglePlayServicesAvailabilityErrorDialog(Activity activity, final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                Constants.RequestCodes.GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}
