package com.example.nitishkumar.newsappv2;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final int READ_TIMEOUT_TIME = 10000;
    private static final int CONNECTION_TIMEOUT_TIME = 15000;
    private static final int OK_RESPONSE_CODE = 200;

    public QueryUtils() {
    }
/********************* fetch json data by giving url****************/
    public static List<NewsData> fetchNewsData(String requestUrl)
    {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        return extractNewsFromJson(jsonResponse);
    }
/************************** create url from string *******************/
    private static URL createUrl (String stringUrl)
    {
        URL url = null;
        try
        {
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e)
        {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }
/*************************** make http request and handle response**********************/
    private static String makeHttpRequest(URL url) throws IOException
    {
        String jsonResponse = "";
        if (url == null)
        {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT_TIME);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT_TIME);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == OK_RESPONSE_CODE)
            {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else
            {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        }catch (IOException e)
        {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        }
        finally {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if (inputStream != null)
            {
                inputStream.close();
            }
        }

        return jsonResponse;
    }
/******************************* read from stream and return string output************/
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null)
            {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }
/************************* fetch and parse json****************************/
    private static List<NewsData> extractNewsFromJson(String NewsJSON)
    {
        if (TextUtils.isEmpty(NewsJSON))
        {
            return null;
        }
        List<NewsData> newsDataSet = new ArrayList<>();
        try {
            JSONObject baseJSONObject = new JSONObject(NewsJSON);
            JSONObject responseJSONObject = baseJSONObject.optJSONObject("response");
            JSONArray resultJSONArray = responseJSONObject.optJSONArray("results");

            for (int i = 0; i < resultJSONArray.length(); i++)
            {
                JSONObject currentNewsJSONObject = resultJSONArray.optJSONObject(i);
                String newsType = currentNewsJSONObject.optString("sectionName");
                String newsdateTimeStamp = currentNewsJSONObject.optString("webPublicationDate");
                 newsdateTimeStamp = newsdateTimeStamp.split("T")[0];
                String newswebsiteURL = currentNewsJSONObject.optString("webUrl");
                JSONObject fieldsJSONObject = currentNewsJSONObject.optJSONObject("fields");
                String newsHeadline = fieldsJSONObject.optString("headline");
                String newsThumbnail = fieldsJSONObject.optString("thumbnail");
                JSONArray tagsJSONArray = currentNewsJSONObject.optJSONArray("tags");
                String newsAuthor = "";
                for (int j = 0; j < tagsJSONArray.length(); j++)
                {
                    newsAuthor = "";
                    JSONObject currentNewsTag = tagsJSONArray.optJSONObject(j);
                    String currentNewsAuthor = currentNewsTag.optString("firstName");
                    currentNewsAuthor += " " + currentNewsTag.optString("lastName");

                    newsAuthor += currentNewsAuthor + ",";
                }
                NewsData newsData = new NewsData(newsThumbnail, newsHeadline, newsdateTimeStamp, newsAuthor, newsType, newswebsiteURL);
                newsDataSet.add(newsData);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem in Parsing JSON.", e);
        }

        return newsDataSet;
    }
}