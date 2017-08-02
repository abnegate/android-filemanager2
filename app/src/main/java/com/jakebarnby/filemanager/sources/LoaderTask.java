package com.jakebarnby.filemanager.sources;

import android.os.AsyncTask;

import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 8/2/2017.
 */

public abstract class LoaderTask extends AsyncTask<String, Void, TreeNode<SourceFile>> {

    protected TreeNode<SourceFile>    mRootTreeNode;
    protected TreeNode<SourceFile>    mCurrentNode;

    protected Source                  mSource;
    protected SourceListener          mListener;

    public abstract Object initRootNode(String path);
    public abstract TreeNode<SourceFile> readFileTree(Object rootObject);

    public LoaderTask(Source source, SourceListener listener) {
        this.mSource = source;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onLoadStarted();
    }

    @Override
    protected TreeNode<SourceFile> doInBackground(String... paths) {
        Object root = initRootNode(paths[0]);
        return readFileTree(root);
    }

    @Override
    protected void onPostExecute(TreeNode<SourceFile> fileTree) {
        super.onPostExecute(fileTree);
        TreeNode.sortTree(fileTree, (node1, node2) -> {
            int result = Boolean.valueOf(!node1.getData().isDirectory()).compareTo(!node2.getData().isDirectory());
            if (result == 0) {
                result = node1.getData().getName().toLowerCase().compareTo(node2.getData().getName().toLowerCase());
            }
            return result;
        });
        mSource.setFilesLoaded(true);
        mListener.onLoadComplete(fileTree);
    }
}
