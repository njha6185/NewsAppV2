package com.example.nitishkumar.newsappv2;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsData>>,
        View.OnClickListener, android.support.v7.widget.SearchView.OnQueryTextListener{

    private static final String GUARDIAN_API_REQUEST_URL = "https://content.guardianapis.com" ;
    String temp = "https://content.guardianapis.com/world/india?q=12%20years%20a%20slave&format=json&tag=film/film,tone/reviews&from-date=2010-01-01&show-tags=contributor&show-fields=headline,thumbnail,short-url&order-by=relevance&api-key=0d7fa385-b5f8-4575-a98a-85b890e847ba";
    private String searchQuery ;
    private android.support.v7.widget.SearchView searchView;
    private static final int NEWS_LOADER_ID = 1;
    private ArrayList<NewsData> newsdataArray;
    private NewsDataAdapter mNewsAdapter;
    LoaderManager loaderManager;
    @BindView(R.id.empty_view) TextView mEmptyStateTextView;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    @BindView(R.id.recyclerViewList) RecyclerView recyclerView;
    @BindView(R.id.loading_indicator) View loadingIndicator;
    @BindView(R.id.fromDateText) TextView fromDateTextView;
    @BindView(R.id.toDateText) TextView toDateTextView;
    @BindView(R.id.refreshImage) ImageView refreshImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fromDateTextView.setText(currentSystemdate());
        toDateTextView.setText(currentSystemdate());

        fromDateTextView.setOnClickListener(this);
        toDateTextView.setOnClickListener(this);
        refreshImageView.setOnClickListener(this);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        newsdataArray = new ArrayList<>();
        mNewsAdapter = new NewsDataAdapter(this, newsdataArray);
        recyclerView.setAdapter(mNewsAdapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                NewsData currentNewsData = mNewsAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNewsData.getNewsWebURL());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        else {
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.connectivity_service_problem);
        }

        if (networkInfo != null && networkInfo.isConnected())
        {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        }
        else {
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<NewsData>> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy = sharedPreferences.getString(getString(R.string.setting_order_by_key), getString(R.string.setting_order_by_default));
        String newsAreaBy = sharedPreferences.getString(getString(R.string.setting_news_area_key), getString(R.string.setting_news_area_default));
        String noOfNewsPerPage = sharedPreferences.getString(getString(R.string.setting_no_of_news_per_page_key), getString(R.string.setting_no_of_news_per_page_default));
        String fromDate = fromDateTextView.getText().toString();
        String toDate = toDateTextView.getText().toString();
//        String wordSeachParams = handleIntent(getIntent());

        Uri baseUri = Uri.parse(GUARDIAN_API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath(newsAreaBy);
        uriBuilder.appendQueryParameter("q", searchQuery);
        uriBuilder.appendQueryParameter("format","json");
//        if (navbarresult == all)
//        {
//            uriBuilder.appendQueryParameter("section","film");
//        }
        uriBuilder.appendQueryParameter("from-date",fromDate);
        uriBuilder.appendQueryParameter("to-date",toDate);
        uriBuilder.appendQueryParameter("page-size",noOfNewsPerPage);
        uriBuilder.appendQueryParameter("page","1");
        uriBuilder.appendQueryParameter("show-fields","headline,thumbnail,short-url");
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("api-key", getString(R.string.API_KEY));


        return new NewsLoader(this, temp);
    }

    @Override
    public void onLoadFinished(Loader<List<NewsData>> loader, List<NewsData> data) {
        loadingIndicator.setVisibility(View.GONE);
        newsdataArray.clear();

        if (data != null && !data.isEmpty())
        {
            newsdataArray.addAll(data);
            mNewsAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setVisibility(View.GONE);
        }
        else
        {
            connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            else {
                loadingIndicator.setVisibility(View.GONE);
                mEmptyStateTextView.setText(R.string.connectivity_service_problem);
            }
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (networkInfo != null && networkInfo.isConnected())
            {
                mEmptyStateTextView.setText(R.string.no_news);
            }
            else
            {
                mEmptyStateTextView.setText(R.string.no_internet_connection);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsData>> loader) {
        newsdataArray.clear();
        mNewsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        newsdataArray.clear();
        mNewsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (android.support.v7.widget.SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting)
        {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String currentSystemdate()
    {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    @Override
    public void onClick(final View v) {

        if (v == fromDateTextView || v == toDateTextView)
        {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    String formattedDate = "";
                    String dateString = year + "-" + month + "-" + dayOfMonth;
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
                    try {
                        Date d = df.parse(dateString);
                        df = new SimpleDateFormat("yyyy-MM-dd");
                        formattedDate = df.format(d);
                    } catch (ParseException e) {
                        Log.e("MainActivity", "Date parsing Problem "+e);
                    }

                    if (v == fromDateTextView)
                    {
                        fromDateTextView.setText(formattedDate);
                        Toast.makeText(MainActivity.this, formattedDate, Toast.LENGTH_SHORT).show();
                    }
                    if (v == toDateTextView)
                    {
                        toDateTextView.setText(formattedDate);
                        Toast.makeText(MainActivity.this, formattedDate, Toast.LENGTH_SHORT).show();
                    }
                }
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if (v == refreshImageView)
        {
            recyclerView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
            Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Search For " + query, Toast.LENGTH_SHORT).show();
        searchQuery = query;
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}