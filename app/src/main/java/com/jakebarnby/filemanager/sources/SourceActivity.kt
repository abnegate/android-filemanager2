package com.jakebarnby.filemanager.sources

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.*
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.BillingManager
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.services.SourceTransferService
import com.jakebarnby.filemanager.sources.local.LocalFragment
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.models.SourceManager
import com.jakebarnby.filemanager.sources.models.SourceType
import com.jakebarnby.filemanager.ui.adapters.*
import com.jakebarnby.filemanager.ui.adapters.SourceLogoutAdapter.LogoutListener
import com.jakebarnby.filemanager.ui.dialogs.*
import com.jakebarnby.filemanager.util.*
import com.jakebarnby.filemanager.util.Constants.ADS_MENU_ID
import com.jakebarnby.filemanager.util.Constants.ADS_MENU_POSITION
import com.jakebarnby.filemanager.util.Constants.ALL
import com.jakebarnby.filemanager.util.Constants.DIALOG_TITLE_KEY
import com.jakebarnby.filemanager.util.Constants.FILE_PATH_KEY
import io.github.yavski.fabspeeddial.FabSpeedDial
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class SourceActivity : AppCompatActivity(), OnPageChangeListener, SearchView.OnQueryTextListener, CoroutineScope {

    var job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    var sourceManager: SourceManager? = null
        private set

    private lateinit var sourcesPagerAdapter: SourcePagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var progressDialog: ProgressDialog
    private lateinit var contextMenu: FabSpeedDial
    private lateinit var blurWrapper: ViewGroup
    private lateinit var billingManager: BillingManager
    private lateinit var interstitialAd: InterstitialAd

    var isCheckingPermissions = false

    /**
     * Gets the fragment currently visible to the user
     * @return  The fragment currently visible to the user
     */
    val activeFragment: SourceFragment?
        get() = sourcesPagerAdapter.fragments[viewPager.currentItem]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        interstitialAd.loadAd(AdRequest.Builder().build())
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        billingManager = BillingManager(this)
        sourceManager = SourceManager()
        sourcesPagerAdapter = SourcePagerAdapter(supportFragmentManager)

//        GlobalScope.launch {
//            withContext(Main) {
//                addLocalSources()
//            }
//        }


        viewPager = findViewById(R.id.view_pager)
        blurWrapper = findViewById(R.id.wrapper)
        viewPager.adapter = sourcesPagerAdapter
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = sourcesPagerAdapter.count - 1
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleIntent(intent)
            }
        }
        contextMenu = findViewById(R.id.fab_speed_dial)
        contextMenu.setMenuListener(object : SimpleMenuListenerAdapter() {

            override fun onPrepareMenu(navigationMenu: NavigationMenu): Boolean {
                prepareContextMenu(navigationMenu)
                return super.onPrepareMenu(navigationMenu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                Blurry.delete(blurWrapper)
                handleFabMenuItemSelected(menuItem)
                return true
            }

            override fun onMenuClosed() {
                Blurry.delete(blurWrapper)
                super.onMenuClosed()
            }
        })
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        SourceTransferService.startClearLocalCache(this)
    }

    @SuppressLint("RestrictedApi")
    private fun prepareContextMenu(navigationMenu: NavigationMenu) {
        Blurry.with(this@SourceActivity)
            .radius(17)
            .sampling(1)
            .async()
            .onto(blurWrapper)

        if (sourceManager?.getFileAction(SelectedFilesManager.operationCount) == null) {
            navigationMenu.findItem(R.id.action_paste).isVisible = false
        }

        if (SelectedFilesManager.currentSelectedFiles.size > 1) {
            navigationMenu.findItem(R.id.action_rename).isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()

        val filterLocal = IntentFilter().apply {
            addAction(IntentExtensions.ACTION_SHOW_DIALOG)
            addAction(IntentExtensions.ACTION_UPDATE_DIALOG)
            addAction(IntentExtensions.ACTION_SHOW_ERROR)
            addAction(IntentExtensions.ACTION_COMPLETE)
        }
        val filterSystem = IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_REMOVED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            addDataScheme("file")
        }

        applicationContext.registerReceiver(broadcastReceiver, filterLocal)
        applicationContext.registerReceiver(broadcastReceiver, filterSystem)
    }

    override fun onPause() {
        applicationContext.unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_source, menu)

        if (!PreferenceUtils.getBoolean(this, Constants.Prefs.HIDE_ADS_KEY, false)) {
            menu.add(Menu.NONE, ADS_MENU_ID, ADS_MENU_POSITION, R.string.action_remove_ads)
        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_view_as -> showViewAsDialog()
            R.id.action_sort_by -> showSortByDialog()
            R.id.action_new_folder -> showCreateFolderDialog()
            R.id.action_multi_select -> handleMenuMultiSelect()
            R.id.action_storage_usage -> showUsageDialog()
            R.id.action_logout -> showLogoutDialog()
            R.id.action_settings -> showSettingsDialog()

            ADS_MENU_ID -> {
                launch {
                    billingManager.purchaseItem(this@SourceActivity, Constants.Billing.SKU_PREMIUM)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Add a local source with the given root path
     * @param path  Root path of the local source
     */
    private fun addLocalSource(path: String) {
        var indexToInsert = 0
        for (fragment in sourcesPagerAdapter.fragments) {
            if (fragment.source.sourceType == SourceType.LOCAL) {
                indexToInsert++
            }
        }
        val newSourceName = if (indexToInsert == 1) {
            getString(R.string.sdcard)
        } else {
            getString(R.string.usb) + (indexToInsert - 1).toString()
        }

        sourcesPagerAdapter.fragments.add(
            indexToInsert,
            LocalFragment.newInstance(newSourceName, path)
        )

        sourcesPagerAdapter.notifyDataSetChanged()
    }

    /**
     * Check all external storage sources and add any that are not already added
     */
    private fun addLocalSources() {
        val paths = Utils.getExternalStorageDirectories(applicationContext)
        if (paths.isEmpty()) {
            return
        }
        for (i in paths.indices) {
            val split = paths[i]?.split("/")?.toTypedArray()
            val rootDirTitle = split?.get(split.size - 1) ?: "$i"
            var alreadyAdded = false
            for (fragment in sourcesPagerAdapter.fragments) {
                if (fragment.source.rootNode.data.path.contains(rootDirTitle)) {
                    alreadyAdded = true
                    break
                }
            }
            if (alreadyAdded) {
                continue
            }
            sourcesPagerAdapter.fragments.add(
                i + 1,
                LocalFragment.newInstance(if (i == 0) getString(R.string.sdcard) else getString(R.string.usb) + i, paths[i]!!)
            )
        }
        sourcesPagerAdapter.notifyDataSetChanged()
    }

    /**
     * Remove a local source fragment
     * @param sourcePath    The root path of the source
     */
    private fun removeLocalSource(sourcePath: String) {
        for (i in sourcesPagerAdapter.fragments.indices) {
            val source = sourcesPagerAdapter.fragments[i].source
            if (source.sourceType == SourceType.LOCAL &&
                source.isFilesLoaded &&
                sourcePath.contains(source.rootNode.data.path)) {
                sourcesPagerAdapter.fragments.removeAt(i)
            }
        }
        sourcesPagerAdapter.notifyDataSetChanged()
    }

    /**
     * Enabled multiselect if it is not already enabled and add a new selection
     */
    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }

    private fun handleMenuMultiSelect() {
        if (activeFragment?.source?.isMultiSelectEnabled != true) {
            activeFragment!!.setMultiSelectEnabled(true)
            if (SelectedFilesManager.operationCount == 0) {
                SelectedFilesManager.startNewSelection()
            }
        }
    }

    /**
     * Handles menu clicks from the fab action menu
     * @param menuItem The selected item
     */
    private fun handleFabMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_rename -> showRenameDialog()
            R.id.action_copy -> startCopyAction()
            R.id.action_cut -> startCutAction()
            R.id.action_paste -> doPasteChecks()
            R.id.action_zip -> showCreateZipDialog()
            R.id.action_properties -> showPropertiesDialog()
            R.id.action_delete -> startDeleteAction()
        }
    }

    /**
     * Called when a broadcasted intent is recieved
     * @param intent The broadcasted intent
     */
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                IntentExtensions.ACTION_COMPLETE -> {
                    completeServiceAction(intent)
                }
                IntentExtensions.ACTION_SHOW_DIALOG -> {
                    showProgressDialog(intent)
                }
                IntentExtensions.ACTION_SHOW_ERROR -> {
                    showErrorDialog(intent.getStringExtra(IntentExtensions.EXTRA_DIALOG_MESSAGE))
                    updateProgressDialog(intent)
                }
                IntentExtensions.ACTION_UPDATE_DIALOG -> {
                    updateProgressDialog(intent)
                }
                Intent.ACTION_MEDIA_MOUNTED -> {
                    addLocalSource(intent.dataString!!.replace("file://", ""))
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED,
                Intent.ACTION_MEDIA_BAD_REMOVAL,
                Intent.ACTION_MEDIA_REMOVED -> {
                    removeLocalSource(intent.dataString!!)
                }
                Intent.ACTION_SEARCH -> {
                    doSearch(intent.getStringExtra(SearchManager.QUERY) ?: "")
                }
            }
        }
    }

    /**
     * Search all files and folders for the given query string
     * @param query     Name of the file or folder to find
     */
    private fun doSearch(query: String) {
        val allResults: MutableList<TreeNode<SourceFile>> = ArrayList()
        val sourceNames: MutableList<String?> = ArrayList()
        for (fragment in sourcesPagerAdapter.fragments) {
            val results = TreeNode.searchForChildren(fragment.source.rootNode, query)
            if (results.isNotEmpty()) {
                allResults.addAll(results)
                sourceNames.add(fragment.source.sourceName)
            }
        }

        allResults.sortWith(Comparator { t1, t2 ->
            t1.data.name.compareTo(t2.data.name, true)
        })

        val searchDialog = AlertDialog.Builder(this)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()

        val adapter = SearchResultAdapter(allResults, object : OnSearchResultClicked {
            override fun navigateToFile(toOpen: TreeNode<SourceFile>) {
                val fragments = sourcesPagerAdapter.fragments

                for (i in sourcesPagerAdapter.fragments.indices) {
                    if (fragments[i].source.sourceName != toOpen.data.sourceName) {
                        continue
                    }

                    if (searchDialog.isShowing) {
                        searchDialog.dismiss()
                    }

                    viewPager.setCurrentItem(i, true)

                    val newDir = if (toOpen.data.isDirectory) {
                        toOpen
                    } else {
                        toOpen.parent
                    }

                    if (newDir == null) {
                        //TODO Log error
                        return
                    }

                    activeFragment?.source?.currentDirectory = newDir

                    (activeFragment!!.recycler!!.adapter as FileAdapter?)
                        ?.setCurrentDirectory(newDir, this@SourceActivity)

                    sourceManager?.activeDirectory = newDir
                    activeFragment?.popAllBreadCrumbs()
                    activeFragment?.pushAllBreadCrumbs(newDir)
                    activeFragment?.recycler?.adapter?.notifyDataSetChanged()
                    activeFragment?.recycler?.requestFocus()
                }
            }
        })

        val view = layoutInflater.inflate(R.layout.dialog_search_results, null)
        val rv: RecyclerView = view.findViewById(R.id.rv_search_results)

        if (allResults.isEmpty()) {
            searchDialog.setMessage(getString(R.string.no_results))
        } else {
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.adapter = adapter
            searchDialog.setTitle(R.string.search_results)
            searchDialog.setView(view)
        }

        val arrayadapter = ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item)
        arrayadapter.add(ALL)
        arrayadapter.addAll(sourceNames)
        arrayadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spn = view.findViewById<Spinner>(R.id.spn_sources)
        spn.adapter = arrayadapter
        spn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val selected = adapterView.getItemAtPosition(i) as String
                if (selected == ALL) {
                    adapter.resetDataset()
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.removeAllSourceExcept(selected)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        searchDialog.show()
    }

    /**
     * Check if there are enough files selected and start a cut action
     * otherwise throw a snackbar with error message
     */
    private fun startCutAction() {
        if (SelectedFilesManager.currentSelectedFiles.size > 0) {
            sourceManager?.addFileAction(SelectedFilesManager.operationCount, FileAction.CUT)
            showSnackbar(getString(R.string.cut))
        } else {
            showSnackbar(getString(R.string.err_no_selection))
        }
        disableAllMultiSelect()
    }

    /**
     * Check if there are enough files selected and start a copy action
     * otherwise throw a snackbar with error message
     */
    private fun startCopyAction() {
        if (SelectedFilesManager.currentSelectedFiles.size > 0) {
            sourceManager?.addFileAction(SelectedFilesManager.operationCount, FileAction.COPY)
            showSnackbar(getString(R.string.copied))
        } else {
            showSnackbar(getString(R.string.err_no_selection))
        }
        disableAllMultiSelect()
    }

    /**
     * Check if there are enough files selected then show a confirmation dialog and start a delete action
     * or do nothing otherwise throw a snackbar with error message
     */
    private fun startDeleteAction() {
        if (activeFragment?.source?.checkConnectionActive(this) != true) {
            return
        }

        val size = SelectedFilesManager.currentSelectedFiles
            .size

        if (size < 0) {
            showSnackbar(getString(R.string.err_no_selection))
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.warning)
            .setMessage(String.format(Locale.getDefault(), getString(R.string.dialog_delete_confirm), size))
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                val activeDir = sourceManager?.activeDirectory ?: return@setPositiveButton

                sourceManager?.addFileAction(SelectedFilesManager.operationCount, FileAction.DELETE)
                SelectedFilesManager.addActionableDirectory(
                    SelectedFilesManager.operationCount,
                    activeDir
                )

                disableAllMultiSelect()
                toggleFloatingMenu(false)
                title = getString(R.string.app_name)

                SourceTransferService.startActionDelete(this@SourceActivity)

                activeFragment?.source
                    ?.increaseFreeSpace(SelectedFilesManager.currentCopySize)

                SelectedFilesManager.startNewSelection()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    /**
     * Check if the active source is connected, logged in, loaded and has enough free space.
     * If so, start a paste action via [SourceTransferService];
     * Otherwise show a snackbar with the error message
     */
    private fun doPasteChecks() {
        if (activeFragment?.source?.checkConnectionActive(this) != true) {
            return
        }
        if (activeFragment?.source?.sourceType == SourceType.LOCAL &&
            activeFragment?.source?.sourceName != Constants.Sources.LOCAL) {
            showSnackbar(getString(R.string.err_no_ext_write))
            return
        }
        if (activeFragment?.source?.isLoggedIn != true) {
            showSnackbar(getString(R.string.err_not_logged_in))
            return
        } else if (activeFragment?.source?.isFilesLoaded != true) {
            showSnackbar(getString(R.string.err_not_loaded))
            return
        }

        val copySize = SelectedFilesManager.currentCopySize
        if (copySize > activeFragment?.source?.freeSpace ?: 0) {
            showSnackbar(
                String.format(
                    getString(R.string.err_no_free_space),
                    activeFragment?.source?.sourceName)
            )
            return
        }

        startPasteAction(copySize)
    }

    /**
     * Start a paste action via [SourceTransferService]
     */
    private fun startPasteAction(copySize: Long) {
        if (sourceManager?.getFileAction(SelectedFilesManager.operationCount) != null) {
            activeFragment?.setMultiSelectEnabled(false)
            title = getString(R.string.app_name)

            toggleFloatingMenu(false)

            val activeDir = sourceManager?.activeDirectory ?: return
            SelectedFilesManager.addActionableDirectory(
                SelectedFilesManager.operationCount,
                activeDir
            )

            val curAction = sourceManager?.getFileAction(SelectedFilesManager.operationCount)
            if (curAction == FileAction.COPY) {
                SourceTransferService.startActionCopy(this@SourceActivity, false)
            } else if (curAction == FileAction.CUT) {
                SourceTransferService.startActionCopy(this@SourceActivity, true)
            }

            SelectedFilesManager.startNewSelection()
            activeFragment?.source?.decreaseFreeSpace(copySize)
        }
    }

    /**
     * Called when [SourceTransferService] broadcasts that it has completed a background action
     */
    private fun completeServiceAction(intent: Intent) {
        val operationId = intent.getIntExtra(IntentExtensions.EXTRA_OPERATION_ID, 0)
        val path = intent.getStringExtra(FILE_PATH_KEY)

        if (path == null) {
            // TODO: Log error
            return
        }

        when (sourceManager?.getFileAction(operationId)) {
            FileAction.CUT,
            FileAction.COPY,
            FileAction.DELETE,
            FileAction.RENAME,
            FileAction.NEW_FOLDER,
            FileAction.NEW_ZIP -> {
                completeTreeModification(operationId)
            }
            FileAction.OPEN -> {
                startOpenFile(path)
            }
        }

        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }

        var operationCount = PreferenceUtils.getInt(
            this,
            Constants.Prefs.OPERATION_COUNT_KEY,
            0
        ) + 1

        PreferenceUtils.savePref(
            this,
            Constants.Prefs.OPERATION_COUNT_KEY,
            operationCount
        )

        val removeAds = PreferenceUtils.getBoolean(
            this,
            Constants.Prefs.HIDE_ADS_KEY,
            false
        )

        if (operationCount == Constants.Ads.SHOW_AD_COUNT && !removeAds) {
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
                PreferenceUtils.savePref(this, Constants.Prefs.OPERATION_COUNT_KEY, 0)
            }
        }
    }

    /**
     * Complete a service action that modified the file tree
     * @param operationId   Id of the completed operation
     */
    private fun completeTreeModification(operationId: Int) {
        val sortRoot = SelectedFilesManager.getActionableDirectory(operationId) ?: return
        TreeNode.sortTree(
            sortRoot,
            ComparatorUtils.resolveComparatorForPrefs(this)
        )

        for (fragment in sourcesPagerAdapter.fragments) {
            fragment.refreshRecycler()
        }
    }

    /**
     * Show a snackbar with the given message.
     * @param message   The message to display in the snackbar.
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(viewPager, message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Shows a dialog allowing a user to choose a grid or list layout for displaying files.
     */
    private fun showViewAsDialog() {
        ViewAsDialog().show(supportFragmentManager, "ViewAs")
    }

    /**
     * Shows a dialog asking for a new folder name which creates the folder on completion
     */
    private fun showCreateFolderDialog() {
        if (activeFragment?.source?.checkConnectionActive(this) != true) {
            return
        }
        if (activeFragment?.source?.isLoggedIn != true) {
            showSnackbar(getString(R.string.err_not_logged_in))
            return
        } else if (activeFragment?.source?.isFilesLoaded != true) {
            showSnackbar(getString(R.string.err_not_loaded))
            return
        }

        val activeDir = sourceManager?.activeDirectory ?: return

        sourceManager?.addFileAction(
            SelectedFilesManager.operationCount,
            FileAction.NEW_FOLDER
        )
        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            activeDir
        )

        CreateFolderDialog.newInstance()
            .show(supportFragmentManager, Constants.DialogTags.CREATE_FOLDER)
    }

    /**
     * Shows a dialog allowing the user to rename a file or folder
     */
    private fun showRenameDialog() {
        if (activeFragment?.source?.checkConnectionActive(this) != true) {
            return
        }
        val activeDir = sourceManager?.activeDirectory ?: return

        title = getString(R.string.app_name)

        toggleFloatingMenu(false)

        for (fragment in sourcesPagerAdapter.fragments) {
            fragment.setMultiSelectEnabled(false)
        }

        sourceManager?.addFileAction(
            SelectedFilesManager.operationCount,
            FileAction.RENAME)

        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            activeDir
        )

        val size = SelectedFilesManager.currentSelectedFiles.size
        if (size == 0) {
            showSnackbar(getString(R.string.err_no_selection))
            return
        } else if (size > 1) {
            showSnackbar(getString(R.string.err_too_many_selected))
            return
        }

        RenameDialog
            .newInstance()
            .show(supportFragmentManager, getString(R.string.rename))
    }

    private fun showCreateZipDialog() {
        if (activeFragment?.source?.checkConnectionActive(this) != true) {
            return
        }
        if (activeFragment?.source?.isLoggedIn != true) {
            showSnackbar(getString(R.string.err_not_logged_in))
            return
        } else if (activeFragment?.source?.isFilesLoaded != true) {
            showSnackbar(getString(R.string.err_not_loaded))
            return
        }
        val activeDir = sourceManager?.activeDirectory ?: return

        title = getString(R.string.app_name)

        toggleFloatingMenu(false)

        disableAllMultiSelect()

        sourceManager!!.addFileAction(
            SelectedFilesManager.operationCount,
            FileAction.NEW_ZIP
        )

        SelectedFilesManager.addActionableDirectory(
            SelectedFilesManager.operationCount,
            activeDir
        )

        CreateZipDialog
            .newInstance()
            .show(supportFragmentManager, getString(R.string.create_zip))
    }

    /**
     * Shows a dialog displaying properties about the selected files and/or folders
     */
    private fun showPropertiesDialog() {
        val size: Int = SelectedFilesManager.currentSelectedFiles.size
        if (size == 0) {
            showSnackbar(getString(R.string.err_no_selection))
        } else {
            PropertiesDialog
                .newInstance()
                .show(supportFragmentManager, "Properties")
        }
    }

    /**
     * Show a progress dialog
     *
     * @param intent The broadcasted intent with dialog extras
     */
    private fun showProgressDialog(intent: Intent) {
        var title = intent.getStringExtra(IntentExtensions.EXTRA_DIALOG_TITLE)
        if (title == null) {
            title = "Operation in progress.."
        }

        val totalCount = intent.getIntExtra(IntentExtensions.EXTRA_DIALOG_MAX_VALUE, 0)
        val currentCount = 0

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(title)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.isIndeterminate = true
        if (totalCount != 0) {
            progressDialog.max = totalCount
            progressDialog.progress = currentCount
        } else {
            progressDialog.setProgressNumberFormat(null)
            progressDialog.setProgressPercentFormat(null)
        }
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { _, _ ->
            stopService(Intent(this, SourceTransferService::class.java))
        }
        progressDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.background)
        ) { dialog, _ -> dialog.dismiss() }

        progressDialog.show()
    }

    private fun showUsageDialog() {
        val sources: MutableList<Source?> = ArrayList()

        for (fragment in sourcesPagerAdapter.fragments) {
            if (fragment.source.isFilesLoaded) {
                sources.add(fragment.source)
            }
        }

        val adapter = SourceUsageAdapter(sources)
        val view = layoutInflater.inflate(R.layout.dialog_source_usage, null)
        val rv: RecyclerView = view.findViewById(R.id.rv_source_usage)

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
        val builder = AlertDialog.Builder(this)

        builder
            .setTitle(R.string.dialog_title_usage)
            .setNegativeButton(R.string.close) { dialog: DialogInterface, _ -> dialog.dismiss() }
            .setView(view)
            .create()
            .show()
    }

    private fun showLogoutDialog() {
        val sources: MutableList<Source?> = ArrayList()
        for (fragment in sourcesPagerAdapter.fragments) {
            if (fragment.source.isFilesLoaded &&
                fragment.source.sourceType == SourceType.REMOTE) {
                sources.add(fragment.source)
            }
        }

        val logoutDialog = AlertDialog
            .Builder(this)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()

        val adapter = SourceLogoutAdapter(sources, object : LogoutListener {
            override fun onLastLogout() {
                logoutDialog.dismiss()
            }
        })

        val view = layoutInflater.inflate(R.layout.dialog_source_logout, null)
        val rv: RecyclerView = view.findViewById(R.id.rv_source_logout)

        if (sources.isEmpty()) {
            logoutDialog.setMessage(getString(R.string.no_connected_sources))
        } else {
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv.adapter = adapter
            logoutDialog.setView(view)
            logoutDialog.setTitle(R.string.dialog_title_logout)
        }
        logoutDialog.show()
    }

    private fun showSortByDialog() {
        val dialog = SortByDialog()
        dialog.show(supportFragmentManager, Constants.DialogTags.SORT_BY)
    }

    /**
     * Show the settings dialog
     */
    private fun showSettingsDialog() {
        val dialog = SettingsDialog()
        dialog.show(supportFragmentManager, Constants.DialogTags.SETTINGS)
    }

    fun showErrorDialog(message: String?) {

        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_error)
            .setMessage(message)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    /**
     * Update the progress of the [ProgressDialog] if it is showing
     *
     * @param intent The broadcasted intent with update extras
     */
    private fun updateProgressDialog(intent: Intent) {
        if (progressDialog.isShowing) {
            if (progressDialog.isIndeterminate) {
                progressDialog.isIndeterminate = false
            }
            val currentCount = intent.getIntExtra(IntentExtensions.EXTRA_DIALOG_CURRENT_VALUE, 0)
            progressDialog.progress = currentCount
        }
    }

    /**
     * Open a file at the given path. If the open operation was finished in the background,
     * inform user file is ready to open with a dialog. Otherwise open the file immediately.
     *
     * If a user selects 'No' and chooses not the open the file, it will still be cached when
     * attempting to open again (until next app launch.)
     *
     * @param filePath  The absolute path of the file to open
     */
    private fun startOpenFile(filePath: String) {
        if (!progressDialog.isShowing) {
            val filename = filePath.substring(
                filePath.lastIndexOf(File.separator) + 1,
                filePath.length)
            val builder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.file_ready))
                .setMessage(String.format(getString(R.string.open_now), filename))
                .setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewFileInExternalApp(filePath)
                }
                .create().show()
        } else {
            viewFileInExternalApp(filePath)
        }
    }

    /**
     * Attempts to open a file by finding it's mimetype then opening a compatible application
     * @param filePath  The absolute path of the file to open
     */
    private fun viewFileInExternalApp(filePath: String?) {
        try {
            if (filePath == null) {
                return
            }

            val file = File(filePath)
            val extension = Utils.fileExt(filePath)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            val reachableUri = FileProvider.getUriForFile(this, applicationInfo.packageName, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(reachableUri, mimeType)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            if (resolveInfo.size > 0) {
                startActivity(intent)
            } else {
                showSnackbar(getString(R.string.err_no_app_available))
            }
        } catch (e: Exception) {
            showErrorDialog(String.format(
                "%s %s %s",
                getString(R.string.problem_encountered),
                getString(R.string.opening_file),
                ": " + e.localizedMessage)
            )
        }
    }

    /**
     * Toggles the floating action context menu
     * @param enabled   Whether the menu should enabled or not
     */
    fun toggleFloatingMenu(enabled: Boolean) {
        if ((!enabled && contextMenu.visibility == View.INVISIBLE) ||
            (enabled && contextMenu.visibility == View.VISIBLE)) {
            return
        }

        if (enabled) {
            contextMenu.visibility = View.VISIBLE
        }

        val screenHeight = Utils.getScreenHeight(contextMenu.context).toFloat()
        val translate = TranslateAnimation(
            0.0f,
            0.0f,
            if (enabled) screenHeight else 0.0f,
            if (enabled) 0.0f else screenHeight
        )

        if (enabled) {
            translate.interpolator = OvershootInterpolator(0.55f)
        } else {
            translate.interpolator = AccelerateInterpolator(2.0f)
        }

        translate.duration = 400
        translate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (!enabled) contextMenu.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        contextMenu.startAnimation(translate)
    }

    private fun disableAllMultiSelect() {
        for (fragment in sourcesPagerAdapter.fragments) {
            fragment.setMultiSelectEnabled(false)
        }
    }

    /**
     * Initializes recyclerviews for all logged in fragments due to a view layout change (Grid <-> List)
     */
    fun initAllRecyclers() {
        for (fragment in sourcesPagerAdapter.fragments) {
            if (fragment.source.isFilesLoaded) {
                fragment.initRecyclerView()
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        sourceManager?.activeDirectory = activeFragment?.source?.currentDirectory ?: return
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onBackPressed() {
        val activeDir = sourceManager?.activeDirectory ?: return
        if (activeFragment != null && activeFragment?.source != null)
            if (activeFragment?.source?.isMultiSelectEnabled == true) {
                activeFragment!!.setMultiSelectEnabled(false)
                toggleFloatingMenu(false)
                setTitle(R.string.app_name)
                SelectedFilesManager.currentSelectedFiles.clear()
            } else if (
                activeFragment?.source?.isLoggedIn == true &&
                activeDir.parent != null
            ) {
                val previousPosition = activeDir.parent?.data?.positionToRestore ?: 0

                (activeFragment?.recycler?.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(previousPosition, 0)

                activeFragment?.source?.currentDirectory = activeDir.parent!!

                (activeFragment!!.recycler!!.adapter as? FileAdapter)
                    ?.setCurrentDirectory(activeDir.parent!!, this)

                sourceManager?.activeDirectory = activeDir.parent!!

                activeFragment?.refreshRecycler()
                activeFragment?.popBreadcrumb()
            } else {
                super.onBackPressed()
            }
    }

}