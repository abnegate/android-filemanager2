package com.jakebarnby.filemanager.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.glide.GlideApp;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Jake on 9/23/2017.
 */

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    private List<TreeNode<SourceFile>>  mResults;
    private OnSearchResultClicked       mListener;

    public SearchResultAdapter(List<TreeNode<SourceFile>> results, OnSearchResultClicked searchResultClicked) {
        this.mResults = results;
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

    class SearchViewHolder extends RecyclerView.ViewHolder {

        private ImageView mSourceLogo;
        private TextView mText;

        SearchViewHolder(View itemView) {
            super(itemView);
            mSourceLogo = itemView.findViewById(R.id.image_source_logo);
            mText = itemView.findViewById(R.id.text_file_title);
        }

        void bindHolder(TreeNode<SourceFile> file) {
            mSourceLogo.setImageResource(Utils.resolveLogoId(file.getData().getSourceName()));
            mText.setText(file.getData().getName());
            itemView.setOnClickListener((view -> mListener.navigateToFile(file)));
        }
    }
}

