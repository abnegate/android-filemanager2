package com.jakebarnby.filemanager.activities.source.adapters;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.jakebarnby.filemanager.R;
import com.jakebarnby.filemanager.glide.GlideApp;
import com.jakebarnby.filemanager.models.files.SourceFile;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.TreeNode;
import com.jakebarnby.filemanager.util.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Jake on 5/31/2017.
 */

public abstract class FileSystemAdapter extends RecyclerView.Adapter<FileSystemAdapter.FileViewHolder>
                                        implements ListPreloader.PreloadModelProvider<TreeNode<SourceFile>>{

    private static final long                    FADE_DURATION = 1000L;
    private static final long                    TRANSLATE_DURATION = 700L;

    private TreeNode<SourceFile>                 mParentDir;
    private List<TreeNode<SourceFile>>           mCurrentDirChildren;
    private OnFileClickedListener                mOnClickListener;
    private OnFileLongClickedListener            mOnLongClickListener;
    private boolean                              mMultiSelectEnabled;
    private TreeNode<SourceFile>                 mRootTreeNode;
    private TreeNode<SourceFile>                 mCurrentDir;
    private int                                  lastPosition = -1;
    private AnimationSet                         mAnimationSet;

    private RequestBuilder<Drawable>             mRequestBuilder;
    private ViewPreloadSizeProvider              mSizeProvider;

    /**
     * Create a new FileSystemAdapter instance with the given root tree node
     * @param rootNode      The root node of the file tree
     * @param sizeProvider
     */
    public FileSystemAdapter(TreeNode<SourceFile> rootNode,
                             RequestBuilder<Drawable> requestBuilder,
                             ViewPreloadSizeProvider sizeProvider) {
        mRootTreeNode = rootNode;
        mRequestBuilder = requestBuilder;
        mSizeProvider = sizeProvider;
        mAnimationSet = new AnimationSet(false);
        setCurrentDirectory(mRootTreeNode);
    }

    /**
     * Toggle multi-select mode
     * @param mMultiSelectEnabled   Whether mdoe is enabled or disabled
     */
    public void setMultiSelectEnabled(boolean mMultiSelectEnabled) {
        this.mMultiSelectEnabled = mMultiSelectEnabled;
    }

    /**
     * Set the current directory based of the given current directory
     * @param currentDir    Directory to set as current
     */
    public void setCurrentDirectory(TreeNode<SourceFile> currentDir) {
        setCurrentDirectory(currentDir.getParent(), currentDir.getChildren());
        mCurrentDir = currentDir;
    }

    /**
     * Set the current directory including it's children and parent
     * @param parent        The parent of the directory to set as current
     * @param children      The children of the directory to set as current
     */
    public void setCurrentDirectory(TreeNode<SourceFile> parent, List<TreeNode<SourceFile>> children) {
        mParentDir = parent;
        mCurrentDirChildren = children;
        if (mParentDir != null) {
            if (mCurrentDirChildren.size() == 0) {
                mCurrentDirChildren.add(0, mParentDir);
            }
            else if (!mCurrentDirChildren.get(0).getData().equals(mParentDir.getData())){
                mCurrentDirChildren.add(0, mParentDir);
            }
        }
    }

    public ListPreloader.PreloadSizeProvider<TreeNode<SourceFile>> getSizeProvider() {
        return mSizeProvider;
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.mCheckbox.setVisibility(mMultiSelectEnabled ? View.VISIBLE : View.GONE);

        if (mParentDir != null && position == 0) {
            holder.mText.setText("..");
            if (mMultiSelectEnabled) {
                holder.mCheckbox.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.mText.setText(mCurrentDirChildren.get(position).getData().getName());
        }

        if (mMultiSelectEnabled && holder.mCheckbox.getVisibility() == View.VISIBLE) {
            TranslateAnimation translate = new TranslateAnimation(-500f, 0.0f, 0.0f, 0.0f);
            translate.setInterpolator(new DecelerateInterpolator(3.0f));
            translate.setDuration(400);
            holder.mCheckbox.startAnimation(translate);
        }

        if (mCurrentDirChildren.get(position).getData().isDirectory()) {
            holder.mPreviewImage.setImageResource(R.drawable.ic_folder);
        } else {
            setThumbnail(holder, position);
        }
        setAnimation(holder.itemView, position);
        mSizeProvider.setView(holder.mPreviewImage);
    }

    /**
     * Sets the thumbnail for this item
     * @param holder        {@link RecyclerView.ViewHolder} to set the thumbnail for
     * @param position      Position of this item in the adapter
     */
    private void setThumbnail(FileViewHolder holder, int position) {
        GlideApp
                .with(holder.itemView)
                .load(mCurrentDir.getData().getSourceName().equals(Constants.Sources.LOCAL)?
                        new File(mCurrentDirChildren.get(position).getData().getThumbnailLink()):
                        Uri.parse(mCurrentDirChildren.get(position).getData().getThumbnailLink()))
                .error(R.mipmap.ic_launcher_round)
                .placeholder(R.mipmap.ic_launcher_round)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(withCrossFade())
                .override(128,128)
                .thumbnail(0.3f)
                .circleCrop()
                .into(holder.mPreviewImage);
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

    /**
     *
     * @param mOnClickListener
     */
    public void setOnClickListener(OnFileClickedListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    /**
     *
     * @param mOnLongClickListener
     */
    public void setOnLongClickListener(OnFileLongClickedListener mOnLongClickListener) {
        this.mOnLongClickListener = mOnLongClickListener;
    }


    @Override
    public List<TreeNode<SourceFile>> getPreloadItems(int position) {
        return Collections.singletonList(mCurrentDirChildren.get(position));
    }

    @Override
    public RequestBuilder<Drawable> getPreloadRequestBuilder(TreeNode<SourceFile> item) {
        return mRequestBuilder
                .load(item.getData().getSourceName().equals(Constants.Sources.LOCAL) ?
                    new File(item.getData().getThumbnailLink()) :
                    Uri.parse(item.getData().getThumbnailLink()));
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
