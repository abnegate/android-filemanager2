package com.jakebarnby.filemanager.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by Jake on 11/3/2017.
 */
object Logger {
    /**
     * Log an event to firebase with the given event name
     *
     * @param analytics Firebase Analytics instance
     * @param eventName The name of the event
     */
    fun logFirebaseEvent(analytics: FirebaseAnalytics?, eventName: String?) {
        analytics!!.logEvent(eventName!!, null)
    }

    /**
     * Log an event to firebase with the given event name and parameters
     *
     * @param analytics  Firebase Analytics instance
     * @param eventName  The name of the event
     * @param parameters Map of event parameters
     */
    fun logFirebaseEvent(
        analytics: FirebaseAnalytics?,
        eventName: String?,
        parameters: Bundle?
    ) {
        analytics!!.logEvent(eventName!!, parameters)
    }
}