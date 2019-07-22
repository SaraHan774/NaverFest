package com.gahee.rss_v2.data.wwf.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class WWFArticle implements Parcelable {

    private String title;
    private String guid;
    private String pubDate;
    private String description;
    private String contentEncoded;
    private List<String> extractedMediaLinks;

    public WWFArticle(String title, String guid, String pubDate, String description, String contentEncoded) {
        this.title = title;
        this.guid = guid;
        this.pubDate = pubDate;
        this.description = description;
        this.contentEncoded = contentEncoded;
    }

    protected WWFArticle(Parcel in) {
        title = in.readString();
        guid = in.readString();
        pubDate = in.readString();
        description = in.readString();
        extractedMediaLinks = in.createStringArrayList();
        contentEncoded = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(guid);
        dest.writeString(pubDate);
        dest.writeString(description);
        dest.writeStringList(extractedMediaLinks);
        dest.writeString(contentEncoded);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WWFArticle> CREATOR = new Creator<WWFArticle>() {
        @Override
        public WWFArticle createFromParcel(Parcel in) {
            return new WWFArticle(in);
        }

        @Override
        public WWFArticle[] newArray(int size) {
            return new WWFArticle[size];
        }
    };

    public void setExtractedMediaLinks(List<String> extractedMediaLinks) {
        this.extractedMediaLinks = extractedMediaLinks;
    }

    public List<String> getExtractedMediaLinks() {
        return extractedMediaLinks;
    }

    public String getTitle() {
        return title;
    }

    public String getGuid() {
        return guid;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }
}