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
import com.jakebarnby.filemanager.models.SourceListener
import com.jakebarnby.filemanager.util.TreeNode

interface SourceFragmentContract {

    interface Presenter : com.jakebarnby.batteries.mvp.presenter.Presenter<View>, SourceListener {

        var source: Source<*,*,*,*,*,*,*,*>
        var sourceManager: SourceManager
        var selectedFilesManager: SelectedFilesManager
        var prefsManager: PreferenceManager
        var connectionManager: ConnectionManager
        var fileRepository: FileDao

        fun setFileSource(source: Source<*,*,*,*,*,*,*,*>)
        fun onFileSelected(file: SourceFile)
        fun onFileLongSelected(file: SourceFile)
        fun onBreadCrumbSelected(name: String, crumbsToPop: Int)

        fun getFilesLiveData(): LiveData<List<SourceFile>>
    }

    interface View : MvpView {
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

        fun pushBreadCrumb(
            fileId: Long,
            name: String,
            arrowVisible: Boolean
        )
        fun popBreadCrumb()
        fun popAllBreadCrumbs()

        fun startActionOpen(toOpen: SourceFile)

        fun startAuthentication()

        fun showAppSettings()

        fun onCheckPermissions(name: String, requestCode: Int)

    }

    interface ListPresenter : com.jakebarnby.batteries.mvp.presenter.ListPresenter<SourceFile, ListView> {
        fun onItemLongSelected(position: Int)
    }

    interface ListView : com.jakebarnby.batteries.core.view.ListView {
        fun setFileName(name: String)
        fun setImage(url: String)
        fun setImage(@DrawableRes imageId: Int)
        fun showSelection()
        fun hideSelection()
        fun animateSelectionIn()
        fun setSelected(selected: Boolean)
        fun setSize(size: Int)
        fun setModifiedDate(date: String)
    }
}