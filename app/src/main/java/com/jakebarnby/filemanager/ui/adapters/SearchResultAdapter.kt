package com.jakebarnby.filemanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.ui.adapters.SearchResultAdapter.SearchViewHolder
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.util.*

/**
 * Created by Jake on 9/23/2017.
 */
class SearchResultAdapter(
    private var results: MutableList<TreeNode<SourceFile>>,
    searchResultClicked: OnSearchResultClicked
) : RecyclerView.Adapter<SearchViewHolder>() {

    private val backup: List<TreeNode<SourceFile>> = results
    private val onClickListener: OnSearchResultClicked = searchResultClicked

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.view_searchresult_list, parent, false)

        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bindHolder(results[position])
    }

    override fun getItemCount(): Int {
        return results.size
    }

    /**
     * Filter out all sources except the given string
     * @param name  Name of the source to keep
     */
    fun removeAllSourceExcept(id: Int) {
        results = ArrayList()
        for (i in backup.indices) {
            if (backup[i].data.sourceId == id) {
                results.add(backup[i])
            }
        }
    }

    fun resetDataset() {
        results = backup.toMutableList()
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val sourceLogo: ImageView = itemView.findViewById(R.id.img_source_logo)
        private val text: TextView = itemView.findViewById(R.id.txt_item_title)
        private val subText: TextView = itemView.findViewById(R.id.text_file_path)

        fun bindHolder(file: TreeNode<SourceFile>) {
            sourceLogo.setImageResource(Utils.resolveLogoId(file.data.sourceId))
            text.text = file.data.name
            subText.text = file.data.path
            itemView.setOnClickListener { onClickListener.navigateToFile(file) }
        }

    }

}