package com.jakebarnby.filemanager.sources.models

import java.io.Serializable

/**
 * Created by Jake on 6/5/2017.
 */
abstract class SourceFile : Serializable {

    lateinit var path: String
    lateinit var name: String
    lateinit var sourceName: String
    lateinit var sourceType: SourceType
    lateinit var thumbnailLink: String

    var isDirectory = false
    var isHidden = false

    var size: Long = 0
    var modifiedTime: Long = 0

    var positionToRestore = -1

    fun addSize(size: Long) {
        this.size += size
    }

    fun removeSize(size: Long) {
        this.size -= size
    }
}