package com.jakebarnby.filemanager.activities.source.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.activities.source.dropbox.DropboxFragment;
import com.jakebarnby.filemanager.activities.source.googledrive.GoogleDriveFragment;
import com.jakebarnby.filemanager.activities.source.local.LocalFragment;
import com.jakebarnby.filemanager.activities.source.onedrive.OneDriveFragment;
import com.jakebarnby.filemanager.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SourcesPagerAdapter extends FragmentPagerAdapter {

    private List<SourceFragment> fragments = new ArrayList<>();

    public SourcesPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(LocalFragment.newInstance(Constants.Sources.LOCAL));
        fragments.add(DropboxFragment.newInstance(Constants.Sources.DROPBOX));
        fragments.add(GoogleDriveFragment.newInstance(Constants.Sources.GOOGLE_DRIVE));
        fragments.add(OneDriveFragment.newInstance(Constants.Sources.ONEDRIVE));
    }

    public List<SourceFragment> getFragments() {
        return fragments;
    }

    public void addFragment(SourceFragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getArguments().getString("TITLE");
    }
}
