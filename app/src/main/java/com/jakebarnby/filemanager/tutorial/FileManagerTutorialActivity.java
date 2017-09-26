package com.jakebarnby.filemanager.tutorial;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;

/**
 * Created by Jake on 9/26/2017.
 */

public class FileManagerTutorialActivity extends TutorialActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPages();
    }

    /**
     * Add all child tutorial pages to the pager adapter.
     */
    private void addPages() {
        addFragment(new TutorialPage.Builder()
                .setTitle("Welcome to File Manager")
                .setContent("The easiest way to manage your files across devices, Dropbox, Google Drive and OneDrive.")
                .setDrawable(R.drawable.ic_onedrive)
                .setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
                .setSummary("Continue to see app features")
                .build()
        );
        addFragment(new TutorialPage.Builder()
                .setTitle("The Ultimate File Manager")
                .setContent("Copy, move, rename files and folders on any of the supported sources.")
                .setDrawable(R.drawable.ic_dropbox)
                .setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
                .setSummary("Continue to the last page")
                .build()
        );
        addFragment(new TutorialPage.Builder()
                .setTitle("Have fun!")
                .setContent("Hope you enjoy this heaps good app it took me ages.")
                .setDrawable(R.drawable.ic_googledrive)
                .setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
                .setSummary("Click finish to begin using the app")
                .build()
        );
    }

    @Override
    public void finish() {
        super.finish();
        startActivity(new Intent(this, SourceActivity.class));
    }
}
