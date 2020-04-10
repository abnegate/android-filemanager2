package com.jakebarnby.filemanager.sources

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.managers.SelectedFilesManager
import com.jakebarnby.filemanager.models.FileAction
import com.jakebarnby.filemanager.services.SourceTransferService
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFragment
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveSource
import com.jakebarnby.filemanager.sources.local.LocalFragment
import com.jakebarnby.filemanager.sources.models.Source
import com.jakebarnby.filemanager.sources.models.SourceFile
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFragment
import com.jakebarnby.filemanager.sources.onedrive.OneDriveSource
import com.jakebarnby.filemanager.ui.adapters.FileAdapter
import com.jakebarnby.filemanager.ui.adapters.FileAdapter.OnFileClickedListener
import com.jakebarnby.filemanager.ui.adapters.FileAdapter.OnFileLongClickedListener
import com.jakebarnby.filemanager.ui.adapters.FileDetailedListAdapter
import com.jakebarnby.filemanager.ui.adapters.FileGridAdapter
import com.jakebarnby.filemanager.ui.adapters.FileListAdapter
import com.jakebarnby.filemanager.util.*
import com.jakebarnby.filemanager.util.Constants.GRID_SIZE
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
abstract class SourceFragment : Fragment(), SourceListener {

    lateinit var source: Source

    var recycler: RecyclerView? = null

    protected lateinit var fileListAdapter: FileListAdapter
    protected lateinit var fileGridAdapter: FileGridAdapter
    protected lateinit var fileDetailedListAdapter: FileDetailedListAdapter
    protected lateinit var progressBar: ProgressBar
    protected lateinit var connectButton: Button
    protected lateinit var sourceLogo: ImageView
    protected lateinit var divider: View

