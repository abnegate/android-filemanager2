package com.jakebarnby.filemanager.activities.source.adapters;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.models.Source;
import com.jakebarnby.filemanager.util.Constants;

import java.util.List;
import java.util.Locale;

/**
 * Created by Jake on 7/31/2017.
 */

public class SourceUsageAdapter extends RecyclerView.Adapter<SourceUsageAdapter.UsageViewHolder> {

    private List<Source> mSources;

    public SourceUsageAdapter(List<Source> sources) {
        mSources = sources;
    }

    @Override
    public UsageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_source_usage_list, parent, false);
        return new SourceUsageAdapter.UsageViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(UsageViewHolder holder, int position) {
        holder.mSourceName.setText(mSources.get(position).getSourceName());
        holder.mSourceUsage.setText(getUsageString(position));
        simulateProgress(holder.mPercentBar, (int) mSources.get(position).getUsedSpacePercent());
    }

    @Override
    public int getItemCount() {
        return mSources.size();
    }

    private String getUsageString(int position) {
        double usedGb = mSources.get(position).getUsedSpaceGB();
        double totalGb = mSources.get(position).getTotalSpaceGB();

        return String.format(
                Locale.getDefault(),
                "%.2f / %.2f GB",
                usedGb,
                totalGb);
    }

    private void simulateProgress(ProgressBar bar, int maxPercent) {
        ValueAnimator animator = ValueAnimator.ofInt(0, maxPercent);
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            bar.setProgress(progress);

        });
        animator.setDuration(Constants.Animation.PROGRESS_DURATION);
        animator.start();
    }

    public static class UsageViewHolder extends RecyclerView.ViewHolder {
        private CircleProgressBar   mPercentBar;
        private TextView            mSourceName;
        private TextView            mSourceUsage;

        public UsageViewHolder(View itemView) {
            super(itemView);
            mPercentBar = itemView.findViewById(R.id.prg_usage);
            mSourceName = itemView.findViewById(R.id.txt_source_title);
            mSourceUsage = itemView.findViewById(R.id.txt_space_consumption);
        }
    }
}
