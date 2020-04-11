package com.jakebarnby.filemanager.sources

import android.content.Intent
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
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

        fun onOpen(path: String)
        fun onCut()
        fun onCopy()
        fun onPaste()
        fun onDelete()
        fun onConfirmDelete()
        fun onBack()

        fun onSourceSelected(position: Int)
        fun onShowViewAsDialog()
        fun onShowCreateFolderDialog()
        fun onShowRenameDialog()
        fun onShowCreateZipDialog()
        fun onShowPropertiesDialog()
        fun onShowProgressDialog(intent: Intent)
        fun onShowUsageDialog()
        fun onShowLogoutDialog()
        fun onShowSortByDialog()
        fun onShowSettingsDialog()
        fun onPrepareContextMenu()

        fun onSearch(query: String)
        fun onNavigateToFile(fileNode: TreeNode<SourceFile>)

        fun onServiceActionComplete(
            operationId: Int,
            path: String? = null
        )
    }

    interface View : ViewPager.OnPageChangeListener,
        SearchView.OnQueryTextListener {

        var menuListener: SimpleMenuListenerAdapter

        fun setTitle(@StringRes titleId: Int)

        fun addLocalSourceView(
            position: Int,
            name: String,
            rootPath: String
        )
        fun changeToSource(position: Int)
        fun removeLocalSourceView(position: Int)

        fun showAd()
        fun showSnackbar(message: String)
        fun showSnackbar(@StringRes messageId: Int)
        fun showSnackbar(@StringRes messageId: Int, vararg formatArgs: String)
        fun showOpeningDialog(path: String)
        fun showViewAsDialog()
        fun showCreateFolderDialog()
        fun showRenameDialog()
        fun showDeleteDialog(deleteCount: Int)
        fun showCreateZipDialog()
        fun showPropertiesDialog()
        fun showProgressDialog(intent: Intent)
        fun isProgressShowing(): Boolean
        fun hideProgressDialog()
        fun showUsageDialog()
        fun showLogoutDialog()
        fun showSortByDialog()
        fun showSettingsDialog()
        fun showErrorDialog(message: String?)
        fun showSearchDialog(results: List<TreeNode<SourceFile>>, sourceNames: List<String>)
        fun updateProgressDialog(intent: Intent)

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
    }
}