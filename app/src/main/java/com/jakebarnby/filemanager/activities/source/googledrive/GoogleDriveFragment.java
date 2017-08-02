package com.jakebarnby.filemanager.activities.source.googledrive;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.activities.source.SourceFragment;
import com.jakebarnby.filemanager.glide.GlideApp;
import com.jakebarnby.filemanager.models.GoogleDriveSource;
import com.jakebarnby.filemanager.util.Constants;

import static android.app.Activity.RESULT_OK;
import static com.jakebarnby.filemanager.util.Constants.RequestCodes.ACCOUNT_PICKER;
import static com.jakebarnby.filemanager.util.Constants.RequestCodes.GOOGLE_PLAY_SERVICES;
import static com.jakebarnby.filemanager.util.Constants.RequestCodes.GOOGLE_SIGN_IN;

/**
 * Created by Jake on 5/31/2017.
 */

public class GoogleDriveFragment extends SourceFragment {

    /**
     * Return a new instance of this Fragment
     * @param sourceName The name of the source controlled by this fragment
     * @return A new instance of this fragment
     */
    public static SourceFragment newInstance(String sourceName) {
        SourceFragment fragment = new GoogleDriveFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", sourceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSource = new GoogleDriveSource(Constants.Sources.GOOGLE_DRIVE, this);
        ((GoogleDriveSource)getSource()).setCredential(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        GlideApp
                .with(mSourceLogo)
                .load(R.drawable.ic_googledrive)
                .centerCrop()
                .into(mSourceLogo);

        return view;
    }

    @Override
    public void onResume() {
        if (!getSource().isLoggedIn()) {
            ((GoogleDriveSource)mSource).authGoogleSilent(this);
        }
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    ((GoogleDriveSource)mSource).getResultsFromApi(this);
                }
                break;
            case GOOGLE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    ((GoogleDriveSource)mSource).saveUserToken(this);
                }
                break;
            case ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        ((GoogleDriveSource)mSource).saveUserAccount(this, accountName);
                    }
                }
                break;
        }
    }
}
