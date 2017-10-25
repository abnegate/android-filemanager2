package com.jakebarnby.filemanager.managers;

import android.util.SparseArray;

import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 6/6/2017.
 */

public class SelectedFilesManager {

    private static volatile  SelectedFilesManager sInstance;

    private SparseArray<List<TreeNode<SourceFile>>> mSelectedFilesMap;
    private SparseArray<TreeNode<SourceFile>> mActionableDirectories;

    private SelectedFilesManager() {
        mSelectedFilesMap = new SparseArray<>();
        mActionableDirectories = new SparseArray<>();
    }

    public static SelectedFilesManager getInstance() {
        if (sInstance == null) {
            sInstance = new SelectedFilesManager();
        }
        return sInstance;
    }

    public List<TreeNode<SourceFile>> getSelectedFiles(int operationId) {
        return mSelectedFilesMap.get(operationId-1);
    }

    public List<TreeNode<SourceFile>> getCurrentSelectedFiles() {
        return mSelectedFilesMap.get(getOperationCount()-1);
    }

    public TreeNode<SourceFile> getActionableDirectory(int operationId) {
        return mActionableDirectories.get(operationId);
    }

    public void addNewSelection() {
        mSelectedFilesMap.put(getOperationCount(), new ArrayList<>());
    }

    public void addActionableDirectory(int operationId, TreeNode<SourceFile> actionableDir) {
        mActionableDirectories.put(operationId, actionableDir);
    }

    public int getOperationCount() {
        return mSelectedFilesMap.size();
    }

    public long getCurrentCopySize() {
        long copySize = 0;
        for(TreeNode<SourceFile> file: SelectedFilesManager.getInstance().getCurrentSelectedFiles()) {
            copySize+=file.getData().getSize();
        }
        return copySize;
    }
}
