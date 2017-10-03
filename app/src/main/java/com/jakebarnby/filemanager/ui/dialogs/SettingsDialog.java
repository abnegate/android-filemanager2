package com.jakebarnby.filemanager.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.SourceActivity;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;

/**
 * Created by Jake on 10/3/2017.
 */

public class SettingsDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.action_settings));

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_settings, null);
        initViews(rootView);

        builder.setView(rootView);
        builder.setNegativeButton(getString(R.string.close), (dialog, which) -> {
            ((SourceActivity)getActivity()).initAllRecyclers();
            dialog.dismiss();
        });

        return builder.create();
    }

    private void initViews(View rootView) {
        CheckBox foldersFirst = rootView.findViewById(R.id.ckb_folders_first);
        CheckBox hiddenFolders = rootView.findViewById(R.id.ckb_hidden_files);

        boolean bFoldersFirst = PreferenceUtils.getBoolean(getContext(), Constants.Prefs.FOLDER_FIRST_KEY, true);
        boolean bShowHiddenFiles = PreferenceUtils.getBoolean(getContext(), Constants.Prefs.HIDDEN_FOLDER_KEY, false);

        foldersFirst.setChecked(bFoldersFirst);
        hiddenFolders.setChecked(bShowHiddenFiles);

        foldersFirst.setOnCheckedChangeListener((compoundButton, checked) -> {
            PreferenceUtils.savePref(getContext(), Constants.Prefs.FOLDER_FIRST_KEY, checked);
        });

        hiddenFolders.setOnCheckedChangeListener((compoundButton, checked) -> {
            PreferenceUtils.savePref(getContext(), Constants.Prefs.HIDDEN_FOLDER_KEY, checked);
        });

//        Button ossButton = rootView.findViewById(R.id.btn_oss_licenses);
//        ossButton.setOnClickListener((view) -> {
//            Intent intent = new Intent(getActivity(), OssLicensesMenuActivity.class);
//            String title = getString(R.string.oss_license_title);
//            intent.putExtra(Constants.DIALOG_TITLE_KEY, title);
//            startActivity(intent);
//
//        });
    }
}
