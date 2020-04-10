package com.jakebarnby.tutorial;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by Jake on 9/26/2017.
 */

public class ParallaxPageTransformer implements ViewPager.PageTransformer {

    private int     mId;
    private int     mBorder = 0;
    private float   mSpeed = 0.3f;

    public ParallaxPageTransformer(int id) {
        this.mId = id;
    }

    @Override
    public void transformPage(View view, float position) {
        View parallaxView = view.findViewById(mId);
        if (parallaxView != null) {
            if (position > -1 && position < 1) {
                float width = parallaxView.getWidth();
                parallaxView.setTranslationX(-(position * width * mSpeed));
                float sc = ((float)view.getWidth() - mBorder)/ view.getWidth();
                if (position == 0) {
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else {
                    view.setScaleX(sc);
                    view.setScaleY(sc);
                }
            }
        }
    }

    public void setBorder(int px) {
        mBorder = px;
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }
}
