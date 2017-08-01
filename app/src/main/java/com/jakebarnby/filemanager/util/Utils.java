package com.jakebarnby.filemanager.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.jakebarnby.filemanager.models.SourceStorageStats;
import com.jakebarnby.filemanager.models.files.SourceFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

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
    public static SourceStorageStats getLocalStorageStats(File dir) {
        StatFs fileSystem = new StatFs(dir.getAbsolutePath());

        SourceStorageStats info = new SourceStorageStats();
        info.setFreeSpace(fileSystem.getAvailableBytes());
        info.setTotalSpace(fileSystem.getTotalBytes());
        info.setUsedSpace(fileSystem.getTotalBytes() - fileSystem.getAvailableBytes());
        return info;
    }

    public static String generateUniqueFilename(String newName, TreeNode<SourceFile> actionableDir) {
        boolean stripExtension = newName.lastIndexOf('.') != -1;
        String name = stripExtension ?
                newName.substring(0, newName.lastIndexOf('.')):
                newName;

        String finalName = name;

        int copyCount = 0;
        for(int i = 0; i < actionableDir.getChildren().size(); i++) {
            SourceFile curFile = actionableDir.getChildren().get(i).getData();
            String curFileName = curFile.getName().lastIndexOf('.') != -1 ?
                    curFile.getName().substring(0, curFile.getName().lastIndexOf('.')) :
                    curFile.getName();
            if (curFileName.equalsIgnoreCase(finalName)) {
                copyCount++;

                boolean isOneDrive = actionableDir.getData().getSourceName().equals(Constants.Sources.ONEDRIVE);
                finalName = String.format(Locale.getDefault(),
                        isOneDrive ? "%s %d" : "%s (%d)",
                        name,
                        copyCount);
                i = -1;
            }
        }

        return stripExtension ?
                finalName + newName.substring(newName.lastIndexOf('.')) :
                finalName;
    }}
