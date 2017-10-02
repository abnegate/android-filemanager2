package com.jakebarnby.filemanager.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by Jake on 10/2/2017.
 */

public class PreferenceUtils {

    public static void savePref(Context context, String name, String value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(name, value).apply();
    }

    public static void savePref(Context context, String name, int value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(name, value).apply();
    }

    public static void savePref(Context context, String name, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(name, value).apply();
    }

    public static void savePref(Context context, String name, float value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putFloat(name, value).apply();
    }

    public static void savePref(Context context, String name, long value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putLong(name, value).apply();
    }

    public static void savePref(Context context, String name, Set<String> value) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(name, value).apply();
    }

    public static String getString(Context context, String name, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        return prefs.getString(name, defaultValue);
    }

    public static int getInt(Context context, String name, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(name, defaultValue);
    }

    public static boolean getBoolean(Context context, String name, boolean defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(name, defaultValue);
    }

    public static float getFloat(Context context, String name, float defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        return prefs.getFloat(name, defaultValue);
    }
    public static long getLong(Context context, String name, long defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        return prefs.getLong(name, defaultValue);
    }
    public static Set<String> getStringSet(Context context, String name, Set<String> defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.PREFS, Context.MODE_PRIVATE);
        return prefs.getStringSet(name, defaultValue);
    }
}
