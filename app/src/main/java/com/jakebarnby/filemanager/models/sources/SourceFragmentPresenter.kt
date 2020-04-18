package com.jakebarnby.filemanager.models.sources

import android.os.Environment
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.managers.SourceManager
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils
import javax.inject.Inject

class SourceFragmentPresenter @Inject constructor(
    override var sourceManager: SourceManager,
    override var selectedFilesManager: SelectedFilesManager,
    override var prefsManager: PreferenceManager,
    override var connectionManager: ConnectionManager
) : SourceFragmentContract.Presenter {

    override var view: SourceFragmentContract.View? = null

    override lateinit var source: Source

    override fun setFileSource(source: Source) {
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

    override fun onLoadComplete(rootFile: TreeNode<SourceFile>) {
        view?.pushBreadCrumb(
            rootFile,
            false,
            SourceType.values()[rootFile.data.sourceId].sourceName
        )
        view?.populateList()

        source.rootNode = rootFile
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