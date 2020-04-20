package com.jakebarnby.filemanager.ui.sources

import com.jakebarnby.batteries.mvp.presenter.BatteriesListPresenter
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.managers.SourceManager
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.ViewType
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.stripExtension
import com.jakebarnby.filemanager.util.toDisplayDate
import javax.inject.Inject

class SourceFragmentListPresenter @Inject constructor(
    private val sourceManager: SourceManager,
    private val selectedFilesManager: SelectedFilesManager,
    private val prefsManager: PreferenceManager
) : BatteriesListPresenter<SourceFile, SourceFragmentContract.ListView>(), SourceFragmentContract.ListPresenter {

    override fun getItemViewTypeAtIndex(position: Int): Int {
        val viewType = ViewType.getFromValue(prefsManager.getInt(
            Constants.Prefs.VIEW_TYPE_KEY,
            ViewType.LIST.value
        ))
        return when (viewType) {
            ViewType.LIST -> R.layout.view_file_list
            ViewType.GRID -> R.layout.view_file_grid
            ViewType.DETAILED_LIST -> R.layout.view_file_detailed_list
            else -> R.layout.view_file_list
        }
    }

    override fun onItemLongSelected(position: Int) {
        TODO("not implemented")
    }

    override fun onBindItemView(view: SourceFragmentContract.ListView, position: Int) {
        val file = getItemAtIndex(position) ?: return

        view.setFileName(file.name.stripExtension())
        view.setSize(0)

        if (file.isDirectory) {
            view.setImage(R.drawable.ic_folder_flat)
        } else {
            view.setImage(file.thumbnailLink)
        }

        if (file.sourceId != SourceType.DROPBOX.id) {
            view.setModifiedDate(file.modifiedTime.toDisplayDate())
        }

        if (sourceManager.activeSource.isMultiSelectEnabled) {
            view.showSelection()
            view.animateSelectionIn()
        } else {
            view.hideSelection()
        }

        view.setSelected(selectedFilesManager.operationCount > 0
            && selectedFilesManager.currentSelectedFiles.contains(file))
    }
}