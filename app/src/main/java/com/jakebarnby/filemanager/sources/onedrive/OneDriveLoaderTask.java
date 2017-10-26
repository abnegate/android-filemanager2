package com.jakebarnby.filemanager.sources.onedrive;

import com.jakebarnby.filemanager.sources.LoaderTask;
import com.jakebarnby.filemanager.sources.SourceListener;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.http.GraphServiceException;

import java.util.List;

/**
 * Created by Jake on 8/2/2017.
 */

public class OneDriveLoaderTask extends LoaderTask {

    private DriveItem mRootDriveItem;

    public OneDriveLoaderTask(Source source, SourceListener listener, DriveItem rootDriveItem) {
        super(source, listener);
        this.mRootDriveItem = rootDriveItem;
    }

    @Override
    protected Object initRootNode(String path) {
        SourceFile rootSourceFile = new OneDriveFile(mRootDriveItem);
        rootSourceFile.setDirectory(true);
        mRootTreeNode = new TreeNode<>(rootSourceFile);
        mCurrentNode = mRootTreeNode;
        mSource.setCurrentDirectory(mRootTreeNode);
        mSource.setQuotaInfo(OneDriveFactory.getInstance().getStorageStats());
        return mRootDriveItem;
    }

    @Override
    protected TreeNode<SourceFile> readFileTree(Object rootObject) {
        try {
            if (rootObject != null && rootObject instanceof DriveItem) {
                DriveItem node = (DriveItem) rootObject;
                IDriveItemCollectionPage items = OneDriveFactory
                        .getInstance()
                        .getGraphClient()
                        .getMe()
                        .getDrive()
                        .getItems(node.id)
                        .getChildren()
                        .buildRequest()
                        .select("id,name,webUrl,folder,size,createdDateTime,lastModifiedDateTime")
                        .expand("thumbnails")
                        .get();

                List<DriveItem> pageItems = items.getCurrentPage();
                long dirSize = 0L;
                if (pageItems != null) {
                    for (DriveItem file : pageItems) {
                        SourceFile sourceFile = new OneDriveFile(file);
                        if (file.folder != null) {
                            mCurrentNode.addChild(sourceFile);
                            mCurrentNode = mCurrentNode.getChildren().get(mCurrentNode.getChildren().size() - 1);
                            readFileTree(file);
                            mCurrentNode.getParent().getData().addSize(mCurrentNode.getData().getSize());
                            mCurrentNode = mCurrentNode.getParent();
                        } else {
                            dirSize += file.size;
                            mCurrentNode.addChild(sourceFile);
                        }
                    }
                    mCurrentNode.getData().addSize(dirSize);
                }
            }
        } catch (GraphServiceException e) {
            mListener.onLoadError(e.getMessage() != null ? e.getMessage() : "");
            mSucess = false;
        }
        return mRootTreeNode;
    }
}
