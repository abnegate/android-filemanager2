package com.jakebarnby.filemanager.ui.adapters;

import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.jakebarnby.filemanager.sources.SourceFragment;
import com.jakebarnby.filemanager.sources.dropbox.DropboxFragment;
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFragment;
import com.jakebarnby.filemanager.sources.local.LocalFragment;
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFragment;
import com.jakebarnby.filemanager.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SourcePagerAdapter extends FragmentStatePagerAdapter {

    private List<SourceFragment> fragments = new ArrayList<>();

    public SourcePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(LocalFragment.newInstance(Constants.Sources.LOCAL, Environment.getExternalStorageDirectory().getPath()));
        fragments.add(DropboxFragment.newInstance(Constants.Sources.DROPBOX));
        fragments.add(GoogleDriveFragment.newInstance(Constants.Sources.GOOGLE_DRIVE));
        fragments.add(OneDriveFragment.newInstance(Constants.Sources.ONEDRIVE));
    }

    public List<SourceFragment> getFragments() {
        return fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
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
