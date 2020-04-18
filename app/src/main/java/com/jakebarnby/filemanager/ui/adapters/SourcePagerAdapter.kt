package com.jakebarnby.filemanager.ui.adapters

import android.os.Environment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.DropboxFragment
import com.jakebarnby.filemanager.ui.sources.GoogleDriveFragment
import com.jakebarnby.filemanager.models.sources.local.LocalFragment
import com.jakebarnby.filemanager.models.sources.onedrive.OneDriveFragment
import com.jakebarnby.filemanager.ui.sources.SourceFragment
import java.util.*

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SourcePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    internal val fragments: MutableList<SourceFragment> = ArrayList()

    init {
        fragments.add(LocalFragment.newInstance(SourceType.LOCAL.id, Environment.getExternalStorageDirectory().path))
        fragments.add(DropboxFragment.newInstance(SourceType.DROPBOX.id))
        fragments.add(GoogleDriveFragment.newInstance(SourceType.GOOGLE_DRIVE.id))
        fragments.add(OneDriveFragment.newInstance(SourceType.ONEDRIVE.id))
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragments[position].arguments?.getString("TITLE")
    }
}