package com.jakebarnby.filemanager.ui.sources

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakebarnby.filemanager.R
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.models.ViewType
import com.jakebarnby.filemanager.services.SourceTransferService
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFragment
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveSource
import com.jakebarnby.filemanager.sources.local.LocalFragment
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFragment
import com.jakebarnby.filemanager.sources.onedrive.OneDriveSource
import com.jakebarnby.filemanager.ui.adapters.FileAdapter
import com.jakebarnby.filemanager.ui.adapters.FileAdapter.OnFileLongClickedListener
import com.jakebarnby.filemanager.ui.adapters.FileDetailedListAdapter
import com.jakebarnby.filemanager.ui.adapters.FileGridAdapter
import com.jakebarnby.filemanager.ui.adapters.FileListAdapter
import com.jakebarnby.filemanager.util.*
import com.jakebarnby.filemanager.util.Constants.GRID_SIZE
import com.jakebarnby.filemanager.util.Constants.Prefs
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject

abstract class SourceFragment : DaggerFragment(), SourceFragmentContract.View {

    @Inject
    protected lateinit var presenter: SourceFragmentContract.Presenter

    var recycler: RecyclerView? = null

    private val adapters = mutableMapOf<ViewType, FileAdapter>()

    private lateinit var progressBar: ProgressBar
    private lateinit var connectButton: Button
    private lateinit var sourceLogo: ImageView
    private lateinit var divider: View

    private lateinit var breadcrumbBar: LinearLayout
    private lateinit var breadcrumbWrapper: HorizontalScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.subscribe(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.checkState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_source, container, false)

        breadcrumbWrapper = rootView.findViewById(R.id.breadcrumb_wrapper)
        breadcrumbBar = rootView.findViewById(R.id.breadcrumbs)
        recycler = rootView.findViewById(R.id.recycler_local)
        progressBar = rootView.findViewById(R.id.animation_view)
        divider = rootView.findViewById(R.id.divider_sort)
        connectButton = rootView.findViewById(R.id.btn_connect)
        sourceLogo = rootView.findViewById(R.id.img_source_logo)

        connectButton.setOnClickListener {
            presenter.onConnect()
        }

