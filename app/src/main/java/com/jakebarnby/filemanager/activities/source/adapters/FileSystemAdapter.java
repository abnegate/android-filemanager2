package com.jakebarnby.filemanager.activities.source.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.models.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;

import java.util.List;

/**
 * Created by Jake on 5/31/2017.
 */

public abstract class FileSystemAdapter extends RecyclerView.Adapter<FileSystemAdapter.FileViewHolder> {

    private TreeNode<SourceFile>        mParentDir;
    private List<TreeNode<SourceFile>>  mCurrentDirChildren;
    private OnFileClickedListener       mOnClickListener;
    private OnFileLongClickedListener   mOnLongClickListener;

    public FileSystemAdapter(TreeNode<SourceFile> parent, List<TreeNode<SourceFile>> children) {
        mParentDir = parent;
        mCurrentDirChildren = children;
    }

    public void setCurrentDirectory(TreeNode<SourceFile> parent, List<TreeNode<SourceFile>> children) {
        mParentDir = parent;
        mCurrentDirChildren = children;
        if (mParentDir != null && mParentDir.getData().canRead()) {
            if (mCurrentDirChildren.size() == 0) {
                mCurrentDirChildren.add(0, mParentDir);
            }
            else if (!mCurrentDirChildren.get(0).getData().equals(mParentDir.getData())){
                mCurrentDirChildren.add(0, mParentDir);
            }
        }
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        if (mParentDir != null && mParentDir.getData().canRead() && position == 0) {
            holder.mText.setText("..");
        } else {
            holder.mText.setText(mCurrentDirChildren.get(position).getData().getName());
        }
        if (mCurrentDirChildren.get(position).getData().isDirectory()) {
            //TODO: Set folder icon
            holder.mPreviewImage.setImageResource(R.drawable.ic_folder);
        } else {
            //TODO: Set file icon based on mime type
        }
    }

    @Override
    public int getItemCount() {
        return mCurrentDirChildren.size();
    }

    public TreeNode<SourceFile> getParentDir() {
        return mParentDir;
    }

    public void setParentDir(TreeNode<SourceFile> mParentDir) {
        this.mParentDir = mParentDir;
    }

    public List<TreeNode<SourceFile>> getCurrentDirChildren() {
        return mCurrentDirChildren;
    }

    public void setCurrentDirChildren(List<TreeNode<SourceFile>> mCurrentDirChildren) {
        this.mCurrentDirChildren = mCurrentDirChildren;
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        private ImageView   mPreviewImage;
        private TextView    mText;

        public FileViewHolder(View itemView) {
            super(itemView);
            mPreviewImage   = (ImageView) itemView.findViewById(R.id.image_file_preview);
            mText           = (TextView)  itemView.findViewById(R.id.text_file_title);
            itemView.setLongClickable(true);
            itemView.setOnClickListener(createOnClickListener());
            itemView.setOnLongClickListener(createOnLongClickListener());
        }

        /**
         * Create the on click listener for this file or folder
         * @return  The click listener
         */
        private View.OnClickListener createOnClickListener() {
            return v ->
                    mOnClickListener.OnClick(mCurrentDirChildren.get(getAdapterPosition()));
        }

        /**
         * The long click listener for this file or folder
         * @return
         */
        private View.OnLongClickListener createOnLongClickListener() {
            return v -> {
                mOnLongClickListener.OnLongClick(mCurrentDirChildren.get(getAdapterPosition()));
                return true;
            };
        }
    }

    public void setOnClickListener(OnFileClickedListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public void setOnLongClickListener(OnFileLongClickedListener mOnLongClickListener) {
        this.mOnLongClickListener = mOnLongClickListener;
    }

    @FunctionalInterface
    public interface OnFileClickedListener {
        void OnClick(TreeNode<SourceFile> file);
    }

    @FunctionalInterface
    public interface OnFileLongClickedListener {
        void OnLongClick(TreeNode<SourceFile> file);
    }
}
