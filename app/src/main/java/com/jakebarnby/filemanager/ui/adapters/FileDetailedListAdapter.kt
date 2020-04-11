package com.jakebarnby.filemanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.util.*

/**
 * Created by Jake on 9/30/2017.
 */
class FileDetailedListAdapter(
    source: Source,
    prefs: PreferenceManager
) : FileAdapter(source, prefs) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_file_detailed_list, parent, false)

        return FileDetailedViewHolder(view)
    }

    private inner class FileDetailedViewHolder internal constructor(itemView: View) : FileViewHolder(itemView) {

        private val sizeOrCountText: TextView = itemView.findViewById(R.id.txt_item_size)
        private val modifiedDateText: TextView = itemView.findViewById(R.id.txt_item_modified_datetime)

        override fun bindHolder(currentDir: TreeNode<SourceFile>) {
            super.bindHolder(currentDir)

            if (currentDir.data.isDirectory) {
                sizeOrCountText.text = currentDir.children.size.toString() + " items"
            } else {
                sizeOrCountText.text = String.format(
                    Locale.getDefault(),
                    "%.2f %s",
                    currentDir.data.size / Constants.BYTES_TO_MEGABYTE,
                    " MB")
            }

            if (currentDir.data.sourceName != Sources.DROPBOX) {
                val displayTime = Utils.getDisplayStringFromDate(currentDir.data.modifiedTime)
                modifiedDateText.text = displayTime
            }
        }
    }
}