package com.jakebarnby.tutorial;

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
    private String  mImageUrl;

    @ColorInt
    private int     mBackgroundColor;


    TutorialPage(){}

    /**
     * Create a new tutorial page using the data in the parcel
     * @param in
     */
    private TutorialPage(Parcel in) {
        this.mTitle = in.readString();
        this.mContent = in.readString();
        this.mSummary = in.readString();
        this.mImageUrl = in.readString();
        this.mBackgroundColor = in.readInt();
    }

    String getTitle() {
        return mTitle;
    }

    String getContent() {
        return mContent;
    }

    String getImageUrl() {
        return mImageUrl;
    }

    int getBackgroundColor() {
        return mBackgroundColor;
    }

    String getSummary() {
        return mSummary;
    }


    /**
     * Builder for <code>TutorialPage</code>
     */
    public static class Builder {

        private TutorialPage mPage;

        public Builder() {
            mPage = new TutorialPage();
        }

        public TutorialPage build() {
            return mPage;
        }

        /**
         * Set the title text of the page
         * @param title     Title text to display on the page
         * @return          The current page
         */
        public Builder setTitle(String title) {
            mPage.mTitle = title;
            return this;
        }

        /**
         * Set the context text of the page
         * @param content   Content text to display on the page
         * @return          The current page
         */
        public Builder setContent(String content) {
            mPage.mContent = content;
            return this;
        }

        /**
         * Set the summary text of the page
         * @param summary   Summary text to display on the page
         * @return
         */
        public Builder setSummary(String summary) {
            mPage.mSummary = summary;
            return this;
        }

        /**
         * Set the drawable of the page
         * @param url       Lnik to the image to display
         * @return          The current page
         */
        public Builder setImageUrl(String url) {
            mPage.mImageUrl = url;
            return this;
        }

        /**
         * Set the background color of the page
         * @param backgroundColor   Color int value to set as background color for the page
         * @return                  The current page
         */
        public Builder setBackgroundColor(@ColorInt int backgroundColor) {
            mPage.mBackgroundColor = backgroundColor;
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
        dest.writeString(this.mImageUrl);
        dest.writeInt(this.mBackgroundColor);
    }

    public static final Creator<TutorialPage> CREATOR = new Creator<TutorialPage>() {
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