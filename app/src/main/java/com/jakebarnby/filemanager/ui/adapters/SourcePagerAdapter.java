package com.jakebarnby.filemanager.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jakebarnby.filemanager.sources.SourceFragment;
import com.jakebarnby.filemanager.sources.dropbox.DropboxFragment;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFragment;
import com.jakebarnby.filemanager.sources.local.LocalFragment;
import com.jakebarnby.filemanager.sources.models.Source;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFragment;
import com.jakebarnby.filemanager.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SourcePagerAdapter extends FragmentPagerAdapter {

    private List<SourceFragment> fragments = new ArrayList<>();

    public SourcePagerAdapter(FragmentManager fm, Map<String, Source> sources) {
        super(fm);
        fragments.add(LocalFragment.newInstance(sources.get(Constants.Sources.LOCAL)));
        fragments.add(DropboxFragment.newInstance(sources.get(Constants.Sources.DROPBOX)));
        fragments.add(GoogleDriveFragment.newInstance(sources.get(Constants.Sources.GOOGLE_DRIVE)));
        fragments.add(OneDriveFragment.newInstance(sources.get(Constants.Sources.ONEDRIVE)));
    }

    public List<SourceFragment> getFragments() {
        return fragments;
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
