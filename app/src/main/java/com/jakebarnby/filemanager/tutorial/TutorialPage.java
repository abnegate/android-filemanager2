package com.jakebarnby.filemanager.tutorial;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;


/**
 * Created by Jake on 9/26/2017.
 */

public class TutorialPage implements Parcelable {

    private String  mTitle;
    private String  mContent;
    private String  mSummary;

    @DrawableRes
    private int     mDrawable;
    @ColorInt
    private int     mBackgroundColor;

    TutorialPage(){}

    private TutorialPage(Parcel in) {
        this.mTitle = in.readString();
        this.mContent = in.readString();
        this.mSummary = in.readString();
        this.mDrawable = in.readInt();
        this.mBackgroundColor = in.readInt();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public int getDrawable() {
        return mDrawable;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public String getSummary() {
        return mSummary;
    }

    public static class Builder {

        private TutorialPage step;

        public Builder() {
            step = new TutorialPage();
        }

        public TutorialPage build() {
            return step;
        }

        public Builder setTitle(String title) {
            step.mTitle = title;
            return this;
        }

        public Builder setContent(String content) {
            step.mContent = content;
            return this;
        }

        public Builder setSummary(String summary) {
            step.mSummary = summary;
            return this;
        }

        public Builder setDrawable(int drawable) {
            step.mDrawable = drawable;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            step.mBackgroundColor = backgroundColor;
            return this;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mContent);
        dest.writeString(this.mSummary);
        dest.writeInt(this.mDrawable);
        dest.writeInt(this.mBackgroundColor);
    }

    public static final Parcelable.Creator<TutorialPage> CREATOR = new Parcelable.Creator<TutorialPage>() {
        @Override
        public TutorialPage createFromParcel(Parcel source) {
            return new TutorialPage(source);
        }

        @Override
        public TutorialPage[] newArray(int size) {
            return new TutorialPage[size];
        }
    };
}