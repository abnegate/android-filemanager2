package com.jakebarnby.tutorial;

import android.animation.ArgbEvaluator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 9/26/2017.
 */

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private List<TutorialPage>      mTutorialPages;
    private TutorialPagerAdapter    mTutAdapter;
    private ViewPager               mViewPager;
    private Button                  mNext,
                                    mPrev;
    private LinearLayout            mIndicatorLayout;
    private ArgbEvaluator           mArgbEvaluator;
    private int                     mCurrentItem;
    private String                  mPrevText,
                                    mNextText,
                                    mFinishText,
                                    mCancelText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tutorial);
        mTutorialPages = new ArrayList<>();
        mArgbEvaluator = new ArgbEvaluator();
        initTexts();
        initViews();
        initAdapter();
    }

    private void initTexts() {
        mPrevText = getString(R.string.back);
        mCancelText = getString(R.string.cancel);
        mFinishText = getString(R.string.finish);
        mNextText = getString(R.string.next);
    }

    private void initAdapter() {
        mTutAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), mTutorialPages);
        mViewPager.setAdapter(mTutAdapter);
        mViewPager.setPageTransformer(false, new ParallaxPageTransformer(R.id.txt_tut_summary));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position < (mTutAdapter.getCount() -1)) {
                    int result = (Integer) mArgbEvaluator.evaluate(
                            positionOffset,
                            mTutorialPages.get(position).getBackgroundColor(),
                            mTutorialPages.get(position + 1).getBackgroundColor());
                    mViewPager.setBackgroundColor(result);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        changeStatusBarColor(result);
                    }
                } else {
                    mViewPager.setBackgroundColor(mTutorialPages.get(mTutorialPages.size() - 1).getBackgroundColor());
                }
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentItem = position;
                controlPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeStatusBarColor(int backgroundColor) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(backgroundColor);
    }

    private void controlPosition(int position) {
        notifyIndicator();
        if (position == mTutorialPages.size() - 1) {
            mNext.setText(mFinishText);
            mPrev.setText(mPrevText);
        } else if (position == 0) {
            mPrev.setText(mCancelText);
            mNext.setText(mNextText);
        } else {
            mPrev.setText(mPrevText);
            mNext.setText(mNextText);
        }
    }

    private void initViews() {
        mCurrentItem = 0;

        mViewPager = findViewById(R.id.viewPager);
        mNext            = findViewById(R.id.next);
        mPrev            = findViewById(R.id.prev);
        mIndicatorLayout = findViewById(R.id.indicatorLayout);

        mNext.setOnClickListener(this);
        mPrev.setOnClickListener(this);
    }

    public void addFragment(TutorialPage tutorialPage) {
        mTutorialPages.add(tutorialPage);
        mTutAdapter.notifyDataSetChanged();
        notifyIndicator();
        controlPosition(mCurrentItem);
    }

    public void addFragment(TutorialPage tutorialPage, int position) {
        mTutorialPages.add(position, tutorialPage);
        mTutAdapter.notifyDataSetChanged();
        notifyIndicator();
    }

    public void notifyIndicator() {
        if (mIndicatorLayout.getChildCount() > 0)
            mIndicatorLayout.removeAllViews();

        for (int i = 0; i < mTutorialPages.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setPadding(8, 8, 8, 8);
            int drawable = R.drawable.circle_black;
            if (i == mCurrentItem)
                drawable = R.drawable.circle_white;

            imageView.setImageResource(drawable);

            int finalI = i;
            imageView.setOnClickListener(v -> changeFragment(finalI));

            mIndicatorLayout.addView(imageView);
        }

    }

    @Override
    public void onBackPressed() {
        if (mCurrentItem == 0) {
            saveFinished();
            super.onBackPressed();
        } else {
            changeFragment(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.next) {
            changeFragment(true);
        } else if (v.getId() == R.id.prev) {
            changeFragment(false);
        }
    }

    private void changeFragment(int position) {
        mViewPager.setCurrentItem(position, true);
    }

    private void changeFragment(boolean isNext) {
        int item = mCurrentItem;
        if (isNext) {
            item++;
        } else {
            item--;
        }

        if (item < 0 || item == mTutorialPages.size()) {
            saveFinished();
            finish();
        } else
            mViewPager.setCurrentItem(item, true);
    }

    private void saveFinished() {
//        getSharedPreferences(Constants.Prefs.PREFS, MODE_PRIVATE)
//                .edit()
//                .putBoolean(Constants.Prefs.TUT_SEEN_KEY, true)
//                .apply();
    }
}