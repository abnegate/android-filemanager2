package com.jakebarnby.filemanager.util;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

/**
 * Created by Jake on 11/3/2017.
 */

public class LogUtils {

    /**
     * Log an event to firebase with the given event name
     *
     * @param analytics Firebase Analytics instance
     * @param eventName The name of the event
     */
    public static void logFirebaseEvent(FirebaseAnalytics analytics, String eventName) {
        analytics.logEvent(eventName, null);
    }

    /**
     * Log an event to firebase with the given event name and parameters
     *
     * @param analytics  Firebase Analytics instance
     * @param eventName  The name of the event
     * @param parameters Map of event parameters
     */
    public static void logFirebaseEvent(FirebaseAnalytics analytics,
                                         String eventName,
                                         Bundle parameters) {
        analytics.logEvent(eventName, parameters);
    }
}
