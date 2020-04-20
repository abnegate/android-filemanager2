package com.jakebarnby.filemanager.ui.sources

import com.jakebarnby.filemanager.managers.*
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.models.SourceConnectionType
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.TreeNode
import java.io.File
import javax.inject.Inject

class SourcePresenter @Inject constructor(
    override var sourceManager: SourceManager,
    override var selectedFilesManager: SelectedFilesManager,
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
                if (source.rootFile.data.path.contains(rootDirTitle)) {
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
        for (sourceType: SourceType in SourceType.values()) {
            val source = sourceManager.sources[sourceType.id]

            if (source.sourceConnectionType == SourceConnectionType.LOCAL &&
                source.isFilesLoaded &&
                roothPath.contains(source.rootNode.data.path)) {

                view?.removeLocalSourceView(sourceType.id)
            }
        }
    }

    override fun onStartMultiSelect() {
        if (sourceManager.activeSource.isMultiSelectEnabled) {
            return
        }
        sourceManager.activeSource.isMultiSelectEnabled = true

        if (selectedFilesManager.operationCount == 0) {
            selectedFilesManager.startNewSelection()
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
        if (selectedFilesManager.currentSelectedFiles.size > 0) {
            sourceManager.addFileAction(selectedFilesManager.operationCount, FileAction.CUT)
            view?.showCutSnackBar()
        } else {
            view?.showNoSelectionSnackBar()
        }
        disableAllMultiSelect()
    }

    override fun onCopy() {
        if (selectedFilesManager.currentSelectedFiles.size > 0) {
            sourceManager.addFileAction(selectedFilesManager.operationCount, FileAction.COPY)
            view?.showCopiedSnackBar()
        } else {
            view?.showNoSelectionSnackBar()
        }
        disableAllMultiSelect()
    }

    override fun onPaste() {
        if (!pastePreconditions()) {
            return
        }

        if (sourceManager.getFileAction(selectedFilesManager.operationCount) != null) {
            sourceManager.activeSource.isMultiSelectEnabled = false

            view?.setAppNameTitle()
            view?.toggleContextMenu(false)

            selectedFilesManager.addActionableDirectory(
                selectedFilesManager.operationCount,
                sourceManager.activeSource.currentDirectory
            )

            val curAction = sourceManager.getFileAction(selectedFilesManager.operationCount)
            if (curAction == FileAction.COPY) {
                view?.startCopyService()
            } else if (curAction == FileAction.CUT) {
                view?.startMoveService()
            }

//            sourceManager.activeSource.decreaseFreeSpace(selectedFilesManager.currentSelectionSize)

            selectedFilesManager.startNewSelection()
        }
    }

    private fun pastePreconditions(): Boolean {
        if (!connectionManager.isConnected) {
            view?.showNoConnectionSnackBar()
            return false
        }
        if (sourceManager.activeSource.sourceConnectionType == SourceConnectionType.LOCAL &&
            sourceManager.activeSource.sourceId != SourceType.LOCAL.id) {
            view?.showUnwritableDestinationSnackBar()
            return false
        }
        if (!sourceManager.activeSource.isLoggedIn) {
            view?.showNotLoggedInSnackBar()
            return false
        } else if (!sourceManager.activeSource.isFilesLoaded) {
            view?.showNotLoadedSnackBar()
            return false
        }

        val copySize = selectedFilesManager.currentSelectionSize
        if (copySize > (sourceManager.activeSource.storageInfo?.freeBytes ?: 0)) {
            view?.showNotEnoughSpaceSnackBar()
            return false
        }
        return true
    }

    override fun onDelete() {
        val size = selectedFilesManager.currentSelectedFiles.size
        if (size < 0) {
            view?.showNoSelectionSnackBar()
            return
        }
        view?.showDeleteDialog(size)
    }

    override fun deleteFiles() {
        val activeDirectory = sourceManager.activeSource.currentDirectory
        sourceManager.addFileAction(
            selectedFilesManager.operationCount,
            FileAction.DELETE
        )

        selectedFilesManager.addActionableDirectory(
            selectedFilesManager.operationCount,
            activeDirectory
        )

        disableAllMultiSelect()

        view?.toggleContextMenu(false)
        view?.setAppNameTitle()
        view?.startDeleteService()

        //sourceManager.activeSource.increaseFreeSpace(selectedFilesManager.currentSelectionSize)

        selectedFilesManager.startNewSelection()
    }

    override fun onBack() {
        if (sourceManager.activeSource.isMultiSelectEnabled) {
            sourceManager.activeSource.isMultiSelectEnabled = false

            view?.toggleContextMenu(false)
            view?.setAppNameTitle()

            selectedFilesManager.clearCurrentSelection()
        } else if (sourceManager.activeSource.isLoggedIn) {
//            val previousPosition = sourceManager.activeSource
//                .currentDirectory.parent?.data?.positionToRestore ?: 0
//
//            view?.scrollToPosition(previousPosition)
//
//            (activeFragment?.recycler?.layoutManager as? LinearLayoutManager)
//                ?.scrollToPositionWithOffset(previousPosition, 0)

//            sourceManager.activeSource.currentDirectory =
//                sourceManager.activeSource.currentDirectory.parent ?: return

//            activeFragment?.refreshRecycler()
//            activeFragment?.popBreadcrumb()
        }
    }

    override fun onSourceSelected(position: Int) {
        sourceManager.activeSource = sourceManager.sources[position]
    }

    override fun onChangeViewType() {
        TODO("Not yet implemented")
    }

    override fun onCreateFolder() {
        if (!connectionManager.isConnected) {
            view?.showNoConnectionSnackBar()
            return
        }
        if (!sourceManager.activeSource.isLoggedIn) {
            view?.showNotLoggedInSnackBar()
            return
        } else if (!sourceManager.activeSource.isFilesLoaded) {
            view?.showNotLoadedSnackBar()
            return
        }

        sourceManager.addFileAction(
            selectedFilesManager.operationCount,
            FileAction.NEW_FOLDER
        )
        selectedFilesManager.addActionableDirectory(
            selectedFilesManager.operationCount,
            sourceManager.activeSource.currentDirectory
        )

        view?.showCreateFolderDialog()
    }

    override fun createFolder(name: String) {
        val activeDirectory = selectedFilesManager.getActionableDirectory(
            selectedFilesManager.operationCount
        )
        for (file in activeDirectory?.children ?: emptyList<TreeNode<SourceFile>>()) {
            if (!file.data.isDirectory) {
                continue
            }
            if (file.data.name == name) {
                view?.showFileExistsSnackBar()
                return
            }
        }
        view?.startCreateFolderService(name)

        selectedFilesManager.startNewSelection()
    }

    override fun onRename() {
        if (!connectionManager.isConnected) {
            view?.showNoConnectionSnackBar()
            return
        }

        view?.setAppNameTitle()
        view?.toggleContextMenu(false)

        disableAllMultiSelect()

        val size = selectedFilesManager.currentSelectedFiles.size
        if (size == 0) {
            view?.showNoSelectionSnackBar()
            return
        } else if (size > 1) {
            view?.showTooManySelectedSnackBar()
            return
        }

        sourceManager.addFileAction(
            selectedFilesManager.operationCount,
            FileAction.RENAME
        )
        selectedFilesManager.addActionableDirectory(
            selectedFilesManager.operationCount,
            sourceManager.activeSource.currentDirectory
        )

        val currentName = selectedFilesManager
            .currentSelectedFiles[0].name

        view?.showRenameDialog(currentName)
    }

    override fun rename(name: String, newName: String) {
        val activeDirectory = sourceManager.activeSource.currentDirectory

        val nameToSet = if (name.lastIndexOf('.') > 0) {
            newName + name.substring(name.lastIndexOf('.'))
        } else {
            newName
        }

        for (file in activeDirectory.children) {
            if (file.data.name.equals(nameToSet, false)) {
                view?.showFileExistsSnackBar()
                return
            }
        }

        view?.startRenameService(nameToSet)

        selectedFilesManager.startNewSelection()
    }

    override fun onCreateZip() {
        if (!connectionManager.isConnected) {
            view?.showNoConnectionSnackBar()
            return
        }
        if (!sourceManager.activeSource.isLoggedIn) {
            view?.showNotLoggedInSnackBar()
            return
        } else if (!sourceManager.activeSource.isFilesLoaded) {
            view?.showNotLoadedSnackBar()
            return
        }

        view?.setAppNameTitle()
        view?.toggleContextMenu(false)

        disableAllMultiSelect()

        sourceManager.addFileAction(
            selectedFilesManager.operationCount,
            FileAction.NEW_ZIP
        )

        selectedFilesManager.addActionableDirectory(
            selectedFilesManager.operationCount,
            sourceManager.activeSource.currentDirectory
        )

        view?.showCreateZipDialog()
    }

    override fun zip(name: String) {
        val activeDirectory = selectedFilesManager
            .getActionableDirectory(selectedFilesManager.operationCount)

        for (file in activeDirectory?.children ?: emptyList<TreeNode<SourceFile>>()) {
            if (file.data.name == name) {
                view?.showFileExistsSnackBar()
                return
            }
        }

        view?.startZipService(name)
        selectedFilesManager.startNewSelection()
    }

    override fun onShowProperties() {
        val selectedCount = selectedFilesManager.currentSelectedFiles.size
        if (selectedCount == 0) {
            view?.showNoSelectionSnackBar()
            return
        }
        val selectedSize = selectedFilesManager.currentSelectionSize

        view?.showPropertiesDialog(selectedCount, selectedSize.toInt())
    }

    override fun onShowProgress() {
        TODO("Not yet implemented")
    }

    override fun onShowUsage() {
        val loadedSources = sourceManager.sources.filter {
            it.isFilesLoaded
        }

        view?.showUsageDialog(loadedSources)
    }

    override fun onLogout() {
        val loggedInSources = sourceManager.sources.filter {
            it.isFilesLoaded && it.sourceConnectionType == SourceConnectionType.REMOTE
        }
        view?.showLogoutDialog(loggedInSources)
    }

    override fun onSortBy() {
        view?.showSortByDialog()
    }

    override fun onShowSettings() {
        view?.showSettingsDialog()
    }

    override fun onPrepareContextMenu() {
        if (sourceManager.getFileAction(selectedFilesManager.operationCount) == null) {
            view?.hidePasteMenuItem()
        }

        if (selectedFilesManager.currentSelectedFiles.size > 1) {
            view?.hideRenameMenuItem()
        }
    }

    override fun onSearch(query: String) {
        val allResults = mutableListOf<TreeNode<SourceFile>>()
        val sourceIds = mutableListOf<String>()

        for (source in sourceManager.sources) {
            val results = TreeNode.searchForChildren(source.rootNode, query)
            if (results.isNotEmpty()) {
                allResults.addAll(results)
                sourceIds.add(SourceType.values()[source.sourceId].sourceName)
            }
        }

        allResults.sortWith(Comparator { node1, node2 ->
            node1.data.name.compareTo(node2.data.name, true)
        })

        view?.showSearchDialog(allResults, sourceIds)
    }

    override fun onNavigateToFile(fileNode: TreeNode<SourceFile>) {
        for (i in sourceManager.sources.indices) {
            if (sourceManager.sources[i].sourceId != fileNode.data.sourceId) {
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