package com.jakebarnby.filemanager.tutorial;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 9/26/2017.
 */

public class TutorialPageFragment extends Fragment {

    private TextView            mTitle;
    private TextView            mContent;
    private TextView            mSummary;
    private ImageView           mImageView;
    private LinearLayout        mLayout;

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
        mImageView  .setImageResource(mTutorialPage.getDrawable());
        mLayout     .setBackgroundColor(mTutorialPage.getBackgroundColor());
    }

    private void initViews(View view) {
        mTitle      = view.findViewById(R.id.txt_tut_title);
        mContent    = view.findViewById(R.id.txt_tut_content);
        mSummary    = view.findViewById(R.id.txt_tut_summary);
        mImageView  = view.findViewById(R.id.img_tutorial);
        mLayout     = view.findViewById(R.id.container);
    }

}