    private lateinit var breadcrumbBar: LinearLayout
    private lateinit var breadcrumbWrapper: HorizontalScrollView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_source, container, false)

        breadcrumbWrapper = rootView.findViewById(R.id.breadcrumb_wrapper)
        breadcrumbBar = rootView.findViewById(R.id.breadcrumbs)
        recycler = rootView.findViewById(R.id.recycler_local)
        progressBar = rootView.findViewById(R.id.animation_view)
        divider = rootView.findViewById(R.id.divider_sort)
        connectButton = rootView.findViewById(R.id.btn_connect)
        sourceLogo = rootView.findViewById(R.id.img_source_logo)

        if (source.hasToken(context!!, source.sourceName)) {
            connectButton.visibility = View.GONE
        }

        connectButton.setOnClickListener {
            if (source.checkConnectionActive(context!!)) {
                if (this is OneDriveFragment) {
                    (source as OneDriveSource?)!!.authenticateSource(this)
                } else {
                    source.authenticateSource(context!!)
                }
            }
        }
        sourceLogo.setImageResource(Utils.resolveLogoId(source.sourceName))
        return rootView
    }

    override fun onLoadStarted() {
        connectButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onLoadAborted() {
        connectButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun onLoadError(errorMessage: String?) {
        activity!!.runOnUiThread {
            onLoadAborted()
            (activity as SourceActivity?)!!.showErrorDialog(String.format(
                "%s %s%s",
                getString(R.string.problem_encountered),
                getString(R.string.loading_source),
                " " + source.sourceName + " files."))
        }
    }

    override fun onLoadComplete(rootFile: TreeNode<SourceFile>) {
        if (isResumed) {
            pushBreadcrumb(rootFile)

            initAdapters(
                rootFile,
                createOnClickListener(),
                createOnLongClickListener()
            )

            val sourceManager = (activity as? SourceActivity)?.sourceManager

            if (sourceManager?.activeDirectory == null) {
                sourceManager?.activeDirectory = rootFile
            }
            progressBar.visibility = View.GONE
            sourceLogo.visibility = View.GONE
        }
    }

    override fun onLogout() {
        recycler?.visibility = View.GONE
        divider.visibility = View.GONE
        breadcrumbWrapper.visibility = View.GONE
        sourceLogo.visibility = View.VISIBLE
        connectButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        popAllBreadCrumbs()
    }

    override fun onNoConnection() {
        onLoadAborted()
        Snackbar.make(recycler!!, R.string.err_no_connection, Snackbar.LENGTH_LONG).show()
    }

    fun setMultiSelectEnabled(enabled: Boolean) {
        if (enabled) {
            (activity as? SourceActivity)?.toggleFloatingMenu(true)
        }
        source.isMultiSelectEnabled = enabled

        if (recycler?.adapter != null) {
            (recycler!!.adapter as? FileAdapter)?.setMultiSelectEnabled(enabled)
            recycler!!.adapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Set the [RecyclerView] layout and adapter based on users preferences
     */
    fun initRecyclerView() {
        val viewType = PreferenceUtils.getInt(
            context!!,
            Constants.Prefs.VIEW_TYPE_KEY,
            Constants.ViewTypes.LIST)

        TreeNode.sortTree(
            source.rootNode,
            ComparatorUtils.resolveComparatorForPrefs(context!!)
        )

        val newAdapter: FileAdapter = when (viewType) {
            Constants.ViewTypes.LIST -> {
                recycler?.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                fileListAdapter
            }
            Constants.ViewTypes.DETAILED_LIST -> {
                recycler?.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                fileDetailedListAdapter
            }
            Constants.ViewTypes.GRID -> {
                recycler?.layoutManager = GridLayoutManager(
                    context,
                    GRID_SIZE
                )
                fileGridAdapter
            }
            else -> {
                recycler?.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                fileListAdapter
            }
        }
        newAdapter.setCurrentDirectory(source.currentDirectory, context!!)

        recycler?.adapter = newAdapter
        recycler?.adapter?.notifyDataSetChanged()
        recycler?.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE
        breadcrumbWrapper.visibility = View.VISIBLE
    }

    /**
     * Initialize the UI for this mSource
     * @param file                  The root file of the mSource
     * @param onClickListener       The click listener for files and folders
     * @param onLongClickListener   The long click listener for files and folders
     */
    protected fun initAdapters(
        file: TreeNode<SourceFile>,
        onClickListener: OnFileClickedListener,
        onLongClickListener: OnFileLongClickedListener
    ) {
        fileListAdapter = FileListAdapter(file, context!!)
        fileListAdapter.setOnClickListener(onClickListener)
        fileListAdapter.setOnLongClickListener(onLongClickListener)

        fileGridAdapter = FileGridAdapter(file, context!!)
        fileGridAdapter.setOnClickListener(onClickListener)
        fileGridAdapter.setOnLongClickListener(onLongClickListener)

        fileDetailedListAdapter = FileDetailedListAdapter(file, context!!)
        fileDetailedListAdapter.setOnClickListener(onClickListener)
        fileDetailedListAdapter.setOnLongClickListener(onLongClickListener)
        initRecyclerView()
    }

    /**
     * Reload the [RecyclerView]
     */
    fun refreshRecycler() {
        recycler!!.adapter!!.notifyDataSetChanged()
    }

    /**
     * Construct a click listener for a file or folder
     * @return  The constructed listener
     */
    protected fun createOnClickListener(): OnFileClickedListener {
        return object : OnFileClickedListener {
            override fun onClick(file: TreeNode<SourceFile>, isChecked: Boolean, position: Int) {
                if (source.isMultiSelectEnabled) {
                    if (isChecked) {
                        SelectedFilesManager.currentSelectedFiles.add(file)
                    } else {
                        SelectedFilesManager.currentSelectedFiles.remove(file)
                    }

                    activity?.title = String.format(
                        Locale.getDefault(),
                        getString(R.string.selected_title),
                        SelectedFilesManager.currentSelectedFiles.size
                    )
                } else {
                    if (file.data.isDirectory) {
                        source.currentDirectory.data.positionToRestore = (recycler!!.layoutManager as LinearLayoutManager?)
                            ?.findFirstVisibleItemPosition() ?: 0

                        (recycler?.adapter as? FileAdapter)
                            ?.setCurrentDirectory(file, context!!)

                        source.currentDirectory = file
                        (activity as? SourceActivity)
                            ?.sourceManager
                            ?.activeDirectory = file

                        pushBreadcrumb(file)

                        recycler?.adapter?.notifyDataSetChanged()
                    } else {
                        if (Utils.getStorageStats(Environment.getExternalStorageDirectory()).freeSpace > file.data.size) {
                            SelectedFilesManager.startNewSelection()
                            (activity as? SourceActivity)?.sourceManager?.addFileAction(
                                SelectedFilesManager.operationCount - 1,
                                FileAction.OPEN)
                            SourceTransferService.startActionOpen(context!!, file.data)
                        } else {
                            Snackbar.make(
                                recycler!!,
                                String.format(getString(R.string.err_no_free_space), file.data.sourceName),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Construct a long click listener for a file or folder
     * @return  The constructed listener
     */
    protected fun createOnLongClickListener(): OnFileLongClickedListener {
        return object : OnFileLongClickedListener {
            override fun onLongClick(file: TreeNode<SourceFile>) {
                if (!source.isMultiSelectEnabled) {
                    setMultiSelectEnabled(true)
                    if (SelectedFilesManager.operationCount == 0) {
                        SelectedFilesManager.startNewSelection()
                    }
                    SelectedFilesManager
                        .getSelectedFiles(SelectedFilesManager.operationCount)
                        ?.add(file)

                    val size = SelectedFilesManager
                        .getSelectedFiles(SelectedFilesManager.operationCount)
                        ?.size

                    activity?.title = "${size ?: 0} selected"
                }
            }
        }
    }

    /**
     * Push a breadcrumb onto the view stack
     * @param directory The directory to push
     */
    protected fun pushBreadcrumb(directory: TreeNode<SourceFile>) {

        val crumbLayout = activity
            ?.layoutInflater
            ?.inflate(R.layout.view_breadcrumb, null) as ViewGroup

        crumbLayout.findViewById<View>(R.id.crumb_arrow).visibility = if (directory.parent == null) {
            View.GONE
        } else {
            View.VISIBLE
        }

        val text = crumbLayout.findViewById<TextView>(R.id.crumb_text)

        // If this is the root node set the text to the source name instead of file name
        text.text = if (directory.parent == null) {
            directory.data.sourceName
        } else {
            directory.data.name
        }

        crumbLayout.setOnClickListener { v: View ->
            val crumbText = v.findViewById<TextView>(R.id.crumb_text)
            val diff = breadcrumbBar.childCount - 1 - breadcrumbBar.indexOfChild(v)

            for (i in 0 until diff) {
                popBreadcrumb()
            }

            val name = crumbText.text.toString()

            if (source.currentDirectory.data.name == name) {
                return@setOnClickListener
            }

            val selectedParent = TreeNode
                .searchForParent(source.currentDirectory, name) ?: return@setOnClickListener

            val previousPosition = selectedParent.data.positionToRestore ?: 0

            (recycler?.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(previousPosition, 0)

            (activity as? SourceActivity)?.sourceManager?.activeDirectory = selectedParent
            (recycler?.adapter as? FileAdapter)?.setCurrentDirectory(selectedParent, context!!)
            source.currentDirectory = selectedParent
            recycler!!.adapter!!.notifyDataSetChanged()
        }

        breadcrumbWrapper.postDelayed(
            { breadcrumbWrapper.fullScroll(HorizontalScrollView.FOCUS_RIGHT) },
            50L
        )

        breadcrumbBar.addView(crumbLayout)

        crumbLayout.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.breadcrumb_overshoot_z)
        )
    }

    fun pushAllBreadCrumbs(directory: TreeNode<SourceFile>) {
        val breadCrumbs = Stack<TreeNode<SourceFile>>()
        var root = directory
        breadCrumbs.push(root)
        while (root.parent != null) {
            root = root.parent!!
            breadCrumbs.push(root)
        }
        while (breadCrumbs.size > 0) {
            pushBreadcrumb(breadCrumbs.pop())
        }
    }

    /**
     * Pop the latest breakcrumb added to the stack
     */
    fun popBreadcrumb() {
        breadcrumbBar.removeViewAt(breadcrumbBar.childCount - 1)
    }

    /**
     * Pop all breadcrumbs in the stack
     */
    fun popAllBreadCrumbs() {
        breadcrumbBar.removeAllViews()
    }

    override fun onCheckPermissions(permission: String, requestCode: Int) {
        val activity = activity as? SourceActivity

        val permissionCheck = ContextCompat.checkSelfPermission(activity!!, permission)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (!activity.isCheckingPermissions) {
                activity.isCheckingPermissions = true
                requestPermissions(arrayOf(permission), requestCode)
            }
        } else {
            if (this is LocalFragment) {
                source.isLoggedIn = true
                source.loadSource(activity)
            } else if (this is GoogleDriveFragment) {
                (source as GoogleDriveSource).authGoogle(this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.RequestCodes.STORAGE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    source.loadSource(context!!)
                    source.isLoggedIn = true
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(recycler!!, R.string.storage_permission, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_settings) { v: View? -> showAppDetails() }
                        .show()
                }
            }
            Constants.RequestCodes.ACCOUNTS_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (this is GoogleDriveFragment) {
                        (source as GoogleDriveSource).authGoogle(this)
                    }
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                    Snackbar.make(recycler!!, R.string.contacts_permission, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_settings) { v: View? -> showAppDetails() }
                        .show()
                }
            }
        }
        (activity as? SourceActivity)?.isCheckingPermissions = false
    }

    /**
     * Opens the app details page for this app
     */
    protected fun showAppDetails() {
        val uri = Uri.fromParts("package", "com.jakebarnby.filemanager", null)
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = uri
        }
        startActivity(intent)
    }
}