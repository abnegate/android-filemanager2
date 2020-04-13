package com.jakebarnby.filemanager.workers

import android.os.AsyncTask
import com.jakebarnby.filemanager.models.Source
import com.jakebarnby.filemanager.models.SourceFile
import com.jakebarnby.filemanager.ui.sources.SourceFragmentContract
import com.jakebarnby.filemanager.util.TreeNode

/**
 * Created by Jake on 8/2/2017.
 */
abstract class LoaderTask(
    protected var source: Source,
    protected var presenter: SourceFragmentContract.Presenter
) : AsyncTask<String, Void?, TreeNode<SourceFile>>() {

    protected lateinit var rootTreeNode: TreeNode<SourceFile>
    protected lateinit var currentNode: TreeNode<SourceFile>
    protected var success = true

    protected abstract fun initRootNode(path: String): Any?

    protected abstract fun readFileTree(rootObject: Any?): TreeNode<SourceFile>

    override fun onPreExecute() {
        super.onPreExecute()
        presenter.onLoadStarted()
    }

    override fun doInBackground(vararg params: String): TreeNode<SourceFile>? {
        val root = initRootNode(params[0])
        return readFileTree(root)
    }

    override fun onPostExecute(fileTree: TreeNode<SourceFile>) {
        super.onPostExecute(fileTree)
        if (success) {
            source.rootNode = fileTree
            source.isFilesLoaded = true
            presenter.onLoadComplete(fileTree)
        }
    }
}