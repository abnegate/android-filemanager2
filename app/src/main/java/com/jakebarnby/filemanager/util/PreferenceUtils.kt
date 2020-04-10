package com.jakebarnby.filemanager.util

import android.content.Context
import com.jakebarnby.filemanager.util.Constants.Prefs

/**
 * Created by Jake on 10/2/2017.
 */
object PreferenceUtils {
    fun savePref(context: Context, name: String?, value: String?) {
        val prefs = context!!.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(name, value).apply()
    }

    fun savePref(context: Context, name: String?, value: Int) {
        val prefs = context!!.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(name, value).apply()
    }

    fun savePref(context: Context, name: String?, value: Boolean) {
        val prefs = context!!.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(name, value).apply()
    }

    fun savePref(context: Context, name: String?, value: Float) {
        val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        prefs.edit().putFloat(name, value).apply()
    }

    fun savePref(context: Context, name: String?, value: Long) {
        val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong(name, value).apply()
    }

    fun savePref(context: Context, name: String?, value: Set<String?>?) {
        val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(name, value).apply()
    }

    fun getString(context: Context, name: String?, defaultValue: String?): String? {
        val prefs = context!!.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        return prefs.getString(name, defaultValue)
    }

    fun getInt(context: Context, name: String?, defaultValue: Int): Int {
        val prefs = context!!.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(name, defaultValue)
    }

    fun getBoolean(context: Context, name: String?, defaultValue: Boolean): Boolean {
        val prefs = context!!.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(name, defaultValue)
    }

    fun getFloat(context: Context, name: String?, defaultValue: Float): Float {
        val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        return prefs.getFloat(name, defaultValue)
    }

    fun getLong(context: Context, name: String?, defaultValue: Long): Long {
        val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        return prefs.getLong(name, defaultValue)
    }

    fun getStringSet(context: Context, name: String?, defaultValue: Set<String?>?): Set<String>? {
        val prefs = context.getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(name, defaultValue)
    }
}