package com.jakebarnby.filemanager.ui.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.managers.SelectedFilesManager;
import com.jakebarnby.filemanager.sources.models.SourceFile;
import com.jakebarnby.filemanager.sources.models.SourceType;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.tutorial.glide.GlideApp;

import java.io.File;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Jake on 5/31/2017.
 */

public abstract class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<TreeNode<SourceFile>>      mCurrentDirChildren;
    private OnFileClickedListener           mOnClickListener;
    private OnFileLongClickedListener       mOnLongClickListener;
    private boolean                         mMultiSelectEnabled;

    /**
     * Create a new FileAdapter instance with the given root tree node
     * @param rootNode     The root node of the file tree
     */
    public FileAdapter(TreeNode<SourceFile> rootNode) {
        setCurrentDirectory(rootNode);
    }

    /**
     * Toggle multi-select mode
     * @param mMultiSelectEnabled Whether mdoe is enabled or disabled
     */
    public void setMultiSelectEnabled(boolean mMultiSelectEnabled) {
        this.mMultiSelectEnabled = mMultiSelectEnabled;
    }

    /**
     * Set the current directory based of the given current directory
     * @param currentDir Directory to set as current
     */
    public void setCurrentDirectory(TreeNode<SourceFile> currentDir) {
        mCurrentDirChildren = currentDir.getChildren();
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        boolean showHiddenFiles = PreferenceUtils.getBoolean(holder.itemView.getContext(), Constants.Prefs.HIDDEN_FOLDER_KEY, false);


        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams();
        if (!showHiddenFiles && mCurrentDirChildren.get(position).getData().isHidden()) {
            holder.itemView.setVisibility(View.GONE);
            param.width = 0;
            param.height = 0;
            param.setMargins(0,0,0,0);
        } else if (showHiddenFiles) {
            holder.itemView.setVisibility(View.VISIBLE);
            param.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            param.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        }

        holder.bindHolder(mCurrentDirChildren.get(position));
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
            mPreviewImage = itemView.findViewById(R.id.img_file_preview);
            mCheckbox = itemView.findViewById(R.id.checkbox);
            mText = itemView.findViewById(R.id.txt_item_title);
            itemView.setLongClickable(true);
            itemView.setOnClickListener(createOnClickListener());
            itemView.setOnLongClickListener(createOnLongClickListener());

            mCheckbox.setOnClickListener(v -> mOnClickListener.OnClick(
                    mCurrentDirChildren.get(getAdapterPosition()), mCheckbox.isChecked(), getAdapterPosition()));
        }

        /**
         *
         * @param currentDir
         */
        void bindHolder(TreeNode<SourceFile> currentDir) {
            String name = currentDir.getData().getName();
            if (currentDir.getData().isDirectory()) {
                mText.setText(name);
                mPreviewImage.setImageResource(R.drawable.ic_folder);
            } else {
                if (name.lastIndexOf('.') > 0) {
                    mText.setText(name.substring(0, name.lastIndexOf('.')));
                } else {
                    mText.setText(name);
                }
                setThumbnail(currentDir);
            }

            if (!mMultiSelectEnabled) {
                mCheckbox.setChecked(false);
                mCheckbox.setVisibility(View.GONE);
            } else {
                mCheckbox.setVisibility(View.VISIBLE);
            }

            if (mMultiSelectEnabled && mCheckbox.getVisibility() == View.VISIBLE) {
                TranslateAnimation translate = new TranslateAnimation(-500f, 0.0f, 0.0f, 0.0f);
                translate.setInterpolator(new DecelerateInterpolator(3.0f));
                translate.setDuration(400);
                mCheckbox.startAnimation(translate);
            }

            if (SelectedFilesManager.getInstance().getOperationCount() > 0) {
                if (SelectedFilesManager
                        .getInstance()
                        .getCurrentSelectedFiles()
                        .contains(currentDir)) {
                    mCheckbox.setChecked(true);
                }
            }

            List<TreeNode<SourceFile>> currentSelection =
                    SelectedFilesManager.getInstance().getCurrentSelectedFiles();

            if (currentSelection != null && currentSelection.contains(currentDir)) {
                mCheckbox.setChecked(true);
            } else {
                mCheckbox.setChecked(false);
            }
        }

        /**
         *
         * @param currentDir
         */
        private void setThumbnail(TreeNode<SourceFile> currentDir) {
            GlideApp
                    .with(itemView)
                    .load(currentDir.getData().getSourceType() == SourceType.LOCAL ?
                            new File(currentDir.getData().getThumbnailLink()) :
                            currentDir.getData().getThumbnailLink())
                    .error(R.mipmap.ic_launcher_round)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(withCrossFade())
                    .override(100, 100)
                    .thumbnail(0.2f)
                    .circleCrop()
                    .into(mPreviewImage);
        }

        /**
         * Create the on click listener for this file or folder
         *
         * @return The click listener
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
         *
         * @return
         */
        private View.OnLongClickListener createOnLongClickListener() {
            return v -> {
                if (!mMultiSelectEnabled) {
                    mMultiSelectEnabled = true;
                }
                mOnLongClickListener.OnLongClick(mCurrentDirChildren.get(getAdapterPosition()));
                return true;
            };
        }
    }

    /**
     * @param mOnClickListener
     */
    public void setOnClickListener(OnFileClickedListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    /**
     * @param mOnLongClickListener
     */
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
