package com.jakebarnby.filemanager.sources.dropbox;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.glide.GlideApp;
import com.jakebarnby.filemanager.sources.SourceFragment;
import com.jakebarnby.filemanager.sources.models.Source;
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
    public static SourceFragment newInstance(Source source) {
        SourceFragment fragment = new DropboxFragment();
        Bundle args = new Bundle();
        args.putString(Constants.FRAGMENT_TITLE, source.getSourceName());
        fragment.setArguments(args);
        fragment.setSource(source);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
