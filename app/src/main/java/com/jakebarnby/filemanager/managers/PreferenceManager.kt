package com.jakebarnby.filemanager.managers

import android.content.SharedPreferences
import javax.inject.Inject

class PreferenceManager @Inject constructor(
    private val preferences: SharedPreferences
) {

    fun savePref(name: String?, value: String?) {
        preferences.edit().putString(name, value).apply()
    }

    fun savePref(name: String?, value: Int) {
        preferences.edit().putInt(name, value).apply()
    }

    fun savePref(name: String?, value: Boolean) {
        preferences.edit().putBoolean(name, value).apply()
    }

    fun savePref(name: String?, value: Float) {
        preferences.edit().putFloat(name, value).apply()
    }

    fun savePref(name: String?, value: Long) {
        preferences.edit().putLong(name, value).apply()
    }

    fun savePref(name: String?, value: Set<String?>?) {
        preferences.edit().putStringSet(name, value).apply()
    }

    fun getString(name: String?, defaultValue: String?): String? {
        return preferences.getString(name, defaultValue)
    }

    fun getInt(name: String?, defaultValue: Int): Int {
        return preferences.getInt(name, defaultValue)
    }

    fun getBoolean(name: String?, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(name, defaultValue)
    }

    fun getFloat(name: String?, defaultValue: Float): Float {
        return preferences.getFloat(name, defaultValue)
    }

    fun getLong(name: String?, defaultValue: Long): Long {
        return preferences.getLong(name, defaultValue)
    }

    fun getStringSet(name: String?, defaultValue: Set<String?>?): Set<String>? {
        return preferences.getStringSet(name, defaultValue)
    }

    fun hasSourceToken(sourceId: Int): Boolean =
        preferences.getString(
            "$sourceId-access-token",
            null
        ) != null
}