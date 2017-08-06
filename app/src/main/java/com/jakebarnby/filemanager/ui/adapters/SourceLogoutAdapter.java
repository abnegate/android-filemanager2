package com.jakebarnby.filemanager.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.Source;

import java.util.List;

/**
 * Created by Jake on 8/6/2017.
 */

public class SourceLogoutAdapter extends RecyclerView.Adapter<SourceLogoutAdapter.LogoutViewHolder> {

    private List<Source> mSources;

    public SourceLogoutAdapter(List<Source> sources) {
        this.mSources = sources;
    }

    @Override
    public LogoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_logout_list, parent);
        return new LogoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LogoutViewHolder holder, int position) {
        holder.bindHolder(mSources.get(position));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class LogoutViewHolder extends RecyclerView.ViewHolder {

        private ImageView   mLogo;
        private TextView    mSourceName;
        private Button      mLogout;

        LogoutViewHolder(View itemView) {
            super(itemView);
            this.mLogo          = itemView.findViewById(R.id.img_logo_logout);
            this.mSourceName    = itemView.findViewById(R.id.txt_source_title);
            this.mLogout        = itemView.findViewById(R.id.btn_logout);
        }

        public void bindHolder(Source source) {
            mSourceName.setText(source.getSourceName());
            mLogout.setText(source.isLoggedIn() ? R.string.logout : R.string.connect);
            mLogout.setOnClickListener((view -> {
                if (source.isLoggedIn()) {
                    source.logout();
                } else {
                    source.authenticateSource(mLogo.getContext());
                }
            }));
        }
    }
}
