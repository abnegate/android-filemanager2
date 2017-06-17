package com.jakebarnby.filemanager.activities.source.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.util.List;

/**
 * Created by Jake on 5/31/2017.
 */

public abstract class FileSystemAdapter extends RecyclerView.Adapter<FileSystemAdapter.FileViewHolder> {

    private static final long           FADE_DURATION = 1000L;
    private static final long           TRANSLATE_DURATION = 700L;

    private TreeNode<SourceFile>        mParentDir;
    private List<TreeNode<SourceFile>>  mCurrentDirChildren;
    private OnFileClickedListener       mOnClickListener;
    private OnFileLongClickedListener   mOnLongClickListener;
    private boolean                     mMultiSelectEnabled;
    private TreeNode<SourceFile>        mRootTreeNode;
    private TreeNode<SourceFile>        mCurrentDir;
    private int                         lastPosition = -1;
    private AnimationSet mAnimationSet;


    public FileSystemAdapter(TreeNode<SourceFile> rootNode) {
        mRootTreeNode = rootNode;
        setCurrentDirectory(mRootTreeNode);
        mAnimationSet = new AnimationSet(false);
    }

    public void setMultiSelectEnabled(boolean mMultiSelectEnabled) {
        this.mMultiSelectEnabled = mMultiSelectEnabled;
    }

    public void setCurrentDirectory(TreeNode<SourceFile> currentDir) {
        setCurrentDirectory(currentDir.getParent(), currentDir.getChildren());
        mCurrentDir = currentDir;
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
        //TODO: Probably animate items here
        holder.mCheckbox.setVisibility(mMultiSelectEnabled ? View.VISIBLE : View.GONE);
        if (mMultiSelectEnabled) {
            TranslateAnimation translate = new TranslateAnimation(-100f, 0.0f, 0.0f, 0.0f);
            translate.setInterpolator(new DecelerateInterpolator(3.0f));
            translate.setDuration(TRANSLATE_DURATION);
            holder.mPreviewImage.startAnimation(translate);
            holder.mText.startAnimation(translate);
            holder.mCheckbox.startAnimation(translate);
        }


        if (mParentDir != null && mParentDir.getData().canRead() && position == 0) {
            holder.mText.setText("..");
            if (mMultiSelectEnabled) {
                holder.mCheckbox.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.mText.setText(mCurrentDirChildren.get(position).getData().getName());
        }

        if (mCurrentDirChildren.get(position).getData().isDirectory()) {
            holder.mPreviewImage.setImageResource(R.drawable.ic_folder);
        } else {
            //TODO: Set file icon as thumbnail for file
        }

        setAnimation(holder.itemView, position);
    }

    /**
     *
     * @param viewToAnimate
     * @param position
     */
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            final int screenHeight = Utils.getScreenHeight(viewToAnimate.getContext());
            TranslateAnimation translate = new TranslateAnimation(0.0f, 0.0f, screenHeight, 0.0f);
            translate.setInterpolator(new DecelerateInterpolator(3.0f));
            translate.setDuration(TRANSLATE_DURATION);

            AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setInterpolator(new DecelerateInterpolator(2.0f));
            alpha.setDuration(FADE_DURATION);

            mAnimationSet.addAnimation(alpha);
            mAnimationSet.addAnimation(translate);
            viewToAnimate.startAnimation(mAnimationSet);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mCurrentDirChildren.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private ImageView   mPreviewImage;
        private CheckBox    mCheckbox;
        private TextView    mText;

        FileViewHolder(View itemView) {
            super(itemView);
            mPreviewImage   =  itemView.findViewById(R.id.image_file_preview);
            mCheckbox       =  itemView.findViewById(R.id.checkbox);
            mText           =  itemView.findViewById(R.id.text_file_title);
            itemView.setLongClickable(true);
            itemView.setOnClickListener(createOnClickListener());
            itemView.setOnLongClickListener(createOnLongClickListener());

            mCheckbox.setOnClickListener(v -> mOnClickListener.OnClick(
                    mCurrentDirChildren.get(getAdapterPosition()), mCheckbox.isChecked(), getAdapterPosition()));
        }

        /**
         * Create the on click listener for this file or folder
         * @return  The click listener
         */
        private View.OnClickListener createOnClickListener() {
            return v -> {
                if (mMultiSelectEnabled) {
                    mCheckbox.setChecked(!mCheckbox.isChecked());
                }
                mOnClickListener.OnClick(mCurrentDirChildren.get(getAdapterPosition()),
                                                                mCheckbox.isChecked(),
                                                                getAdapterPosition());
            };
        }

        /**
         * The long click listener for this file or folder
         * @return
         */
        private View.OnLongClickListener createOnLongClickListener() {
            return v -> {
                if (!mMultiSelectEnabled) {
                    mMultiSelectEnabled = true;
                    //TODO: Animate checkbox here
                }
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
        void OnClick(TreeNode<SourceFile> file, boolean isChecked, int position);
    }

    @FunctionalInterface
    public interface OnFileLongClickedListener {
        void OnLongClick(TreeNode<SourceFile> file);
    }
}
