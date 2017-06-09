package com.jakebarnby.filemanager.services;

import com.jakebarnby.filemanager.models.SourceFile;

/**
 * Created by Jake on 6/6/2017.
 */

public class GoogleDriveTransferService extends SourceTransferService {

    public GoogleDriveTransferService(String name) {
        super(name);
    }

    @Override
    protected void copy(SourceFile source, SourceFile sourceDir, SourceFile destDir) {

    }

    @Override
    protected void move(SourceFile source, SourceFile sourceDir, SourceFile destDir) {

    }

    @Override
    protected void delete(SourceFile source) {

    }

    @Override
    protected void rename(SourceFile source) {

    }
}
