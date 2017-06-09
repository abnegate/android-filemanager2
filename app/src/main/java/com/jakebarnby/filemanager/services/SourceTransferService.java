package com.jakebarnby.filemanager.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.jakebarnby.filemanager.models.SourceFile;

import java.io.Serializable;

/**
 * Created by Jake on 6/6/2017.
 */

public abstract class SourceTransferService extends IntentService {
    private static final String ACTION_COPY = "com.jakebarnby.filemanager.services.action.COPY";
    private static final String ACTION_MOVE = "com.jakebarnby.filemanager.services.action.MOVE";
    private static final String ACTION_DELETE = "com.jakebarnby.filemanager.services.action.DELETE";
    private static final String ACTION_RENAME = "com.jakebarnby.filemanager.services.action.RENAME";

    private static final String EXTRA_SOURCE_FILE = "com.jakebarnby.filemanager.services.extra.PARAM1";
    private static final String EXTRA_SOURCE_DIR = "com.jakebarnby.filemanager.services.extra.PARAM2";
    private static final String EXTRA_SOURCE_DEST = "com.jakebarnby.filemanager.services.extra.PARAM2";

    public SourceTransferService(String name) {
        super(name);
    }

    protected abstract void copy(SourceFile source, SourceFile sourceDir, SourceFile destDir);
    protected abstract void move(SourceFile source, SourceFile sourceDir, SourceFile destDir);
    protected abstract void delete(SourceFile source);
    protected abstract void rename(SourceFile source);

    /**
     *
     * @param context
     * @param sourceFile
     * @param sourceDir
     * @param sourceDest
     */
    public static void startActionCopy(Context context, SourceFile sourceFile, SourceFile sourceDir, SourceFile sourceDest) {
        Intent intent = new Intent(context, LocalTransferService.class);
        intent.setAction(ACTION_COPY);
        intent.putExtra(EXTRA_SOURCE_FILE, (Serializable) sourceFile);
        intent.putExtra(EXTRA_SOURCE_DIR, (Serializable) sourceDir);
        intent.putExtra(EXTRA_SOURCE_DEST, (Serializable) sourceDest);
        context.startService(intent);
    }

    /**
     *
     * @param context
     * @param sourceFile
     * @param sourceDir
     * @param sourceDest
     */
    public static void startActionMove(Context context, SourceFile sourceFile, SourceFile sourceDir, SourceFile sourceDest) {
        Intent intent = new Intent(context, LocalTransferService.class);
        intent.setAction(ACTION_MOVE);
        intent.putExtra(EXTRA_SOURCE_FILE, (Serializable) sourceFile);
        intent.putExtra(EXTRA_SOURCE_DIR, (Serializable) sourceDir);
        intent.putExtra(EXTRA_SOURCE_DEST, (Serializable) sourceDest);
        context.startService(intent);
    }

    /**
     *
     * @param context
     * @param sourceFile
     */
    public static void startActionDelete(Context context, SourceFile sourceFile) {
        Intent intent = new Intent(context, LocalTransferService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_SOURCE_FILE, (Serializable) sourceFile);
        context.startService(intent);
    }

    /**
     *
     * @param context
     * @param sourceFile
     */
    public static void startActionRename(Context context, SourceFile sourceFile) {
        Intent intent = new Intent(context, LocalTransferService.class);
        intent.setAction(ACTION_RENAME);
        intent.putExtra(EXTRA_SOURCE_FILE, (Serializable) sourceFile);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            final SourceFile source = (SourceFile) intent.getSerializableExtra(EXTRA_SOURCE_FILE);
            final SourceFile sourceDir = (SourceFile) intent.getSerializableExtra(EXTRA_SOURCE_DIR);
            final SourceFile sourceDest = (SourceFile) intent.getSerializableExtra(EXTRA_SOURCE_DEST);

            if (ACTION_COPY.equals(action)) {
                copy(source, sourceDir, sourceDest);
            } else if (ACTION_MOVE.equals(action)) {
                move(source, sourceDir, sourceDest);
            } else if (ACTION_DELETE.equals(action)) {
                delete(source);
            } else if (ACTION_RENAME.equals((action))) {
                rename(source);
            }
        }
    }
}
