package com.jakebarnby.filemanager.util;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.SourceStorageStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 6/6/2017.
 */

public class Utils {
    /**
     * Checks whether the device currently has a network connection.
     * @return True if the device has a network connection, false otherwise.
     */
    public static boolean isConnectionReady(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected());
    }

    /**
     * Get the file extension from a path
     * @param path The path to the file, can be either local or remote
     * @return
     */
    public static String fileExt(String path) {
        if (path.indexOf("?") > -1) {
            path = path.substring(0, path.indexOf("?"));
        }
        if (path.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = path.substring(path.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    /**
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }
        return screenHeight;
    }

    /**
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }
        return screenWidth;
    }

    public static void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the free space available in the given file
     * @param dir   The dir to check free space of
     * @return      Amount of bytes free in file
     */
    public static SourceStorageStats getStorageStats(File dir) {
        StatFs fileSystem = new StatFs(dir.getAbsolutePath());

        SourceStorageStats info = new SourceStorageStats();
        info.setFreeSpace(fileSystem.getAvailableBytes());
        info.setTotalSpace(fileSystem.getTotalBytes());
        info.setUsedSpace(fileSystem.getTotalBytes() - fileSystem.getAvailableBytes());
        return info;
    }

    /**
     * Returns external storage paths (directory of external memory card) as array of Strings
     * @param context   Context for resources
     * @return          Array of external storage paths
     */
    public static String[] getExternalStorageDirectories(Context context) {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = context.getExternalFilesDirs(null);

            for (File file : externalDirs) {
                if (file == null) continue;
                String path = file.getPath().split("/Android")[0];

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) {
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    public static int resolveLogoId(String sourceName) {
        switch (sourceName) {
            case Constants.Sources.DROPBOX:
                return R.drawable.ic_dropbox;
            case Constants.Sources.GOOGLE_DRIVE:
                return R.drawable.ic_googledrive;
            case Constants.Sources.ONEDRIVE:
                return R.drawable.ic_onedrive;
            default:
                return R.mipmap.ic_launcher;
        }
    }

    public static void logFirebaseSourceErrorEvent(FirebaseAnalytics analytics,
                                                   String eventName,
                                                   String errorMessage,
                                                   String sourceName) {
        Bundle params = new Bundle();
        params.putString(Constants.Analytics.PARAM_MESSAGE, errorMessage);
        params.putString(Constants.Analytics.PARAM_SOURCE_NAME, sourceName);
        analytics.logEvent(eventName, params);
    }

    public static void logFirebaseErrorEvent(FirebaseAnalytics analytics,
                                             String eventName,
                                             String errorMessage) {
        Bundle params = new Bundle();
        params.putString(Constants.Analytics.PARAM_MESSAGE, errorMessage);
        analytics.logEvent(eventName, params);
    }

    public static void logFirebaseEvent(FirebaseAnalytics analytics, String eventName) {
        analytics.logEvent(eventName, new Bundle());
    }
}
