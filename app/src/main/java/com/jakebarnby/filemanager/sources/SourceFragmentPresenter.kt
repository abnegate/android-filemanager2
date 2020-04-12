package com.jakebarnby.filemanager.sources

import android.os.Environment
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceManager
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode
import com.jakebarnby.filemanager.util.Utils

class SourceFragmentPresenter(
    override var sourceManager: SourceManager,
    override var prefsManager: PreferenceManager,
    override var connectionManager: ConnectionManager

) : SourceFragmentContract.Presenter {

    override var view: SourceFragmentContract.View? = null

    override lateinit var source: Source

    override fun checkState() {
        if (prefsManager.hasSourceToken(source.sourceName)) {
            view?.hideConnectButton()
        }
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
        view?.showLoadError(source.sourceName)
    }

    override fun onLoadComplete(rootFile: TreeNode<SourceFile>) {
        view?.pushBreadCrumb(
            rootFile,
            false,
            rootFile.data.sourceName
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
                SelectedFilesManager.currentSelectedFiles.add(file)
            } else {
                SelectedFilesManager.currentSelectedFiles.remove(file)
            }

            view?.setSelectedCountTitle(SelectedFilesManager.currentSelectedFiles.size)
        } else {
            if (file.data.isDirectory) {
//                source.currentDirectory.data.positionToRestore = (recycler!!.layoutManager as LinearLayoutManager?)
//                    ?.findFirstVisibleItemPosition() ?: 0
                source.currentDirectory = file

                val name = if (file.parent == null) {
                    file.data.sourceName
                } else {
                    file.data.name
                }

                view?.pushBreadCrumb(
                    file,
                    file.parent != null,
                    name
                )
                view?.updateFileList()
            } else {
                if (Utils.getStorageStats(Environment.getExternalStorageDirectory()).freeSpace > file.data.size) {
                    SelectedFilesManager.startNewSelection()

                    sourceManager.addFileAction(
                        SelectedFilesManager.operationCount - 1,
                        FileAction.OPEN
                    )

                    view?.startActionOpen(file.data)
                } else {
                    view?.showNotEnoughSpaceSnackBar()

                }
            }
        }
    }

    override fun onFileLongSelected(file: TreeNode<SourceFile>) {
        if (source.isMultiSelectEnabled) {
            return
        }
        source.isMultiSelectEnabled = true

        if (SelectedFilesManager.operationCount == 0) {
            SelectedFilesManager.startNewSelection()
        }
        SelectedFilesManager
            .getSelectedFiles(SelectedFilesManager.operationCount)
            ?.add(file)

        val size = SelectedFilesManager
            .getSelectedFiles(SelectedFilesManager.operationCount)
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

        val previousPosition = selectedParent.data.positionToRestore

//        (recycler?.layoutManager as? LinearLayoutManager)
//            ?.scrollToPositionWithOffset(previousPosition, 0)

        source.currentDirectory = selectedParent
        view?.updateFileList()
    }
}