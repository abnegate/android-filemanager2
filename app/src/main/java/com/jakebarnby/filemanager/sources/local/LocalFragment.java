package com.jakebarnby.filemanager.sources.local;

import android.Manifest;
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
 * Created by Jake on 5/31/2017.
 */

public class LocalFragment extends SourceFragment {

    /**
     * Return a new instance of this Fragment
     *
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName, String rootPath) {
        SourceFragment fragment = new LocalFragment();
        Bundle args = new Bundle();
        args.putString(Constants.FRAGMENT_TITLE, sourceName);
        args.putString(Constants.LOCAL_ROOT, rootPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getArguments().getString(Constants.FRAGMENT_TITLE);
        String rootPath = getArguments().getString(Constants.LOCAL_ROOT);
        mSource = new LocalSource(name, rootPath, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.RequestCodes.STORAGE_PERMISSIONS);
    }
}
