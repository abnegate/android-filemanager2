package com.jakebarnby.filemanager.data

import javax.inject.Inject

class FileRepository @Inject constructor(
    val fileDao: FileDao
) {

}