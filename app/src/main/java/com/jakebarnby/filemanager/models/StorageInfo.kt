package com.jakebarnby.filemanager.models

import com.jakebarnby.filemanager.util.Constants

/**
 * Created by Jake on 7/31/2017.
 */
data class StorageInfo(
    val totalBytes: Long = 0,
    val usedBytes: Long = 0
) {
    val freeBytes: Long
        get() = totalBytes - usedBytes

    val totalGB: Double
        get() = totalBytes / Constants.BYTES_TO_GIGABYTE

    val usedGB: Double
        get() = usedBytes / Constants.BYTES_TO_GIGABYTE

    val freeGB: Double
        get() = freeBytes / Constants.BYTES_TO_GIGABYTE

    val usedPercent: Int
        get() = (100 * usedBytes / totalBytes).toInt()
}