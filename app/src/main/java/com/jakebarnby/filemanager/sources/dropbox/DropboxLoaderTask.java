package com.jakebarnby.filemanager.sources.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.jakebarnby.filemanager.sources.LoaderTask;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

/**
 * Created by Jake on 8/2/2017.
 */

public class DropboxLoaderTask extends LoaderTask {

    public DropboxLoaderTask(Source source, SourceListener listener) {
        super(source, listener);
    }

    @Override
    protected Object initRootNode(String path) {
        SourceFile rootSourceFile = new DropboxFile(new Metadata(path));
        rootSourceFile.setDirectory(true);
        mRootTreeNode = new TreeNode<>(rootSourceFile);
        mCurrentNode = mRootTreeNode;
        mSource.setCurrentDirectory(mRootTreeNode);
        mSource.setQuotaInfo(DropboxFactory.getInstance().getStorageStats());

        ListFolderResult result = null;
        try {
            result = DropboxFactory
                    .getInstance()
                    .getClient()
                    .files()
                    .listFolderBuilder(path)
                    .start();
        } catch (DbxException e) {
            mListener.onLoadError(e.getMessage() != null ? e.getMessage() : "");
            mSucess = false;
        }
        return result;
    }

    @Override
    protected TreeNode<SourceFile> readFileTree(Object rootObject) {
        if (rootObject != null && rootObject instanceof ListFolderResult) {
            ListFolderResult node = (ListFolderResult)rootObject;

            long dirSize = 0L;
            for (Metadata data : node.getEntries()) {
                SourceFile sourceFile = new DropboxFile(data);
                try {
                    if (!sourceFile.isDirectory()) {
                        sourceFile.setThumbnailLink(DropboxFactory
                                .getInstance()
                                .getClient()
                                .files()
                                .getTemporaryLink(sourceFile.getPath()).getLink());
                    }
                } catch (DbxException e) {
                    mListener.onLoadError(e.getMessage() != null ? e.getMessage() : "");
                    mSucess = false;
                }
                if (data instanceof FolderMetadata) {
                    mCurrentNode.addChild(sourceFile);
                    mCurrentNode = mCurrentNode.getChildren().get(mCurrentNode.getChildren().size() - 1);
                    try {
                        readFileTree(DropboxFactory
                                .getInstance()
                                .getClient()
                                .files()
                                .listFolder(data.getPathLower()));
                    } catch (DbxException e) {
                        mListener.onLoadError(e.getMessage() != null ? e.getMessage() : "");
                    }
                    mCurrentNode.getParent().getData().addSize(mCurrentNode.getData().getSize());
                    mCurrentNode = mCurrentNode.getParent();
                } else {
                    dirSize += ((FileMetadata)data).getSize();
                    mCurrentNode.addChild(sourceFile);
                }
            }
            mCurrentNode.getData().addSize(dirSize);
        }
        return mRootTreeNode;
    }
}
