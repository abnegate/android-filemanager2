package com.jakebarnby.filemanager.services;

import android.app.IntentService;

import com.jakebarnby.filemanager.models.SourceFile;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class LocalTransferService extends SourceTransferService {

    public LocalTransferService(String name) {
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
