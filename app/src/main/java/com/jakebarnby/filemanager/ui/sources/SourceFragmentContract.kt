package com.jakebarnby.filemanager.ui.sources

import com.jakebarnby.filemanager.core.BasePresenter
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceManager
import com.jakebarnby.filemanager.util.TreeNode

interface SourceFragmentContract {

    interface Presenter : BasePresenter<View> {
        var source: Source
        var sourceManager: SourceManager
        var prefsManager: PreferenceManager
        var connectionManager: ConnectionManager

        fun checkState()
        fun onConnect()
        fun onNoConnection()
        fun onLoadStarted()
        fun onLoadAborted()
        fun onLoadError(errorMessage: String?)
        fun onLoadComplete(rootFile: TreeNode<SourceFile>)
        fun onLogout()

        fun onFileSelected(
            file: TreeNode<SourceFile>,
            isChecked: Boolean,
            position: Int
        )
        fun onFileLongSelected(file: TreeNode<SourceFile>)
        fun onBreadCrumbSelected(name: String, crumbsToPop: Int)
    }

    interface View {
        fun populateList()

        fun showProgressBar()
        fun hideProgressBar()

        fun showConnectButton()
        fun hideConnectButton()

        fun showSourceLogo()
        fun hideSourceLogo()

        fun showLoadError(sourceName: String)

        fun showFileList()
        fun hideFileList()

        fun showNoConnectionSnackBar()
        fun showNotEnoughSpaceSnackBar()

        fun setSelectedCountTitle(size: Int)
        fun updateFileList()

        fun pushBreadCrumb(
            directory: TreeNode<SourceFile>,
            arrowVisible: Boolean,
            name: String
        )
        fun popBreadCrumb()
        fun popAllBreadCrumbs()

        fun startActionOpen(toOpen: SourceFile)

        fun authenticate()

        fun showAppSettings()

    }

    interface ListPresenter {

    }

    interface ListView {

    }
}