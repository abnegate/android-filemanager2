package com.jakebarnby.filemanager.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 9/23/2017.
 */

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    private List<TreeNode<SourceFile>>  mResults;
    private List<TreeNode<SourceFile>>  mBackup;
    private OnSearchResultClicked       mListener;

    public SearchResultAdapter(List<TreeNode<SourceFile>> results, OnSearchResultClicked searchResultClicked) {
        this.mResults = results;
        this.mBackup = results;
        this.mListener = searchResultClicked;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_searchresult_list, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        holder.bindHolder(mResults.get(position));
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    /**
     * Filter out all sources except the given string
     * @param name  Name of the source to keep
     */
    public void removeAllSourceExcept(String name) {
        mResults = new ArrayList<>();
        for (int i = 0; i < mBackup.size(); i++) {
            if (mBackup.get(i).getData().getSourceName().equals(name)) {
                mResults.add(mBackup.get(i));
            }
        }
    }

    public void resetDataset() {
        mResults = new ArrayList<>(mBackup);
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        private ImageView mSourceLogo;
        private TextView mText;
        private TextView mSubText;

        SearchViewHolder(View itemView) {
            super(itemView);
            mSourceLogo = itemView.findViewById(R.id.img_source_logo);
            mText = itemView.findViewById(R.id.txt_item_title);
            mSubText = itemView.findViewById(R.id.text_file_path);
        }

        void bindHolder(TreeNode<SourceFile> file) {
            mSourceLogo.setImageResource(Utils.resolveLogoId(file.getData().getSourceName()));
            mText.setText(file.getData().getName());
            mSubText.setText(file.getData().getPath());
            itemView.setOnClickListener((view -> mListener.navigateToFile(file)));
        }
    }
}

