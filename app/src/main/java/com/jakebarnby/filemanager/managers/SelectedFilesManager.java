package com.jakebarnby.filemanager.managers;

import com.jakebarnby.filemanager.models.files.SourceFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 6/6/2017.
 */

public class SelectedFilesManager {

    private static volatile  SelectedFilesManager sInstance;
    private List<SourceFile> mSelectedFiles = new ArrayList<>();

    public static SelectedFilesManager getInstance() {
        if (sInstance == null) {
            sInstance = new SelectedFilesManager();
        }
        return sInstance;
    }

    public List<SourceFile> getSelectedFiles() {
        return mSelectedFiles;
    }
}
