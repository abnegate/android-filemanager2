package com.jakebarnby.filemanager.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jakebarnby.batteries.core.model.Model
import java.io.Serializable

/**
 * Created by Jake on 6/5/2017.
 */
@Entity
open class SourceFile : Serializable, Model<Long>(0) {

    @PrimaryKey
    var fileId: Long = 0

    var remoteFileId: Long = -1
    var parentFileId: Long = -1
    var sourceId: Int = -1

    lateinit var path: String
    lateinit var name: String
    lateinit var thumbnailLink: String
    lateinit var fileType: String

    var isDirectory = false
    var isHidden = false

    var size: Long = 0
    var modifiedTime: Long = 0
}