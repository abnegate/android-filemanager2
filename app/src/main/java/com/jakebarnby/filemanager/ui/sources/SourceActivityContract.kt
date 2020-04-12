package com.jakebarnby.filemanager.ui.sources

import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
import com.jakebarnby.filemanager.core.BasePresenter
import com.jakebarnby.filemanager.managers.BillingManager
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceManager
import com.jakebarnby.filemanager.util.TreeNode
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter

interface SourceActivityContract {

    interface Presenter : BasePresenter<View> {

        var sourceManager: SourceManager
        var prefsManager: PreferenceManager
        var billingManager: BillingManager
        var connectionManager: ConnectionManager

        fun onAddLocalSources(
            rootPaths: List<String>,
            sdCardString: String,
            usbString: String
        )

        fun onAddLocalSource(
            rootPath: String,
            localSourceCount: Int,
            sdCardString: String,
            usbString: String
        )

        fun onRemoveLocalSource(
            roothPath: String
        )

        fun onStartMultiSelect()
        fun disableAllMultiSelect()

        fun onSourceSelected(position: Int)
        fun onOpen(path: String)
        fun onCut()
        fun onCopy()
        fun onPaste()
        fun onDelete()
        fun onConfirmDelete()
        fun onCreateFolder()
        fun onRename()
        fun onCreateZip()
        fun onChangeViewType()
        fun onShowProperties()
        fun onShowProgress()
        fun onShowUsage()
        fun onLogout()
        fun onSortBy()
        fun onShowSettings()
        fun onPrepareContextMenu()

        fun onSearch(query: String)
        fun onNavigateToFile(fileNode: TreeNode<SourceFile>)

        fun onServiceActionComplete(
            operationId: Int,
            path: String? = null
        )

        fun onBack()
    }

    interface View : ViewPager.OnPageChangeListener,
        SearchView.OnQueryTextListener {

        var menuListener: SimpleMenuListenerAdapter

        fun addLocalSourceView(
            position: Int,
            name: String,
            rootPath: String
        )

        fun changeToSource(position: Int)
        fun removeLocalSourceView(position: Int)

        fun showAd()
        fun showOpeningDialog(path: String)
        fun showViewAsDialog()
        fun showCreateFolderDialog()
        fun showRenameDialog()
        fun showDeleteDialog(deleteCount: Int)
        fun showCreateZipDialog()
        fun showPropertiesDialog()
        fun showProgressDialog(title: String, maxProgress: Int)
        fun isProgressShowing(): Boolean
        fun hideProgressDialog()
        fun showUsageDialog()
        fun showLogoutDialog()
        fun showSortByDialog()
        fun showSettingsDialog()
        fun showSearchDialog(results: List<TreeNode<SourceFile>>, sourceNames: List<String>)
        fun updateProgressDialog(newProgress: Int)

        fun viewFileInExternalApp(path: String)
        fun toggleContextMenu(enabled: Boolean)

        fun hidePasteMenuItem()
        fun hideRenameMenuItem()

        fun blurFileList()
        fun clearBlur()

        fun startDeleteService()
        fun startCopyService()
        fun startMoveService()

        fun refreshFileLists(operationId: Int)
        fun popAllBreadCrumbs()
        fun pushAllBreadCrumbs(newDir: TreeNode<SourceFile>)

        fun showCutSnackBar()
        fun showNoSelectionSnackBar()
        fun showCopiedSnackBar()
        fun showNoConnectionSnackBar()
        fun showUnwritableDestinationSnackBar()
        fun showNotLoggedInSnackBar()
        fun showNotLoadedSnackBar()
        fun showNotEnoughSpaceSnackBar()
        fun showTooManySelectedSnackBar()
        fun showNoAppAvailableSnackBar()
        fun setAppNameTitle()
    }
}