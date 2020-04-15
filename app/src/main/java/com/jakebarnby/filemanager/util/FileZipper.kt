package com.jakebarnby.filemanager.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by Jake on 10/14/2017.
 */
class FileZipper {

    @Throws(IOException::class)
    fun zipFiles(
        destZipFilePath: String,
        filePathsToZip: Collection<String>
    ) {
        ZipOutputStream(FileOutputStream(destZipFilePath)).use {
            for (filePath in filePathsToZip) {
                addFileToZip("", filePath, it)
            }
        }
    }

    @Throws(IOException::class)
    private fun addFileToZip(
        folderPath: String,
        filePath: String,
        zip: ZipOutputStream
    ) {
        val file = File(filePath)
        if (file.isDirectory) {
            addFolderToZip(folderPath, filePath, zip)
            return
        }
        val buf = ByteArray(2048)
        var len: Int
        val inStream = FileInputStream(filePath)
        zip.putNextEntry(ZipEntry(folderPath + "/" + file.name))
        while (inStream.read(buf).also { len = it } > 0) {
            zip.write(buf, 0, len)
        }
        inStream.close()

    }

    @Throws(IOException::class)
    private fun addFolderToZip(
        folderPath: String,
        filePath: String,
        zip: ZipOutputStream
    ) {
        val folder = File(filePath)
        if (folder.list()?.isEmpty() == true) {
            zip.putNextEntry(ZipEntry(folderPath + folder.name + "/"))
            return
        }
        for (file in folder.listFiles() ?: emptyArray()) {
            addFileToZip(folderPath + folder.name + "/", file.path, zip)
        }
    }
}