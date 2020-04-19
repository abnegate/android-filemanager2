package com.jakebarnby.filemanager.models.sources

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.jakebarnby.filemanager.data.FileDao
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.managers.SourceManager
import com.jakebarnby.filemanager.models.*
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import javax.inject.Inject

class SourceFragmentPresenter @Inject constructor(
    override var sourceManager: SourceManager,
    override var selectedFilesManager: SelectedFilesManager,
    override var prefsManager: PreferenceManager,
    override var connectionManager: ConnectionManager,
    override var fileRepository: FileDao
) : SourceFragmentContract.Presenter {

    override var view: SourceFragmentContract.View? = null

    override lateinit var source: Source<*, *, *, *, *, *, *, *>

    override fun getFilesLiveData(): LiveData<List<SourceFile>> {
        val viewType = ViewType.getFromValue(prefsManager.getInt(
            Constants.Prefs.VIEW_TYPE_KEY,
            ViewType.LIST.value
        ))
        val showFoldersFirst = prefsManager.getBoolean(Constants.Prefs.FOLDER_FIRST_KEY, true)
        val sortType = prefsManager.getInt(Constants.Prefs.SORT_TYPE_KEY, SortType.NAME.ordinal)
        val orderType = prefsManager.getInt(Constants.Prefs.ORDER_TYPE_KEY, OrderType.ASCENDING.ordinal)

        val query = buildString {
            append("SELECT * FROM SourceFile ORDER BY ")

            if (showFoldersFirst) {
                append("isDirectory ASC, ")
            }

            append(when (sortType) {
                SortType.NAME.ordinal -> "name "
                SortType.TYPE.ordinal -> "fileType "
                SortType.SIZE.ordinal -> "size "
                SortType.MODIFIED_TIME.ordinal -> "modifiedTime "
                else -> "name "
            })

            append(when (orderType) {
                OrderType.ASCENDING.ordinal -> "ASC"
                OrderType.DESCENDING.ordinal -> "DESC"
                else -> "ASC"
            })
        }
        return fileRepository.execRaw(SimpleSQLiteQuery(query))
    }

    override fun setFileSource(source: Source<*, *, *, *, *, *, *, *>) {
        val index = sourceManager.sources.indexOf(source)
        if (index == -1) {
            sourceManager.sources.add(source)
        } else {
            sourceManager.sources.removeAt(index)
            sourceManager.sources.add(index, source)
        }
        this.source = source
    }

    override fun checkState() {
        if (prefsManager.hasSourceToken(source.sourceId)) {
            view?.hideConnectButton()
        }
    }

    override fun onCheckPermissions(name: String, requestCode: Int) {
        view?.onCheckPermissions(name, requestCode)
    }

    override fun onConnect() {
        if (!connectionManager.isConnected) {
            view?.showNoConnectionSnackBar()
            return
        }

        view?.authenticate()
    }

    override fun onNoConnection() {
        view?.showConnectButton()
        view?.hideProgressBar()
        view?.showNoConnectionSnackBar()
    }

    override fun onLoadStarted() {
        view?.hideConnectButton()
        view?.showProgressBar()
    }

    override fun onLoadAborted() {
        view?.showConnectButton()
        view?.hideProgressBar()
    }

    override fun onLoadError(errorMessage: String?) {
        view?.showConnectButton()
        view?.hideProgressBar()
        view?.showLoadError(source.sourceId)
    }

    override fun onLoadComplete(rootFile: SourceFile) {
        view?.pushBreadCrumb(
            rootFile.fileId,
            SourceType.values()[rootFile.sourceId].sourceName,
            false
        )
        view?.populateList()
        source.currentDirectory = rootFile

        view?.hideProgressBar()
        view?.hideSourceLogo()
    }

    override fun onLogout() {
        view?.hideFileList()
        view?.popAllBreadCrumbs()
    }

    override fun onFileSelected(
        file: TreeNode<SourceFile>,
        isChecked: Boolean,
        position: Int
    ) {
        if (source.isMultiSelectEnabled) {
            if (isChecked) {
                selectedFilesManager.currentSelectedFiles.add(file)
            } else {
                selectedFilesManager.currentSelectedFiles.remove(file)
            }

            view?.setSelectedCountTitle(selectedFilesManager.currentSelectedFiles.size)
            return
        }

        if (file.data.isDirectory) {
            source.currentDirectory = file

            val name = if (file.parent == null) {
                SourceType.values()[file.data.sourceId].sourceName
            } else {
                file.data.name
            }

            view?.pushBreadCrumb(
                file,
                file.parent != null,
                name
            )
            view?.updateFileList()
            return
        }

        if (Utils.getStorageStats(Environment.getExternalStorageDirectory()).freeSpace > file.data.size) {
            selectedFilesManager.startNewSelection()

            sourceManager.addFileAction(
                selectedFilesManager.operationCount - 1,
                FileAction.OPEN
            )
            view?.startActionOpen(file.data)
        } else {
            view?.showNotEnoughSpaceSnackBar()
        }
    }

    override fun onFileLongSelected(file: TreeNode<SourceFile>) {
        if (source.isMultiSelectEnabled) {
            return
        }
        source.isMultiSelectEnabled = true

        if (selectedFilesManager.operationCount == 0) {
            selectedFilesManager.startNewSelection()
        }
        selectedFilesManager
            .getSelectedFiles(selectedFilesManager.operationCount)
            ?.add(file)

        val size = selectedFilesManager
            .getSelectedFiles(selectedFilesManager.operationCount)
            ?.size

        view?.setSelectedCountTitle(size ?: 0)
    }

    override fun onBreadCrumbSelected(name: String, crumbsToPop: Int) {
        if (source.currentDirectory.data.name == name) {
            return
        }

        for (i in 0 until crumbsToPop) {
            view?.popBreadCrumb()
        }

        val selectedParent = TreeNode
            .searchForParent(source.currentDirectory, name) ?: return

        source.currentDirectory = selectedParent
        view?.updateFileList()
    }
}