        sourceLogo.setImageResource(
            Utils.resolveLogoId(presenter.source.sourceId)
        )
        return rootView
    }

    override fun onResume() {
        super.onResume()
        presenter.subscribe(this)
    }

    override fun onPause() {
        presenter.unsubscribe()
        super.onPause()
    }

    override fun authenticate() {
        if (this is OneDriveFragment) {
            (presenter.source as? OneDriveSource)?.authenticateSource(this)
        } else {
            presenter.source.authenticate(context!!)
        }
    }

    override fun startActionOpen(toOpen: SourceFile) {
        SourceTransferService.startActionOpen(context!!, toOpen)
    }

    override fun setSelectedCountTitle(size: Int) {
        activity?.title = String.format(
            Locale.getDefault(),
            getString(R.string.selected_title),
            size
        )
    }

    override fun updateFileList() {
        recycler?.adapter?.notifyDataSetChanged()
    }

    override fun showLoadError(sourceName: Int) {
        val message = String.format(
            "%s %s %s",
            getString(R.string.problem_encountered),
            getString(R.string.loading_source),
            "$sourceName files."
        )
        AlertDialog.Builder(context!!)
            .setTitle(R.string.dialog_error)
            .setMessage(message)
            .setNegativeButton(R.string.close) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    override fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    override fun showConnectButton() {
        connectButton.visibility = View.VISIBLE
    }

    override fun hideConnectButton() {
        connectButton.visibility = View.GONE
    }

    override fun showSourceLogo() {
        sourceLogo.visibility = View.VISIBLE
    }

    override fun hideSourceLogo() {
        sourceLogo.visibility = View.GONE
    }

    override fun showFileList() {
        TODO("Not yet implemented")
    }

    override fun hideFileList() {
        recycler?.visibility = View.GONE
        divider.visibility = View.GONE
        breadcrumbWrapper.visibility = View.GONE
        sourceLogo.visibility = View.VISIBLE
        connectButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun showNoConnectionSnackBar() {
        Snackbar.make(recycler!!, R.string.err_no_connection, Snackbar.LENGTH_LONG).show()
    }

    override fun showNotEnoughSpaceSnackBar() {
        Snackbar.make(recycler!!, R.string.err_no_free_space, Snackbar.LENGTH_LONG).show()
    }

    fun initRecyclerView() {
        val viewType = ViewType.getFromValue(presenter.prefsManager.getInt(
            Prefs.VIEW_TYPE_KEY,
            ViewType.LIST.value
        ))

        TreeNode.sortTree(
            presenter.source.rootNode,
            Comparators.resolveComparatorForPrefs(presenter.prefsManager)
        )

        val newAdapter = when (viewType) {
            ViewType.LIST -> {
                recycler?.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapters[ViewType.LIST]
            }
            ViewType.DETAILED_LIST -> {
                recycler?.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapters[ViewType.DETAILED_LIST]
            }
            ViewType.GRID -> {
                recycler?.layoutManager = GridLayoutManager(
                    context,
                    GRID_SIZE
                )
                adapters[ViewType.GRID]
            }
            else -> {
                recycler?.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapters[ViewType.LIST]
            }
        }
        recycler?.adapter = newAdapter
        recycler?.adapter?.notifyDataSetChanged()
        recycler?.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE
        breadcrumbWrapper.visibility = View.VISIBLE
    }

    override fun populateList() {
        val clickListener = object : FileAdapter.OnFileClickedListener {
            override fun onClick(file: TreeNode<SourceFile>, isChecked: Boolean, position: Int) {
                presenter.onFileSelected(file, isChecked, position)
            }
        }
        val longClickListener = object : OnFileLongClickedListener {
            override fun onLongClick(file: TreeNode<SourceFile>) {
                presenter.onFileLongSelected(file)
            }
        }
        val listAdapter = FileListAdapter(
            presenter.source,
            presenter.selectedFilesManager,
            presenter.prefsManager
        ).apply {
            setOnClickListener(clickListener)
            setOnLongClickListener(longClickListener)
        }
        val gridAdapter = FileGridAdapter(
            presenter.source,
            presenter.selectedFilesManager,
            presenter.prefsManager
        ).apply {
            setOnClickListener(clickListener)
            setOnLongClickListener(longClickListener)
        }
        val detailedListAdapter = FileDetailedListAdapter(
            presenter.source,
            presenter.selectedFilesManager,
            presenter.prefsManager
        ).apply {
            setOnClickListener(clickListener)
            setOnLongClickListener(longClickListener)
        }

        adapters[ViewType.LIST] = listAdapter
        adapters[ViewType.DETAILED_LIST] = detailedListAdapter
        adapters[ViewType.GRID] = gridAdapter

        initRecyclerView()
    }

    /**
     * Push a breadcrumb onto the view stack
     * @param directory The directory to push
     */
    override fun pushBreadCrumb(
        directory: TreeNode<SourceFile>,
        arrowVisible: Boolean,
        name: String
    ) {
        val crumbLayout = activity
            ?.layoutInflater
            ?.inflate(R.layout.view_breadcrumb, breadcrumbWrapper, false)
            as? ViewGroup
            ?: return

        crumbLayout.findViewById<View>(R.id.crumb_arrow).visibility = if (arrowVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }

        crumbLayout.findViewById<TextView>(R.id.crumb_text)
            ?.text = name

        crumbLayout.setOnClickListener { v: View ->
            val crumbsToPop = breadcrumbBar.childCount - 1 - breadcrumbBar.indexOfChild(v)
            val selectedDirName = v.findViewById<TextView>(R.id.crumb_text).text.toString()

            presenter.onBreadCrumbSelected(selectedDirName, crumbsToPop)
        }

        breadcrumbWrapper.afterLayout {
            breadcrumbWrapper.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
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
            val node = breadCrumbs.pop()
            pushBreadCrumb(
                node,
                node.parent != null,
                if (node.parent == null) {
                    SourceType.values()[node.data.sourceId].sourceName
                } else {
                    node.data.name
                }
            )
        }
    }

    override fun popBreadCrumb() {
        breadcrumbBar.removeViewAt(breadcrumbBar.childCount - 1)
    }

    override fun popAllBreadCrumbs() {
        breadcrumbBar.removeAllViews()
    }

    override fun onCheckPermissions(name: String, requestCode: Int) {
        val activity = activity as? SourceActivity ?: return

        val permissionCheck = ContextCompat.checkSelfPermission(context!!, name)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (!activity.isCheckingPermissions) {
                activity.isCheckingPermissions = true
                requestPermissions(arrayOf(name), requestCode)
            }
        } else {
            if (this is LocalFragment) {
                presenter.source.isLoggedIn = true
                presenter.source.loadFiles(activity)
            } else if (this is GoogleDriveFragment) {
                (presenter.source as GoogleDriveSource)
                    .authGoogle(this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.RequestCodes.STORAGE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.source.loadFiles(context!!)
                    presenter.source.isLoggedIn = true
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(recycler!!, R.string.storage_permission, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_settings) { showAppSettings() }
                        .show()
                }
            }
            Constants.RequestCodes.ACCOUNTS_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (this is GoogleDriveFragment) {
                        (presenter.source as GoogleDriveSource).authGoogle(this)
                    }
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                    Snackbar.make(recycler!!, R.string.contacts_permission, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_settings) { showAppSettings() }
                        .show()
                }
            }
        }
        (activity as? SourceActivity)?.isCheckingPermissions = false
    }

    override fun showAppSettings() {
        val uri = Uri.fromParts("package", "com.jakebarnby.filemanager", null)
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = uri
        }
        startActivity(intent)
    }
}