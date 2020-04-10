package com.jakebarnby.filemanager.ui.adapters

import android.os.Environment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.jakebarnby.filemanager.sources.SourceFragment
import com.jakebarnby.filemanager.sources.dropbox.DropboxFragment
import com.jakebarnby.filemanager.sources.googledrive.GoogleDriveFragment
import com.jakebarnby.filemanager.sources.local.LocalFragment
import com.jakebarnby.filemanager.sources.onedrive.OneDriveFragment
import com.jakebarnby.filemanager.util.Constants.Sources
import java.util.*

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SourcePagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm!!) {
    internal val fragments: MutableList<SourceFragment> = ArrayList()
    fun getFragments(): List<SourceFragment> {
        return fragments
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragments[position].arguments!!.getString("TITLE")
    }

    init {
        fragments.add(LocalFragment.Companion.newInstance(Sources.LOCAL, Environment.getExternalStorageDirectory().path))
        fragments.add(DropboxFragment.Companion.newInstance(Sources.DROPBOX))
        fragments.add(GoogleDriveFragment.Companion.newInstance(Sources.GOOGLE_DRIVE))
        fragments.add(OneDriveFragment.Companion.newInstance(Sources.ONEDRIVE))
    }
}