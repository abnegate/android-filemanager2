package com.jakebarnby.filemanager.ui.sources

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import com.jakebarnby.batteries.core.view.ListView
import com.jakebarnby.batteries.mvp.presenter.ListPresenter
import com.jakebarnby.batteries.mvp.view.MvpView
import com.jakebarnby.filemanager.data.FileDao
import com.jakebarnby.filemanager.data.FileDatabase
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.managers.SourceManager
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.util.TreeNode

interface SourceFragmentContract {

    interface Presenter : com.jakebarnby.batteries.mvp.presenter.Presenter<View> {

        var source: Source<*,*,*,*,*,*,*,*>
        var sourceManager: SourceManager
        var selectedFilesManager: SelectedFilesManager
        var prefsManager: PreferenceManager
        var connectionManager: ConnectionManager
        var fileRepository: FileDao

        fun setFileSource(source: Source<*,*,*,*,*,*,*,*>)
        fun checkState()
        fun onCheckPermissions(name: String, requestCode: Int)
        fun onConnect()
        fun onNoConnection()
        fun onLoadStarted()
        fun onLoadAborted()
        fun onLoadError(errorMessage: String?)
        fun onLoadComplete(rootFile: SourceFile)
        fun onLogout()

        fun onFileSelected(
            file: TreeNode<SourceFile>,
            isChecked: Boolean,
            position: Int
        )

        fun onFileLongSelected(file: TreeNode<SourceFile>)
        fun onBreadCrumbSelected(name: String, crumbsToPop: Int)

        fun getFilesLiveData(): LiveData<List<SourceFile>>
    }

    interface View : MvpView {
        fun populateList()

        fun showProgressBar()
        fun hideProgressBar()

        fun showConnectButton()
        fun hideConnectButton()

        fun showSourceLogo()
        fun hideSourceLogo()

        fun showLoadError(sourceName: Int)

        fun showFileList()
        fun hideFileList()

        fun showNoConnectionSnackBar()
        fun showNotEnoughSpaceSnackBar()

        fun setSelectedCountTitle(size: Int)
        fun updateFileList()

        fun pushBreadCrumb(
            fileId: Long,
            name: String,
            arrowVisible: Boolean
        )
        fun popBreadCrumb()
        fun popAllBreadCrumbs()

        fun startActionOpen(toOpen: SourceFile)

        fun authenticate()

        fun showAppSettings()

        fun onCheckPermissions(name: String, requestCode: Int)

    }

    interface ListPresenter : com.jakebarnby.batteries.mvp.presenter.ListPresenter<SourceFile, ListView>

    interface ListView : com.jakebarnby.batteries.core.view.ListView {
        fun setFileName(name: String)
        fun setImage(url: String)
        fun setImage(@DrawableRes imageId: Int)
        fun setSelected(selected: Boolean)
        fun setSize(size: Int)
        fun setModifiedDate(date: String)
    }
}