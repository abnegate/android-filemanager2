package com.jakebarnby.tutorial;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.jakebarnby.tutorial.util.Constants;

/**
 * Created by Jake on 9/26/2017.
 */

public class TutorialPageFragment extends Fragment {

    private TextView            mTitle;
    private TextView            mContent;
    private TextView            mSummary;
    private ImageView           mImageView;

    private TutorialPage        mTutorialPage;

    static TutorialPageFragment newInstance(TutorialPage tutorialPage) {
        TutorialPageFragment fragment = new TutorialPageFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.TUT_PAGE_KEY, tutorialPage);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTutorialPage = getArguments().getParcelable(Constants.TUT_PAGE_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorialpage, container, false);

        initViews(view);
        initData();

        return view;
    }

    private void initData() {
        mTitle      .setText(mTutorialPage.getTitle());
        mContent    .setText(mTutorialPage.getContent());
        mSummary    .setText(mTutorialPage.getSummary());
        setImage();
    }

    private void setImage() {
        ScaleAnimation anim = new ScaleAnimation(0.5f, 1f, 0.5f, 1.0f);
        OvershootInterpolator interpolator = new OvershootInterpolator(1f);

        AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        alphaAnim.setDuration(Constants.TUT_IMAGE_FADE_DURATION);

        anim.setInterpolator(interpolator);
        anim.setDuration(Constants.TUT_IMAGE_BOUNCE_DURATION);

        Target<Bitmap> target = new BaseTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                mImageView.setImageBitmap(resource);
                mImageView.startAnimation(anim);
                mImageView.startAnimation(alphaAnim);
            }

            @Override
            public void getSize(SizeReadyCallback cb) {
                cb.onSizeReady(250, 250);
            }

            @Override
            public void removeCallback(SizeReadyCallback cb) {}
        };

        Glide
                .with(getContext())
                .asBitmap()
                .load(mTutorialPage.getImageUrl())
                .into(target);
    }

    private void initViews(View view) {
        mTitle      = view.findViewById(R.id.txt_tut_title);
        mContent    = view.findViewById(R.id.txt_tut_content);
        mSummary    = view.findViewById(R.id.txt_tut_summary);
        mImageView  = view.findViewById(R.id.img_tutorial);
    }

}
