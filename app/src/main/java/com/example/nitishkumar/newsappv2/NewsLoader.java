package com.example.nitishkumar.newsappv2;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<NewsData>>{

    private String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }
/*********************** invoked on loader start******************/
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
/**************************** run tread in background to load data***************/
    @Override
    public List<NewsData> loadInBackground() {
        if (mUrl == null)
        {
            return null;
        }

        List<NewsData> newsData = QueryUtils.fetchNewsData(mUrl);
        return newsData;
    }
}
