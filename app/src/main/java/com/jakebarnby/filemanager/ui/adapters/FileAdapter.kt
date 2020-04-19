package com.jakebarnby.filemanager.ui.adapters

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.glide.GlideApp
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.adapters.FileAdapter.FileViewHolder
import com.jakebarnby.filemanager.util.Comparators
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.io.File
import java.util.*

/**
 * Created by Jake on 5/31/2017.
 */
abstract class FileAdapter(
    private val source: Source,
    private val selectedFilesManager: SelectedFilesManager,
    private val prefs: PreferenceManager
) : RecyclerView.Adapter<FileViewHolder>() {

    private var onClickListener: OnFileClickedListener? = null
    private var onLongClickListener: OnFileLongClickedListener? = null
    private var showHiddenFiles: Boolean

    init {
        showHiddenFiles = prefs.getBoolean(Prefs.HIDDEN_FILES_KEY, false)
        getVisibileFiles(source.rootNode)
    }

    private fun getVisibileFiles(currentDir: TreeNode<SourceFile>): TreeNode<SourceFile> {
        showHiddenFiles = prefs.getBoolean(Prefs.HIDDEN_FILES_KEY, false)

        var currentDirChildren = currentDir.children
        if (!showHiddenFiles) {
            val readableChildren = mutableListOf<TreeNode<SourceFile>>()
            for (file in currentDirChildren) {
                if (!file.data.isHidden) {
                    readableChildren.add(file)
                }
            }
            currentDirChildren = readableChildren
            if (currentDirChildren.isEmpty()) {
                //TODO: Show empty source
                return currentDir
            }

            TreeNode.sortTree(
                currentDir,
                Comparators.resolveComparatorForPrefs(prefs)
            )
        }
        return currentDir
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bindHolder(getVisibileFiles(source.currentDirectory.children[position]))
    }

    override fun getItemCount(): Int {
        return source.currentDirectory.children.size
    }

    open inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val previewImage: ImageView = itemView.findViewById(R.id.img_file_preview)
        private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox)
        private val fileName: TextView = itemView.findViewById(R.id.txt_item_title)

        private val sizeOrCountText: TextView? = itemView.findViewById(R.id.txt_item_size)
        private val modifiedDateText: TextView? = itemView.findViewById(R.id.txt_item_modified_datetime)

        open fun bindHolder(currentDir: TreeNode<SourceFile>) {
            val name = currentDir.data.name
            if (currentDir.data.isDirectory) {
                fileName.text = name
                previewImage.setImageResource(R.drawable.ic_folder_flat)
            } else {
                if (name.lastIndexOf('.') > 0) {
                    fileName.text = name.substring(0, name.lastIndexOf('.'))
                } else {
                    fileName.text = name
                }
                setThumbnail(currentDir)
            }

            if (currentDir.data.isDirectory) {
                sizeOrCountText?.text = currentDir.children.size.toString() + " items"
            } else {
                sizeOrCountText?.text = String.format(
                    Locale.getDefault(),
                    "%.2f %s",
                    currentDir.data.size / Constants.BYTES_TO_MEGABYTE,
                    "MB"
                )
            }

            if (currentDir.data.sourceId != SourceType.DROPBOX.id) {
                val displayTime = Utils.getDisplayStringFromDate(currentDir.data.modifiedTime)
                modifiedDateText?.text = displayTime
            }

            if (!source.isMultiSelectEnabled) {
                selectionCheckbox.isChecked = false
                selectionCheckbox.visibility = View.GONE
            } else {
                selectionCheckbox.visibility = View.VISIBLE
            }

            if (source.isMultiSelectEnabled && selectionCheckbox.visibility == View.VISIBLE) {
                val translate = TranslateAnimation(-500f, 0.0f, 0.0f, 0.0f)
                translate.interpolator = DecelerateInterpolator(3.0f)
                translate.duration = 400
                selectionCheckbox.startAnimation(translate)
            }

            if (selectedFilesManager.operationCount > 0 &&
                selectedFilesManager.currentSelectedFiles.contains(currentDir)) {
                selectionCheckbox.isChecked = true
            }
        }

        private fun setThumbnail(currentDir: TreeNode<SourceFile>) {
            GlideApp
                .with(itemView)
                .load(
                    if (currentDir.data.sourceId == SourceType.LOCAL.id) {
                        File(currentDir.data.thumbnailLink)
                    } else {
                        currentDir.data.thumbnailLink
                    })
                .error(R.drawable.ic_file)
                .placeholder(R.drawable.ic_file)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .override(100, 100)
                .thumbnail(0.2f)
                .circleCrop()
                .into(previewImage)
        }

        private fun createOnClickListener(): View.OnClickListener {
            return View.OnClickListener {
                if (source.isMultiSelectEnabled) {
                    selectionCheckbox.isChecked = !selectionCheckbox.isChecked
                }

                onClickListener?.onClick(
                    source.currentDirectory.children[adapterPosition],
                    selectionCheckbox.isChecked,
                    adapterPosition
                )
            }
        }

        /**
         * The long click listener for this file or folder
         *
         * @return
         */
        private fun createOnLongClickListener(): View.OnLongClickListener {
            return View.OnLongClickListener { v: View? ->
                if (!source.isMultiSelectEnabled) {
                    source.isMultiSelectEnabled = true
                }
                onLongClickListener?.onLongClick(
                    source.currentDirectory.children[adapterPosition]
                )
                true
            }
        }

        init {
            itemView.isLongClickable = true
            itemView.setOnClickListener(createOnClickListener())
            itemView.setOnLongClickListener(createOnLongClickListener())

            selectionCheckbox.setOnClickListener { v: View? ->
                onClickListener?.onClick(
                    source.currentDirectory.children[adapterPosition],
                    selectionCheckbox.isChecked,
                    adapterPosition
                )
            }
        }
    }

    @FunctionalInterface
    interface OnFileClickedListener {
        fun onClick(file: TreeNode<SourceFile>, isChecked: Boolean, position: Int)
    }

    @FunctionalInterface
    interface OnFileLongClickedListener {
        fun onLongClick(file: TreeNode<SourceFile>)
    }
}