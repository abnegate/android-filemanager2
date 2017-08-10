package com.jakebarnby.filemanager.sources.onedrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceFragment;
import com.jakebarnby.filemanager.glide.GlideApp;
import com.jakebarnby.filemanager.util.Constants;

/**
 * Created by Jake on 6/7/2017.
 */
public class OneDriveFragment extends SourceFragment {

    /**
     * Return a new instance of this Fragment
     *
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new OneDriveFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = new OneDriveSource(Constants.Sources.ONEDRIVE, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((OneDriveSource)getSource()).checkForAccessToken(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((OneDriveSource)getSource()).getClient().handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }
}
