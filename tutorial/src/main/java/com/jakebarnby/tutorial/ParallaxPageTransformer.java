package com.jakebarnby.tutorial;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by Jake on 9/26/2017.
 */

public class ParallaxPageTransformer implements ViewPager.PageTransformer {

    private int id;
    private int border = 0;
    private float speed = 0.3f;

    public ParallaxPageTransformer(int id) {
        this.id = id;
    }

    @Override
    public void transformPage(View view, float position) {
        View parallaxView = view.findViewById(id);

        if (parallaxView == null ||
            position <= -1 ||
            position >= 1) {
            return;
        }

        float width = parallaxView.getWidth();
        parallaxView.setTranslationX(-(position * width * speed));
        float sc = ((float) view.getWidth() - border) / view.getWidth();
        if (position == 0) {
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (!Float.isNaN(sc)) {
            view.setScaleX(sc);
            view.setScaleY(sc);
        }
    }

    public void setBorder(int px) {
        border = px;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
