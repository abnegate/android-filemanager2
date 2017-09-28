package com.jakebarnby.tutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Jake on 9/26/2017.
 */
public class TutorialPagerAdapter extends FragmentPagerAdapter {
    private List<TutorialPage> mTutorialPages;

    TutorialPagerAdapter(FragmentManager fm, List<TutorialPage> tutorialPages) {
        super(fm);
        this.mTutorialPages = tutorialPages;
    }

    @Override
    public Fragment getItem(int position) {
        return TutorialPageFragment.newInstance(mTutorialPages.get(position));
    }

    @Override
    public int getCount() {
        return mTutorialPages.size();
    }
}
