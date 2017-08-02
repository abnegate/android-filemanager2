package com.jakebarnby.filemanager.activities.source.dropbox;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.glide.GlideApp;
import com.jakebarnby.filemanager.models.DropboxSource;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 5/31/2017.
 */

public class DropboxFragment extends SourceFragment {

    private static final String TAG = "DROPBOX";

    /**
     * Return a new instance of this Fragment
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new DropboxFragment();
        Bundle args = new Bundle();
        args.putString(Constants.FRAGMENT_TITLE, sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = new DropboxSource(Constants.Sources.DROPBOX, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        GlideApp
                .with(mSourceLogo)
                .load(R.drawable.ic_dropbox)
                .centerCrop()
                .into(mSourceLogo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DropboxSource)getSource()).checkForAccessToken(getContext());
    }
}
