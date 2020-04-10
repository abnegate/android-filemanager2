package com.jakebarnby.filemanager.ui.adapters

import android.content.Context
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
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.ui.adapters.FileAdapter.FileViewHolder
import com.jakebarnby.filemanager.util.ComparatorUtils
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.PreferenceUtils
import com.jakebarnby.filemanager.util.TreeNode
import java.io.File

/**
 * Created by Jake on 5/31/2017.
 */
abstract class FileAdapter(
    rootNode: TreeNode<SourceFile>,
    context: Context
) : RecyclerView.Adapter<FileViewHolder>() {

    private var currentDirChildren: List<TreeNode<SourceFile>>? = null
    private var onClickListener: OnFileClickedListener? = null
    private var onLongClickListener: OnFileLongClickedListener? = null
    private var multiSelectEnabled = false
    private var showHiddenFiles: Boolean

    init {
        showHiddenFiles = PreferenceUtils.getBoolean(context, Prefs.HIDDEN_FOLDER_KEY, false)
        setCurrentDirectory(rootNode, context)
    }

    /**
     * Toggle multi-select mode
     * @param mMultiSelectEnabled Whether mdoe is enabled or disabled
     */
    fun setMultiSelectEnabled(mMultiSelectEnabled: Boolean) {
        this.multiSelectEnabled = mMultiSelectEnabled
    }

    val currentDirectory: TreeNode<SourceFile>?
        get() = currentDirChildren?.get(0)?.parent

    /**
     * Set the current directory based of the given current directory
     * @param currentDir Directory to set as current
     */
    fun setCurrentDirectory(
        currentDir: TreeNode<SourceFile>,
        context: Context
    ) {
        showHiddenFiles = PreferenceUtils.getBoolean(context, Prefs.HIDDEN_FOLDER_KEY, false)
        currentDirChildren = currentDir.children
        if (!showHiddenFiles) {
            val readableChildren = mutableListOf<TreeNode<SourceFile>>()
            for (file in currentDirChildren!!) {
                if (!file.data.isHidden) {
                    readableChildren.add(file)
                }
            }
            currentDirChildren = readableChildren
            if (currentDirChildren?.isEmpty() == true) {
                //TODO: Show empty source
                return
            }

            TreeNode.sortTree(
                    currentDir,
                    ComparatorUtils.resolveComparatorForPrefs(context)
            )
        }
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        showHiddenFiles = PreferenceUtils.getBoolean(holder.itemView.context, Prefs.HIDDEN_FOLDER_KEY, false)
        holder.bindHolder(currentDirChildren!![position])
    }

    override fun getItemCount(): Int {
        return currentDirChildren!!.size
    }

    open inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val previewImage: ImageView = itemView.findViewById(R.id.img_file_preview)
        private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox)
        private val fileName: TextView = itemView.findViewById(R.id.txt_item_title)

        /**
         *
         * @param currentDir
         */
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

            if (!multiSelectEnabled) {
                selectionCheckbox.isChecked = false
                selectionCheckbox.visibility = View.GONE
            } else {
                selectionCheckbox.visibility = View.VISIBLE
            }

            if (multiSelectEnabled && selectionCheckbox.visibility == View.VISIBLE) {
                val translate = TranslateAnimation(-500f, 0.0f, 0.0f, 0.0f)
                translate.interpolator = DecelerateInterpolator(3.0f)
                translate.duration = 400
                selectionCheckbox.startAnimation(translate)
            }

            if (SelectedFilesManager.operationCount > 0 &&
                SelectedFilesManager.currentSelectedFiles.contains(currentDir)) {
                selectionCheckbox.isChecked = true
            }
        }

        /**
         *
         * @param currentDir
         */
        private fun setThumbnail(currentDir: TreeNode<SourceFile>) {
            GlideApp
                    .with(itemView)
                    .load(
                        if (currentDir.data.sourceType == SourceType.LOCAL) {
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

        /**
         * Create the on click listener for this file or folder
         *
         * @return The click listener
         */
        private fun createOnClickListener(): View.OnClickListener {
            return View.OnClickListener {
                if (multiSelectEnabled) {
                    selectionCheckbox.isChecked = !selectionCheckbox.isChecked
                }

                onClickListener?.onClick(
                    currentDirChildren!![adapterPosition],
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
                if (!multiSelectEnabled) {
                    multiSelectEnabled = true
                }
                onLongClickListener!!.onLongClick(currentDirChildren!![adapterPosition])
                true
            }
        }

        init {
            itemView.isLongClickable = true
            itemView.setOnClickListener(createOnClickListener())
            itemView.setOnLongClickListener(createOnLongClickListener())
            selectionCheckbox.setOnClickListener { v: View? ->
                onClickListener!!.onClick(
                        currentDirChildren!![adapterPosition], selectionCheckbox.isChecked, adapterPosition)
            }
        }
    }

    /**
     * @param mOnClickListener
     */
    fun setOnClickListener(mOnClickListener: OnFileClickedListener?) {
        this.onClickListener = mOnClickListener
    }

    /**
     * @param mOnLongClickListener
     */
    fun setOnLongClickListener(mOnLongClickListener: OnFileLongClickedListener?) {
        this.onLongClickListener = mOnLongClickListener
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