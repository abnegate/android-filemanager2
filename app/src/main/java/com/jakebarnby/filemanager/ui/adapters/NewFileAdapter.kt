package com.jakebarnby.filemanager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakebarnby.batteries.mvp.view.BatteriesMvpAdapter
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract

class NewFileAdapter(
    onItemSelected: (Int) -> Unit,
    private val onItemLongSelected: (Int) -> Unit,
    private val onGetItemViewType: (Int) -> Int,
    onBindItemView: (SourceFragmentContract.ListView, Int) -> Unit,
    itemCount: () -> Int
) : BatteriesMvpAdapter<SourceFile, SourceFragmentContract.ListView>(
    onItemSelected,
    onBindItemView,
    itemCount
) {

    override fun getItemViewType(position: Int): Int =
        onGetItemViewType(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context!!)
            .inflate(viewType, parent, false)

        return FileViewHolder(view)
    }
}