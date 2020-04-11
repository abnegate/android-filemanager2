package com.jakebarnby.filemanager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.sources.models.Source

/**
 * Created by Jake on 5/31/2017.
 */
class FileListAdapter(
    source: Source,
    prefs: PreferenceManager
) : FileAdapter(source, prefs) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_file_list, parent, false)
        
        return FileViewHolder(inflatedView)
    }
}