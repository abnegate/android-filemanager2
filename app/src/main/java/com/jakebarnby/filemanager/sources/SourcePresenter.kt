package com.jakebarnby.filemanager.sources

import android.content.Intent
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.BillingManager
import com.jakebarnby.filemanager.managers.ConnectionManager
import com.jakebarnby.filemanager.managers.PreferenceManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceManager
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.TreeNode
import java.io.File

class SourcePresenter(
    override var sourceManager: SourceManager,
    override var prefsManager: PreferenceManager,
    override var billingManager: BillingManager,
    override var connectionManager: ConnectionManager
) : SourceActivityContract.Presenter {

    override var view: SourceActivityContract.View? = null

    override fun onAddLocalSources(
        rootPaths: List<String>,
        sdCardString: String,
        usbString: String
    ) {
        if (rootPaths.isEmpty()) {
            return
        }

        for (i in rootPaths.indices) {
            val split = rootPaths[i].split("/").toTypedArray()
            val rootDirTitle = split[split.size - 1]
            var alreadyAdded = false
            for (source in sourceManager.sources) {
                if (source.rootNode.data.path.contains(rootDirTitle)) {
                    alreadyAdded = true
                    break
                }
            }
            if (alreadyAdded) {
                continue
            }

            val newName = if (i == 0) {
                sdCardString
            } else {
                usbString + i
            }

            view?.addLocalSourceView(
                i + 1,
                newName,
                rootPaths[i]
            )
        }
    }

    override fun onAddLocalSource(
        rootPath: String,
        localSourceCount: Int,
        sdCardString: String,
        usbString: String
    ) {
        val newSourceName = if (localSourceCount == 1) {
            sdCardString
        } else {
            usbString + (localSourceCount - 1).toString()
        }

        view?.addLocalSourceView(
            localSourceCount,
            newSourceName,
            rootPath
        )
    }

    override fun onRemoveLocalSource(roothPath: String) {
        for (i in sourceManager.sources.indices) {
            val source = sourceManager.sources[i]

            if (source.sourceType == SourceType.LOCAL &&
                source.isFilesLoaded &&
                roothPath.contains(source.rootNode.data.path)) {

                view?.removeLocalSourceView(i)
            }
        }
    }

    override fun onStartMultiSelect() {
        if (sourceManager.activeSource.isMultiSelectEnabled) {
            return
        }
        sourceManager.activeSource.isMultiSelectEnabled = true

        if (SelectedFilesManager.operationCount == 0) {
            SelectedFilesManager.startNewSelection()
        }
    }

    override fun disableAllMultiSelect() {
        sourceManager.sources.forEach {
            it.isMultiSelectEnabled = false
        }
    }

    override fun onOpen(path: String) {
        if (view?.isProgressShowing() != true) {
            val filename = path.substring(
                path.lastIndexOf(File.separator) + 1,
                path.length
            )
            view?.showOpeningDialog(filename)
        } else {
            view?.viewFileInExternalApp(path)
        }
    }

    override fun onCut() {
        if (SelectedFilesManager.currentSelectedFiles.size > 0) {
            sourceManager.addFileAction(SelectedFilesManager.operationCount, FileAction.CUT)
            view?.showSnackbar(R.string.cut)
        } else {
            view?.showSnackbar(R.string.err_no_selection)
        }
        disableAllMultiSelect()
    }

    override fun onCopy() {
        if (SelectedFilesManager.currentSelectedFiles.size > 0) {
            sourceManager.addFileAction(SelectedFilesManager.operationCount, FileAction.COPY)
            view?.showSnackbar(R.string.copied)
        } else {
            view?.showSnackbar(R.string.err_no_selection)
        }
        disableAllMultiSelect()
    }

    override fun onPaste() {
        if (!pastePreconditions()) {
            return
        }

        if (sourceManager.getFileAction(SelectedFilesManager.operationCount) != null) {
            sourceManager.activeSource.isMultiSelectEnabled = false

            view?.setTitle(R.string.app_name)
            view?.toggleContextMenu(false)

            SelectedFilesManager.addActionableDirectory(
                SelectedFilesManager.operationCount,
                sourceManager.activeSource.currentDirectory
            )

            val curAction = sourceManager.getFileAction(SelectedFilesManager.operationCount)
            if (curAction == FileAction.COPY) {
                view?.startCopyService()
            } else if (curAction == FileAction.CUT) {
                view?.startMoveService()
            }

            sourceManager.activeSource
                .decreaseFreeSpace(SelectedFilesManager.currentCopySize)

            SelectedFilesManager.startNewSelection()
        }
    }

    private fun pastePreconditions(): Boolean {
        if (!connectionManager.isConnected) {
            view?.showSnackbar(R.id.err_no_connection)
            return false
        }
        if (sourceManager.activeSource.sourceType == SourceType.LOCAL &&
            sourceManager.activeSource.sourceName != Constants.Sources.LOCAL) {
            view?.showSnackbar(R.string.err_no_ext_write)
            return false
        }
        if (!sourceManager.activeSource.isLoggedIn) {
            view?.showSnackbar(R.string.err_not_logged_in)
            return false
        } else if (!sourceManager.activeSource.isFilesLoaded) {
            view?.showSnackbar(R.string.err_not_loaded)
            return false
        }

        val copySize = SelectedFilesManager.currentCopySize
        if (copySize > sourceManager.activeSource.freeSpace) {
            view?.showSnackbar(
                R.string.err_no_free_space,
                sourceManager.activeSource.sourceName
            )
            return false
        }
        return true
    }

    override fun onDelete() {
        val size = SelectedFilesManager.currentSelectedFiles.size
        if (size < 0) {
            view?.showSnackbar(R.string.err_no_selection)
            return
        }
        view?.showDeleteDialog(size)
    }

    override fun onConfirmDelete() {
        val activeDirectory = sourceManager.activeSource.currentDirectory
        sourceManager.addFileAction(SelectedFilesManager.operationCount, FileAction.DELETE)

        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            activeDirectory
        )

        disableAllMultiSelect()

        view?.toggleContextMenu(false)
        view?.setTitle(R.string.app_name)
        view?.startDeleteService()

        sourceManager.activeSource
            .increaseFreeSpace(SelectedFilesManager.currentCopySize)

        SelectedFilesManager.startNewSelection()
    }

    override fun onBack() {
        if (sourceManager.activeSource.isMultiSelectEnabled) {
            sourceManager.activeSource.isMultiSelectEnabled = false

            view?.toggleContextMenu(false)
            view?.setTitle(R.string.app_name)

            SelectedFilesManager.currentSelectedFiles.clear()
        } else if (sourceManager.activeSource.isLoggedIn) {
//            val previousPosition = sourceManager.activeSource
//                .currentDirectory.parent?.data?.positionToRestore ?: 0
//
//            view?.scrollToPosition(previousPosition)
//
//            (activeFragment?.recycler?.layoutManager as? LinearLayoutManager)
//                ?.scrollToPositionWithOffset(previousPosition, 0)

            sourceManager.activeSource.currentDirectory =
                sourceManager.activeSource.currentDirectory.parent ?: return

//            activeFragment?.refreshRecycler()
//            activeFragment?.popBreadcrumb()
        }
    }

    override fun onSourceSelected(position: Int) {
        sourceManager.activeSource = sourceManager.sources[position]
    }

    override fun onShowViewAsDialog() {
        TODO("Not yet implemented")
    }

    override fun onShowCreateFolderDialog() {
        if (!connectionManager.isConnected) {
            view?.showSnackbar(R.id.err_no_connection)
            return
        }
        if (!sourceManager.activeSource.isLoggedIn) {
            view?.showSnackbar(R.string.err_not_logged_in)
            return
        } else if (!sourceManager.activeSource.isFilesLoaded) {
            view?.showSnackbar(R.string.err_not_loaded)
            return
        }

        sourceManager.addFileAction(
            SelectedFilesManager.operationCount,
            FileAction.NEW_FOLDER
        )
        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            sourceManager.activeSource.currentDirectory
        )

        view?.showCreateFolderDialog()
    }

    override fun onShowRenameDialog() {
        if (!connectionManager.isConnected) {
            view?.showSnackbar(R.id.err_no_connection)
            return
        }

        view?.setTitle(R.string.app_name)
        view?.toggleContextMenu(false)

        disableAllMultiSelect()

        val size = SelectedFilesManager.currentSelectedFiles.size
        if (size == 0) {
            view?.showSnackbar(R.string.err_no_selection)
            return
        } else if (size > 1) {
            view?.showSnackbar(R.string.err_too_many_selected)
            return
        }

        sourceManager.addFileAction(
            SelectedFilesManager.operationCount,
            FileAction.RENAME
        )
        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            sourceManager.activeSource.currentDirectory
        )

        view?.showRenameDialog()
    }

    override fun onShowCreateZipDialog() {
        if (!connectionManager.isConnected) {
            view?.showSnackbar(R.id.err_no_connection)
            return
        }
        if (!sourceManager.activeSource.isLoggedIn) {
            view?.showSnackbar(R.string.err_not_logged_in)
            return
        } else if (!sourceManager.activeSource.isFilesLoaded) {
            view?.showSnackbar(R.string.err_not_loaded)
            return
        }

        view?.setTitle(R.string.app_name)
        view?.toggleContextMenu(false)

        disableAllMultiSelect()

        sourceManager.addFileAction(
            SelectedFilesManager.operationCount,
            FileAction.NEW_ZIP
        )

        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            sourceManager.activeSource.currentDirectory
        )

        view?.showCreateZipDialog()
    }

    override fun onShowPropertiesDialog() {
        val size: Int = SelectedFilesManager.currentSelectedFiles.size
        if (size == 0) {
            view?.showSnackbar(R.string.err_no_selection)
                return
        }
        view?.showPropertiesDialog()
    }

    override fun onShowProgressDialog(intent: Intent) {
        TODO("Not yet implemented")
    }

    override fun onShowUsageDialog() {
        view?.showUsageDialog()
    }

    override fun onShowLogoutDialog() {
        TODO("Not yet implemented")
    }

    override fun onShowSortByDialog() {
        TODO("Not yet implemented")
    }

    override fun onShowSettingsDialog() {
        TODO("Not yet implemented")
    }

    override fun onPrepareContextMenu() {
        if (sourceManager.getFileAction(SelectedFilesManager.operationCount) == null) {
            view?.hidePasteMenuItem()
        }

        if (SelectedFilesManager.currentSelectedFiles.size > 1) {
            view?.hideRenameMenuItem()
        }
    }

    override fun onSearch(query: String) {
        val allResults = mutableListOf<TreeNode<SourceFile>>()
        val sourceNames = mutableListOf<String>()

        for (source in sourceManager.sources) {
            val results = TreeNode.searchForChildren(source.rootNode, query)
            if (results.isNotEmpty()) {
                allResults.addAll(results)
                sourceNames.add(source.sourceName)
            }
        }

        allResults.sortWith(Comparator { node1, node2 ->
            node1.data.name.compareTo(node2.data.name, true)
        })

        view?.showSearchDialog(allResults, sourceNames)
    }

    override fun onNavigateToFile(fileNode: TreeNode<SourceFile>) {
        for (i in sourceManager.sources.indices) {
            if (sourceManager.sources[i].sourceName != fileNode.data.sourceName) {
                continue
            }

            val newDir = if (fileNode.data.isDirectory) {
                fileNode
            } else {
                fileNode.parent
            }

            if (newDir == null) {
                //TODO Log error
                return
            }

            sourceManager.activeSource.currentDirectory = newDir

            view?.changeToSource(i)
            view?.popAllBreadCrumbs()
            view?.pushAllBreadCrumbs(newDir)
        }
    }

    override fun onServiceActionComplete(operationId: Int, path: String?) {
        if (path == null) {
            // TODO: Log error
            return
        }

        when (sourceManager.getFileAction(operationId)) {
            FileAction.CUT,
            FileAction.COPY,
            FileAction.DELETE,
            FileAction.RENAME,
            FileAction.NEW_FOLDER,
            FileAction.NEW_ZIP -> {
                view?.refreshFileLists(operationId)
            }
            FileAction.OPEN -> {
                onOpen(path)
            }
        }

        view?.hideProgressDialog()

        val operationCount = prefsManager.getInt(
            Prefs.OPERATION_COUNT_KEY,
            0
        ) + 1
        prefsManager.savePref(
            Prefs.OPERATION_COUNT_KEY,
            operationCount
        )

        val removeAds = prefsManager.getBoolean(
            Prefs.HIDE_ADS_KEY,
            false
        )

        if (operationCount == Constants.Ads.SHOW_AD_COUNT && !removeAds) {
            view?.showAd()
        }
    }
}