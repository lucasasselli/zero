package com.lucasasselli.zero.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CatalogItem implements Parcelable {

    private String id;
    private String title;
    private String author;
    private String site;
    private int pro;
    private int test;
    private long downloads;

    // Parceling
    private CatalogItem(Parcel in) {
        String[] data = new String[7];
        in.readStringArray(data);

        this.id = data[0];
        this.title = data[1];
        this.author = data[2];
        this.site = data[3];
        this.pro = Integer.valueOf(data[4]);
        this.test = Integer.valueOf(data[5]);
        this.downloads = Long.valueOf(data[6]);
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

    public int getPro() {
        return pro;
    }

    public void setPro(int pro) {
        this.pro = pro;
    }

    public boolean isPro() {
        return pro == 1;
    }

    public void setPro(boolean pro) {
        this.pro = pro ? 1 : 0;
    }

    public int getTest() {
        return test;
    }

    public void setTest(int test) {
        this.test = test;
    }

    public long getDownloads() {
        return downloads;
    }

    public void setDownloads(long downloads) {
        this.downloads = downloads;
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
                String.valueOf(downloads)
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
