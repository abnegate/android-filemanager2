package com.jakebarnby.filemanager.managers;

import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 6/6/2017.
 */

public class SelectedFilesManager {

    private static volatile  SelectedFilesManager sInstance;
    private List<TreeNode<SourceFile>> mSelectedFiles = new ArrayList<>();

    public static SelectedFilesManager getInstance() {
        if (sInstance == null) {
            sInstance = new SelectedFilesManager();
        }
        return sInstance;
    }

    public List<TreeNode<SourceFile>> getSelectedFiles() {
        return mSelectedFiles;
    }
}
