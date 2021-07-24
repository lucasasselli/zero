package com.zero.zerolivewallpaper.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CatalogItem implements Parcelable {

    private String id;
    private String title;
    private String author;
    private String site;
    private boolean pro;
    private boolean test;

    // Parceling
    private CatalogItem(Parcel in) {
        String[] data = new String[6];
        in.readStringArray(data);

        this.id = data[0];
        this.title = data[1];
        this.author = data[2];
        this.site = data[3];
        this.pro = Boolean.valueOf(data[4]);
        this.test = Boolean.valueOf(data[5]);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public boolean getPro() {
        return pro;
    }

    public boolean isPro() {
        return pro;
    }

    public void setPro(boolean pro) {
            this.pro = pro;
    }

    public boolean getTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.id,
                this.title,
                this.author,
                this.site,
                String.valueOf(this.pro),
                String.valueOf(this.test),
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CatalogItem createFromParcel(Parcel in) {
            return new CatalogItem(in);
        }

        public CatalogItem[] newArray(int size) {
            return new CatalogItem[size];
        }
    };
}
