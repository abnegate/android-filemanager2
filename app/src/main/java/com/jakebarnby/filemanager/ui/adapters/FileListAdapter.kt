package com.jakebarnby.filemanager.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode

/**
 * Created by Jake on 5/31/2017.
 */
class FileListAdapter(
    rootNode: TreeNode<SourceFile>,
    context: Context
) : FileAdapter(rootNode, context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.view_file_list, parent, false)
        return FileViewHolder(inflatedView)
    }
}