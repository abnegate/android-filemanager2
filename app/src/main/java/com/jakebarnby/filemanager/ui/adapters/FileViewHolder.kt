package com.jakebarnby.filemanager.ui.adapters

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.io.File
import java.lang.String.format
import java.util.*

class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), SourceFragmentContract.ListView {

    private val previewImage: ImageView = itemView.findViewById(R.id.img_file_preview)
    private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox)
    private val fileName: TextView = itemView.findViewById(R.id.txt_item_title)

    private val sizeOrCountText: TextView? = itemView.findViewById(R.id.txt_item_size)
    private val modifiedDateText: TextView? = itemView.findViewById(R.id.txt_item_modified_datetime)

    override fun setFileName(name: String) {
        fileName.text = name
    }

    override fun setImage(@DrawableRes imageId: Int) {
        previewImage.setImageResource(imageId)
    }

    override fun setImage(url: String) {
        GlideApp
            .with(itemView)
            .load(if (File(url).exists()) File(url) else url)
            .error(R.drawable.ic_file)
            .placeholder(R.drawable.ic_file)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(100, 100)
            .thumbnail(0.2f)
            .circleCrop()
            .into(previewImage)
    }

    override fun setSelected(selected: Boolean) {
        selectionCheckbox.isChecked = selected
    }

    override fun setSize(size: Int) {
        sizeOrCountText?.text = format(Locale.getDefault(), "%d items", size)
    }

    override fun setModifiedDate(date: String) {
        modifiedDateText?.text = date
    }

    fun bindHolder(currentDir: TreeNode<SourceFile>) {
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