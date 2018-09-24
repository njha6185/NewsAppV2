package com.example.nitishkumar.newsappv2;

public class NewsData {
    private String newsThumbNailImageUrl;
    private String newsHeading;
    private String newsDate;
    private String newsAuthor;
    private String newsType;
    private String newsWebURL;

    public NewsData(String newsThumbNailImageUrl, String newsHeading, String newsDate, String newsAuthor, String newsType, String newsWebURL) {
        this.newsThumbNailImageUrl = newsThumbNailImageUrl;
        this.newsHeading = newsHeading;
        this.newsDate = newsDate;
        this.newsAuthor = newsAuthor;
        this.newsType = newsType;
        this.newsWebURL = newsWebURL;
    }

    public String getNewsThumbNailImageUrl() {
        return newsThumbNailImageUrl;
    }

    public String getNewsHeading() {
        return newsHeading;
    }

    public String getNewsDate() {
        return newsDate;
    }

    public String getNewsAuthor() {
        return newsAuthor;
    }

    public String getNewsType() {
        return newsType;
    }

    public String getNewsWebURL() {
        return newsWebURL;
    }

    public void setNewsWebURL(String newsWebURL) {
        this.newsWebURL = newsWebURL;
    }

    public void setNewsThumbNailImageUrl(String newsThumbNailImageUrl) {
        this.newsThumbNailImageUrl = newsThumbNailImageUrl;
    }

    public void setNewsHeading(String newsHeading) {
        this.newsHeading = newsHeading;
    }

    public void setNewsDate(String newsDate) {
        this.newsDate = newsDate;
    }

    public void setNewsAuthor(String newsAuthor) {
        this.newsAuthor = newsAuthor;
    }

    public void setNewsType(String newsType) {
        this.newsType = newsType;
    }
}