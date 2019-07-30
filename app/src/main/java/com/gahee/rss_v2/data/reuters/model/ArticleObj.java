package com.gahee.rss_v2.data.reuters.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ArticleObj implements Parcelable {

    private String mArticleTitle;
    private String mArticleLink;
    private String mArticleDescription;
    private String mArticlePubDate;
    private String mVideoLink;
    private String mThumbnailLink;

    public ArticleObj(String mArticleTitle, String mArticleLink, String mArticleDescription, String mArticlePubDate, String mVideoLink, String mThumbnailLink) {
        this.mArticleTitle = mArticleTitle;
        this.mArticleLink = mArticleLink;
        this.mArticleDescription = mArticleDescription;
        this.mArticlePubDate = mArticlePubDate;
        this.mVideoLink = mVideoLink;
        this.mThumbnailLink = mThumbnailLink;
    }

    public ArticleObj(){

    }


    private ArticleObj(Parcel in) {
        mArticleTitle = in.readString();
        mArticleLink = in.readString();
        mArticleDescription = in.readString();
        mArticlePubDate = in.readString();
        mVideoLink = in.readString();
        mThumbnailLink = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mArticleTitle);
        dest.writeString(mArticleLink);
        dest.writeString(mArticleDescription);
        dest.writeString(mArticlePubDate);
        dest.writeString(mVideoLink);
        dest.writeString(mThumbnailLink);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ArticleObj> CREATOR = new Creator<ArticleObj>() {
        @Override
        public ArticleObj createFromParcel(Parcel in) {
            return new ArticleObj(in);
        }

        @Override
        public ArticleObj[] newArray(int size) {
            return new ArticleObj[size];
        }
    };

    public String getmArticleTitle() {
        return mArticleTitle;
    }

    public String getmArticleLink() {
        return mArticleLink;
    }

    public String getmArticleDescription() {
        return mArticleDescription;
    }

    public String getmArticlePubDate() {
        return mArticlePubDate;
    }

    public String getmVideoLink() {
        return mVideoLink;
    }

    public String getmThumbnailLink() {
        return mThumbnailLink;
    }
}
