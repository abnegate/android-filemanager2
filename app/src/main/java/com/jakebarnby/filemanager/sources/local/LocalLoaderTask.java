package com.jakebarnby.filemanager.sources.local;

import android.os.Environment;

import com.jakebarnby.filemanager.sources.LoaderTask;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;

/**
 * Created by Jake on 8/2/2017.
 */

public class LocalLoaderTask extends LoaderTask {

    public LocalLoaderTask(Source source, SourceListener listener) {
        super(source, listener);
    }

    @Override
    public Object initRootNode(String path) {
        File rootFile = new File(path);
        SourceFile rootSourceFile = new LocalFile(rootFile);
        mRootTreeNode = new TreeNode<>(rootSourceFile);
        mCurrentNode = mRootTreeNode;
        mSource.setCurrentDirectory(mRootTreeNode);
        mSource.setQuotaInfo(Utils.getStorageStats(Environment.getExternalStorageDirectory()));
        return rootFile;
    }

    @Override
    public TreeNode<SourceFile> readFileTree(Object rootObject) {
        if (rootObject != null && rootObject instanceof File) {
            File node = (File)rootObject;
            File listFile[] = node.listFiles();
            long dirSize = 0L;
            if (listFile != null && listFile.length > 0) {
                for (File file : listFile) {
                    SourceFile sourceFile = new LocalFile(file);
                    if (file.isDirectory()) {
                        mCurrentNode.addChild(sourceFile);
                        mCurrentNode = mCurrentNode.getChildren().get(mCurrentNode.getChildren().size() - 1);
                        readFileTree(file);
                        mCurrentNode.getParent().getData().addSize(mCurrentNode.getData().getSize());
                        mCurrentNode = mCurrentNode.getParent();
                    } else {
                        dirSize += file.length();
                        mCurrentNode.addChild(sourceFile);
                    }
                }
                mCurrentNode.getData().addSize(dirSize);
            }
        }
        return mRootTreeNode;
    }
}