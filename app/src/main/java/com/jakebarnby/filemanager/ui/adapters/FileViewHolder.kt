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
import com.jakebarnby.batteries.core.extensions.setGone
import com.jakebarnby.batteries.core.extensions.setVisible
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.core.GlideApp
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import java.io.File
import java.lang.String.format
import java.util.*

class FileViewHolder(
    itemView: View,
    onItemSelected: (Int) -> Unit,
    onItemLongSelected: (Int) -> Unit
) : RecyclerView.ViewHolder(itemView), SourceFragmentContract.ListView {

    private val previewImage: ImageView = itemView.findViewById(R.id.img_file_preview)
    private val selectionCheckbox: CheckBox = itemView.findViewById(R.id.checkbox)
    private val fileName: TextView = itemView.findViewById(R.id.txt_item_title)

    private val sizeOrCountText: TextView? = itemView.findViewById(R.id.txt_item_size)
    private val modifiedDateText: TextView? = itemView.findViewById(R.id.txt_item_modified_datetime)

    init {
        itemView.setOnClickListener {
            onItemSelected(adapterPosition)
        }
        itemView.setOnLongClickListener {
            onItemLongSelected(adapterPosition)
            true
        }
    }

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

    override fun showSelection() {
        selectionCheckbox.setVisible()
    }

    override fun hideSelection() {
        selectionCheckbox.setGone()
    }

    override fun animateSelectionIn() {
        val translate = TranslateAnimation(-500f, 0.0f, 0.0f, 0.0f)
        translate.interpolator = DecelerateInterpolator(3.0f)
        translate.duration = 400
        selectionCheckbox.startAnimation(translate)
    }
}