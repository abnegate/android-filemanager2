package com.jakebarnby.filemanager.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.core.os.EnvironmentCompat
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.StorageInfo
import com.jakebarnby.filemanager.util.Constants.Sources
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * Created by Jake on 6/6/2017.
 */
object Utils {

    /**
     * Checks whether the device currently has a network connection.
     * @return True if the device has a network connection, false otherwise.
     */
    fun isConnectionReady(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }

    /**
     * Get the file extension from a path
     * @param path The path to the file, can be either local or remote
     * @return
     */
    fun fileExt(path: String?): String? {
        var path = path
        if (path!!.contains("?")) {
            path = path.substring(0, path.indexOf("?"))
        }
        return if (path.lastIndexOf(".") == -1) {
            null
        } else {
            var ext = path.substring(path.lastIndexOf(".") + 1)
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"))
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"))
            }
            ext.toLowerCase()
        }
    }

    private var screenWidth = 0
    private var screenHeight = 0

    /**
     *
     * @param context
     * @return
     */
    fun getScreenHeight(context: Context): Int {
        if (screenHeight == 0) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display
            if (wm != null) {
                display = wm.defaultDisplay
                val size = Point()
                display.getSize(size)
                screenHeight = size.y
            }
        }
        return screenHeight
    }

    /**
     *
     * @param context
     * @return
     */
    fun getScreenWidth(context: Context): Int {
        if (screenWidth == 0) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        }
        return screenWidth
    }

    fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        try {
            val out: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get the free space available in the given file
     * @param dir   The dir to check free space of
     * @return      Amount of bytes free in file
     */
    fun getStorageStats(dir: File): StorageInfo {
        val fileSystem = StatFs(dir.absolutePath)
        val info = StorageInfo()
        info.freeSpace = fileSystem.availableBytes
        info.totalSpace = fileSystem.totalBytes
        info.usedSpace = fileSystem.totalBytes - fileSystem.availableBytes
        return info
    }

    /**
     * Returns external storage paths (directory of external memory card) as array of Strings
     * @param context   Context for resources
     * @return          Array of external storage paths
     */
    fun getExternalStorageDirectories(context: Context): List<String> {
        val results = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val externalDirs = context.getExternalFilesDirs(null)
            for (file in externalDirs) {
                if (file == null) continue
                val path = file.path.split("/Android").toTypedArray()[0]
                var addPath = false
                addPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Environment.isExternalStorageRemovable(file)
                } else {
                    Environment.MEDIA_MOUNTED == EnvironmentCompat.getStorageState(file)
                }
                if (addPath) {
                    results.add(path)
                }
            }
        }
        if (results.isEmpty()) {
            var output = ""
            try {
                val process = ProcessBuilder().command("mount | grep /dev/block/vold")
                    .redirectErrorStream(true).start()
                process.waitFor()
                val inputStream = process.inputStream
                val buffer = ByteArray(1024)
                while (inputStream.read(buffer) != -1) {
                    output += String(buffer)
                }
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (output.trim { it <= ' ' }.isNotEmpty()) {
                val devicePoints = output.split("\n").toTypedArray()
                for (voldPoint in devicePoints) {
                    results.add(voldPoint.split(" ").toTypedArray()[2])
                }
            }
        }

        return results
    }

    fun getDisplayStringFromDate(date: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }

        return String.format(
            Locale.getDefault(),
            Constants.DATE_TIME_FORMAT,
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            calendar[Calendar.DAY_OF_MONTH],
            calendar[Calendar.MONTH] + 1,
            calendar[Calendar.YEAR] - 2000
        )
    }

    fun resolveLogoId(sourceId: Int): Int {
        return when (sourceId) {
            SourceType.DROPBOX.id -> R.drawable.ic_dropbox
            SourceType.GOOGLE_DRIVE.id -> R.drawable.ic_googledrive
            SourceType.ONEDRIVE.id -> R.drawable.ic_onedrive
            else -> R.drawable.ic_file
        }
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    fun pxToDp(px: Int): Int {
        val metrics = Resources.getSystem().displayMetrics
        return px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}